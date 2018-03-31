/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.faces.impl.state.invokers;

import java.io.IOException;
import java.io.Serializable;
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
import org.apache.common.faces.state.component.UIStateChartRoot;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.apache.common.faces.impl.state.StateFlowParams;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.faces.impl.state.utils.SharedUtils;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.BEFORE_RESTORE_VIEW;
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.InvokeContext;
import org.apache.common.scxml.model.ModelException;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document may not
 * contain external namespace elements, further invokes etc.
 */
public class ViewInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(ViewInvoker.class.getName());

    public static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * invokeId ID.
     */
    private transient String invokeId;
    /**
     * Invoking parent SCXMLExecutor
     */
    private transient SCXMLExecutor executor;

    /**
     * Cancellation status.
     */
    private boolean cancelled;

    private String control;
    private String viewId;
    private String stateKey;
    private Object lastViewState;
    private Map<String, Object> vieparams;

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getInvokeId() {
        return invokeId;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor executor) {
        this.executor = executor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        // not used
        return null;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final InvokeContext ictx, final String source, final Map<String, Object> params) throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();
        boolean oldProcessingEvents = fc.isProcessingEvents();
        try {
            fc.setProcessingEvents(false);
            ExternalContext ec = fc.getExternalContext();
            ViewHandler vh = fc.getApplication().getViewHandler();

            NavigationCase navCase = findNavigationCase(fc, source);
            viewId = source;
            try {
                viewId = navCase.getToViewId(fc);
            } catch (NullPointerException th) {
                throw new IOException(String.format("Invoke source \"%s\" not found", source));
            } catch (Throwable th) {
                throw new IOException(String.format("Invoke source \"%s\" not found", source), th);
            }
            viewId = vh.deriveLogicalViewId(fc, viewId);

            Map<String, Object> options = new HashMap();
            vieparams = new HashMap();
            for (String key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (skey.startsWith("@view.")) {
                    skey = skey.substring(6);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    vieparams.put(skey, value);
                }
            }

            boolean transientState = false;
            if (options.containsKey("transient")) {
                Object val = options.get("transient");
                if (val instanceof String) {
                    transientState = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    transientState = (Boolean) val;
                }
            }

            boolean redirect = StateFlowParams.isDefaultViewRedirect();

            if (options.containsKey("redirect")) {
                Object val = options.get("redirect");
                if (val instanceof String) {
                    redirect = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    redirect = (Boolean) val;
                }
            }

            if (!transientState) {
                stateKey = "__@@Invoke:" + invokeId + ":";
            }

            if (stateKey != null) {
                stateKey = "__@@Invoke:" + invokeId + ":";

                Context stateContext = executor.getGlobalContext();
                lastViewState = stateContext.get(stateKey + "ViewState");
                String lastViewId = (String) stateContext.get(stateKey + "LastViewId");
                if (lastViewId != null) {
                    viewId = lastViewId;
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

                String url = viewHandler.getRedirectURL(fc, viewId, SharedUtils.evaluateExpressions(fc, param), true);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);
                updateRenderTargets(fc, viewId);
                ec.redirect(url);
                fc.responseComplete();
            } else if (redirect) {
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

                String url = viewHandler.getRedirectURL(fc, viewId, SharedUtils.evaluateExpressions(fc, param), true);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);
                updateRenderTargets(fc, viewId);
                ec.redirect(url);
                fc.responseComplete();
            } else {
                if (fc.getViewRoot() != null) {
                    String currentViewId = fc.getViewRoot().getViewId();
                    if (currentViewId.equals(viewId)) {
                        return;
                    }
                }

                UIViewRoot viewRoot;

                if (lastViewState != null) {
                    fc.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                    viewRoot = vh.restoreView(fc, viewId);

                    applyParams(fc, viewRoot, params);

                    fc.setViewRoot(viewRoot);
                    fc.setProcessingEvents(true);
                    vh.initView(fc);
                    
                    applyParams(fc, viewRoot, vieparams);
                    vieparams = null;
                    lastViewState = null;
                } else {
                    SCXML stateChart = null;
                    viewRoot = null;
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(fc, viewId);

                        if (metadata != null) {
                            viewRoot = metadata.createMetadataView(fc);
                            UIComponent facet = viewRoot.getFacet(STATECHART_FACET_NAME);
                            if (facet != null) {
                                UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent("main");
                                if (uichart != null) {
                                    stateChart = uichart.getStateChart();
                                }
                            }
                        }
                    }

                    applyParams(fc, viewRoot, vieparams);
                    vieparams = null;

                    if (viewRoot != null) {
                        if (!ViewMetadata.hasMetadata(viewRoot)) {
                            fc.renderResponse();
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

    private void applyParams(FacesContext context, UIViewRoot viewRoot, Map<String, Object> params) {
        if (viewRoot != null) {
            if (ViewMetadata.hasMetadata(viewRoot)) {
                VisitContext vc = VisitContext.createVisitContext(context);
                viewRoot.visitTree(vc, (VisitContext ivc, UIComponent target) -> {

                    if (target instanceof UIViewParameter) {
                        UIViewParameter parametr = (UIViewParameter) target;
                        String name = parametr.getName();
                        if (params.containsKey(name)) {
                            parametr.setValue(params.get(name));
                        }
                    }
                    return VisitResult.ACCEPT;
                });
            }
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

    protected NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        return navigationHandler.getNavigationCase(context, null, outcome);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final InvokeContext ictx, final TriggerEvent event) throws InvokerException {
        if (cancelled) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        if (viewId.equals(event.getSendId())) {
            if (event.getType() == TriggerEvent.CALL_EVENT && (event.getName()
                    .startsWith(AFTER_PHASE_EVENT_PREFIX))) {
                try {

                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                    context.getELContext().putContext(SCXMLExecutor.class, executor);

                    Context stateContext = ictx.getContext();
                    context.getELContext().putContext(Context.class, stateContext);
                } catch (ModelException ex) {
                    throw new InvokerException(ex);
                }
            } else if (event.getType() == TriggerEvent.CALL_EVENT && (event.getName()
                    .startsWith(BEFORE_RESTORE_VIEW))) {

                if (lastViewState != null) {
                    context.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                    lastViewState = null;
                }

            } else if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
                ExternalContext ec = context.getExternalContext();

                Map<String, String> params = new HashMap<>();
                params.putAll(ec.getRequestParameterMap());

                String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                EventBuilder evb = new EventBuilder("view.action." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);

                evb.data(params);
                evb.sendId(invokeId);
                executor.addEvent(evb.build());
            }
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;

        if (control.equals("statefull")) {
            FacesContext fc = FacesContext.getCurrentInstance();
            UIViewRoot viewRoot = fc.getViewRoot();
            if (viewRoot != null) {
                String lastViewId = viewRoot.getViewId();
                RenderKit renderKit = fc.getRenderKit();
                ResponseStateManager rsm = renderKit.getResponseStateManager();
                Object viewState = rsm.getState(fc, lastViewId);
                Context storeContext = executor.getGlobalContext();

                storeContext.setLocal(stateKey + "ViewState", viewState);
                storeContext.setLocal(stateKey + "LastViewId", lastViewId);
            }
        }
    }
}
