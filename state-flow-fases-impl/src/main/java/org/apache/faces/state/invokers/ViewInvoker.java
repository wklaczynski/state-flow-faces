/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.invokers;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.PathResolver;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.invoke.InvokerException;
import javax.faces.state.NamespacePrefixesHolder;
import javax.faces.state.model.Parallel;
import javax.faces.state.PathResolverHolder;
import javax.faces.state.model.State;
import javax.faces.state.model.TransitionTarget;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.apache.faces.state.utils.AsyncTrigger;
import org.apache.faces.state.utils.SharedUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ViewInvoker implements Invoker, Serializable, PathResolverHolder, NamespacePrefixesHolder {

    public static final String OUTCOME_EVENT_PREFIX = "faces.outcome.";
    public static final String VIEW_PARAMS_MAP = "___@@@ParamsMap____";
    public static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    private String parentStateId;
    private String eventPrefix;
    private String statePrefix;
    private boolean cancelled;
    private FlowInstance parentnstance;
    private static final String invokePrefix = ".view.";
    private PathResolver pathResolver;
    private String stateStore;
    private String control;
    private String viewId;
    private Map namespaces;

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
        this.eventPrefix = this.parentStateId + invokePrefix;
        this.statePrefix = this.parentStateId + "view.state.";
        this.cancelled = false;
    }

    @Override
    public void setInstance(FlowInstance instance) {
        this.parentnstance = instance;
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
            viewId = navCase.getToViewId(fc);
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
                StateFlowExecutor executor = parentnstance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                String stateKey = "";
                TransitionTarget target = state;
                while (target != null) {
                    stateKey = target.getId() + ":" + stateKey;
                    target = state.getParent();
                }
                FlowContext stateContext = parentnstance.getContext(state);
                if (!stateKey.endsWith(":")) {
                    stateKey += ":";
                }
                stateKey = "__@@" + stateKey;

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
                UIViewRoot viewRoot = null;
                if (viewState != null) {
                    fc.getAttributes().put(FACES_VIEW_STATE, viewState);
                    viewRoot = vh.restoreView(fc, viewId);
                    fc.setViewRoot(viewRoot);
                    fc.setProcessingEvents(true);
                    vh.initView(fc);
                } else {
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(fc, viewId);

                        if (metadata != null) {
                            viewRoot = metadata.createMetadataView(fc);

                            if (!ViewMetadata.hasMetadata(viewRoot)) {
                                fc.renderResponse();
                            } else {
                                VisitContext vc = VisitContext.createVisitContext(fc);
                                viewRoot.visitTree(vc, (VisitContext context, UIComponent target) -> {

                                    if (target instanceof UIViewParameter) {
                                        UIViewParameter parametr = (UIViewParameter) target;
                                        String name = parametr.getName();
                                    }

                                    return VisitResult.ACCEPT;
                                });
                            }
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
        } catch (MalformedURLException ex) {
            throw new InvokerException(ex);
        } catch (IOException ex) {
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
                FlowTriggerEvent te = new FlowTriggerEvent(eventPrefix + outcome, FlowTriggerEvent.SIGNAL_EVENT);
                ExternalContext ec = context.getExternalContext();

                StateFlowExecutor executor = parentnstance.getExecutor();
                Iterator iterator = executor.getCurrentStatus().getStates().iterator();
                State state = ((State) iterator.next());
                FlowContext stateContext = parentnstance.getContext(state);

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
                        FlowContext storeContext = parentnstance.getRootContext();

                        if (storeTarget != null) {
                            storeContext = parentnstance.getContext(storeTarget);
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
    }

    @Override
    public void setPathResolver(PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    @Override
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    protected NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        return navigationHandler.getNavigationCase(context, null, outcome);
    }

    @Override
    public void setNamespaces(Map namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public Map getNamespaces() {
        return namespaces;
    }

}
