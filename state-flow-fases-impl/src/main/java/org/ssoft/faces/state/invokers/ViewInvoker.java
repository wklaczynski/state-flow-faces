/*
 * Copyright 2018 Waldemar Kłaczyński.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ssoft.faces.state.invokers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.invoke.InvokerException;
import javax.faces.state.model.Parallel;
import javax.faces.state.invoke.AbstractInvoker;
import javax.faces.state.model.State;
import javax.faces.state.model.TransitionTarget;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.ssoft.faces.state.utils.AsyncTrigger;
import org.ssoft.faces.state.utils.SharedUtils;
import javax.faces.state.annotation.Statefull;
import javax.faces.state.component.UIStateChartRoot;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ViewInvoker extends AbstractInvoker implements Invoker {

    private final static Logger logger = Logger.getLogger(ViewInvoker.class.getName());

    public static final String OUTCOME_EVENT_PREFIX = "faces.outcome.";
    public static final String VIEW_PARAMS_MAP = "___@@@ParamsMap____";
    public static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    private boolean cancelled;

    @Statefull
    private String stateStore;
    @Statefull
    private String control;
    @Statefull
    private String viewId;

    @Override
    public void setParentStateId(String parentStateId) {
        super.setParentStateId(parentStateId);
        this.cancelled = false;
    }

    @Override
    public void invoke(String source, Map params) throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();
        boolean oldProcessingEvents = fc.isProcessingEvents();
        try {
            fc.setProcessingEvents(false);
            ExternalContext ec = fc.getExternalContext();
            ViewHandler vh = fc.getApplication().getViewHandler();

            getViewParamsContext(fc).putAll(params);

            NavigationCase navCase = findNavigationCase(fc, source);
            viewId = source;
            try {
                viewId = navCase.getToViewId(fc);
            } catch (NullPointerException th) {
                //throw new IOException(String.format("Invoke source \"%s\" not found", source));
            } catch (Throwable th) {
                //throw new IOException(String.format("Invoke source \"%s\" not found", source), th);
            }
            viewId = vh.deriveLogicalViewId(fc, viewId);

            Map<String, Object> options = new HashMap();
            Map<String, Object> vieparams = new HashMap();
            for (Object key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (skey.startsWith("@view.")) {
                    skey = skey.substring(6);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    vieparams.put(skey, value);
                }
            }

            if (options.containsKey("store")) {
                stateStore = (String) options.get("store");
            } else {
                stateStore = "parallel";
            }
            boolean trans = false;
            if (options.containsKey("transient")) {
                Object val = options.get("transient");
                if (val instanceof String) {
                    trans = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    trans = (Boolean) val;
                }
            }

            if (trans) {
                control = "stateless";
            } else {
                control = "statefull";
            }

            Object viewState = null;
            if (control.equals("statefull")) {
                StateFlowExecutor executor = instance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                String stateKey = "__@@" + state.getClientId() + ":";

                FlowContext stateContext = instance.getContext(state);
                viewState = stateContext.get(stateKey + "ViewState");
                String lastViewId = (String) stateContext.get(stateKey + "LastViewId");
                if (lastViewId != null) {
                    viewId = lastViewId;
                }

            }

            if (fc.getViewRoot() != null) {
                String currentViewId = fc.getViewRoot().getViewId();
                if (currentViewId.equals(viewId)) {
                    return;
                }
            }

            StateChart stateChart = null;
            UIViewRoot viewRoot = null;
            ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
            ViewMetadata metadata = null;
            if (vdl != null) {
                metadata = vdl.getViewMetadata(fc, viewId);

                if (metadata != null) {
                    viewRoot = metadata.createMetadataView(fc);
                    UIComponent facet = viewRoot.getFacet(StateChart.STATECHART_FACET_NAME);
                    if (facet != null) {
                        UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent("main");
                        if (uichart != null) {
                            stateChart = uichart.getStateChart();
                        }
                    }
                }
            }

            PartialViewContext pvc = fc.getPartialViewContext();
            if (pvc != null && pvc.isAjaxRequest()) {
                Map<String, List<String>> param = new HashMap<>();
                Map<String, List<String>> navparams = navCase.getParameters();
                if (navparams != null) {
                    params.putAll(navparams);
                }

                Iterator<Map.Entry<String, Object>> it = ((Map<String, Object>) vieparams).entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, Object> p = it.next();
                    param.put(p.getKey(), Collections.singletonList(p.getValue().toString()));
                }

                Application application = fc.getApplication();
                ViewHandler viewHandler = application.getViewHandler();

                if (viewState != null) {
                    RenderKit renderKit = fc.getRenderKit();
                    ResponseStateManager rsm = renderKit.getResponseStateManager();
                    String viewStateId = rsm.getViewState(fc, viewState);
                    param.put(ResponseStateManager.VIEW_STATE_PARAM, Arrays.asList(viewStateId));
                }

                String redirect = viewHandler.getRedirectURL(fc, viewId, SharedUtils.evaluateExpressions(fc, param), true);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);
                updateRenderTargets(fc, viewId);
                ec.getFlash().setRedirect(true);
                ec.getFlash().setKeepMessages(true);
                ec.redirect(redirect);
                fc.responseComplete();
            } else {
                if (viewState != null) {
                    fc.getAttributes().put(FACES_VIEW_STATE, viewState);
                    viewRoot = vh.restoreView(fc, viewId);
                    fc.setViewRoot(viewRoot);
                    fc.setProcessingEvents(true);
                    vh.initView(fc);
                } else {
                    if (viewRoot != null) {
                        if (!ViewMetadata.hasMetadata(viewRoot)) {
                            fc.renderResponse();
                        } else {
                            VisitContext vc = VisitContext.createVisitContext(fc);
                            viewRoot.visitTree(vc, (VisitContext context, UIComponent target) -> {

                                if (target instanceof UIViewParameter) {
                                    UIViewParameter parametr = (UIViewParameter) target;
                                    String name = parametr.getName();
                                    //parametr.setSubmittedValue(viewState);
                                }

                                return VisitResult.ACCEPT;
                            });
                        }
                    }
                    if (vdl == null || metadata == null) {
                        fc.renderResponse();
                    }

                    if (viewRoot == null) {
                        viewRoot = vh.createView(fc, viewId);
                    }

                    viewRoot.setViewId(viewId);
                }
                viewRoot.getViewMap(true).put(VIEW_PARAMS_MAP, vieparams);
                viewRoot.getViewMap(true).putAll(vieparams);
                fc.setViewRoot(viewRoot);
                fc.renderResponse();
            }
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex);
        } finally {
            fc.setProcessingEvents(oldProcessingEvents);
        }

    }

    private void clearViewMapIfNecessary(UIViewRoot root, String newId) {

        if (root != null && !root.getViewId().equals(newId)) {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null) {
                viewMap.clear();
            }
        }

    }

    private void updateRenderTargets(FacesContext ctx, String newId) {

        if (ctx.getViewRoot() == null || !ctx.getViewRoot().getViewId().equals(newId)) {
            PartialViewContext pctx = ctx.getPartialViewContext();
            if (!pctx.isRenderAll()) {
                pctx.setRenderAll(true);
            }
        }

    }

    private ViewParamsContext getViewParamsContext(FacesContext fc) {
        ExternalContext ec = fc.getExternalContext();
        ViewParamsContext viewParamsContext = (ViewParamsContext) ec.getRequestMap().get(ViewParamsContext.class.getName());
        if (viewParamsContext == null) {
            viewParamsContext = new ViewParamsContext();
            ec.getRequestMap().put(ViewParamsContext.class.getName(), viewParamsContext);
        }
        return viewParamsContext;

    }

    @Override
    public void parentEvents(FlowTriggerEvent[] evts) throws InvokerException {
        if (cancelled) {
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot view = context.getViewRoot();
        Map params = (Map) view.getViewMap(true).get(VIEW_PARAMS_MAP);
        if (params != null) {
            getViewParamsContext(context).putAll(params);
        }

        for (FlowTriggerEvent event : evts) {
            if (event.getType() == FlowTriggerEvent.SIGNAL_EVENT && event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
                String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                FlowTriggerEvent te = new FlowTriggerEvent(event(ACTION_EVENT, outcome), FlowTriggerEvent.SIGNAL_EVENT);
                ExternalContext ec = context.getExternalContext();

                StateFlowExecutor executor = instance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                FlowContext stateContext = instance.getContext(state);

                Map<String, String> parameterMap = ec.getRequestParameterMap();
                for (Map.Entry<String, String> entry : parameterMap.entrySet()) {
                    stateContext.setLocal(entry.getKey(), entry.getValue());
                }

                if (control.equals("statefull")) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    UIViewRoot viewRoot = fc.getViewRoot();
                    if (viewRoot != null) {
                        String lastViewId = viewRoot.getViewId();
                        RenderKit renderKit = fc.getRenderKit();
                        ResponseStateManager rsm = renderKit.getResponseStateManager();
                        Object viewState = rsm.getState(fc, lastViewId);
                        String stateKey = "";
                        TransitionTarget storeTarget = null;
                        TransitionTarget target = state;
                        while (target != null) {
                            stateKey = target.getId() + ":" + stateKey;
                            if (storeTarget == null) {
                                if ("state".equals(stateStore) && target instanceof State) {
                                    storeTarget = target;
                                }
                                if ("parallel".equals(stateStore) && target instanceof Parallel) {
                                    storeTarget = target;
                                }
                            }
                            target = state.getParent();
                        }
                        FlowContext storeContext = instance.getRootContext();

                        if (storeTarget != null) {
                            storeContext = instance.getContext(storeTarget);
                        }
                        if (!stateKey.endsWith(":")) {
                            stateKey += ":";
                        }
                        stateKey = "__@@" + stateKey;

                        storeContext.setLocal(stateKey + "ViewState", viewState);
                        storeContext.setLocal(stateKey + "LastViewId", lastViewId);
                    }
                }
                new AsyncTrigger(executor, te).start();
            }
        }
    }

    @Override
    public void cancel() throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();
        getViewParamsContext(fc).clear();
        cancelled = true;
        FlowTriggerEvent te = new FlowTriggerEvent(event(INVOKE_EVENT, INVOKE_CANCEL_EVENT), FlowTriggerEvent.SIGNAL_EVENT);
        new AsyncTrigger(instance.getExecutor(), te).start();
    }

    protected NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        return navigationHandler.getNavigationCase(context, null, outcome);
    }

}
