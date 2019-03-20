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
package org.ssoft.faces.impl.state.invokers;

import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
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
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.ssoft.faces.impl.state.StateFlowParams;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import static javax.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static javax.faces.state.StateFlow.BEFORE_APPLY_REQUEST_VALUES;
import static javax.faces.state.StateFlow.VIEW_EVENT_PREFIX;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.StateChartExecuteContext;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.model.ModelException;
import static javax.faces.state.StateFlow.EXECUTOR_CONTEXT_VIEW_PATH;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document may not
 * contain external namespace elements, further invokes etc.
 */
public class ViewInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(ViewInvoker.class.getName());

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

    private String viewId;
    private boolean resolved;
    private Map<String, Object> vieparams;
    private Map<String, List<String>> reqparams;

    private String prevExecutorId;

    private String lastStateKey;
    private String lastViewId;
    private Object lastViewState;

    private String prevStateKey;
    private String prevViewId;
    private Object prevViewState;

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
    public void invoke(final InvokeContext ictx, String source, final Map<String, Object> params) throws InvokerException {
        FacesContext context = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        boolean oldProcessingEvents = context.isProcessingEvents();
        try {
            context.setProcessingEvents(false);
            ExternalContext ec = context.getExternalContext();
            ViewHandler vh = context.getApplication().getViewHandler();

            if (source.equals("@this")) {
                String machineViewId = (String) executor
                        .getStateMachine().getMetadata().get("faces-viewid");

                source = machineViewId;
            }

            NavigationCase navCase = findNavigationCase(context, source);
            viewId = source;
            try {
                viewId = navCase.getToViewId(context);
            } catch (NullPointerException th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source));
            } catch (Throwable th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source), th);
            }
            viewId = vh.deriveLogicalViewId(context, viewId);
            prevExecutorId = handler.getExecutorViewRootId(context);

            String oldInvokeViewId = (String) executor.getRootContext().get(EXECUTOR_CONTEXT_VIEW_PATH);
            if (oldInvokeViewId != null) {
                throw new InvokerException(String.format(
                        "can not start invoke new view: \"%s\", in other view: \"%s\".",
                        viewId, oldInvokeViewId));
            }

            Map<String, Object> options = new HashMap();

            reqparams = new HashMap<>();
            Map<String, List<String>> navparams = navCase.getParameters();
            if (navparams != null) {
                reqparams.putAll(navparams);
            }

            vieparams = new HashMap();
            for (String key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (value instanceof String) {
                    if (containsOnlyDigits((String) value)) {
                        value = NumberFormat.getInstance().parse((String) value);
                    } else if ("true".equals(value)) {
                        value = true;
                    } else if ("false".equals(value)) {
                        value = false;
                    }
                }
                if (skey.startsWith("@redirect.param.")) {
                    skey = skey.substring(16);
                    reqparams.put(skey, Collections.singletonList(value.toString()));
                } else if (skey.startsWith("@view.param.")) {
                    skey = skey.substring(12);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    vieparams.put(skey, value.toString());
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
            boolean ajaxredirect = StateFlowParams.isDefaultAjaxRedirect();
            boolean useflash = StateFlowParams.isDefaultUseFlashInRedirect();

            if (options.containsKey("redirect")) {
                Object val = options.get("redirect");
                if (val instanceof String) {
                    redirect = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    redirect = (Boolean) val;
                }
            }

            if (options.containsKey("flash")) {
                Object val = options.get("flash");
                if (val instanceof String) {
                    useflash = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    useflash = (Boolean) val;
                }
            }

            Context rctx = executor.getRootContext();

            if (!transientState) {
                lastStateKey = "__@@Invoke:last:" + invokeId + ":";
            }

            if (lastStateKey != null) {
                lastStateKey = "__@@Invoke:last:" + invokeId + ":";
                if (rctx.hasLocal(lastStateKey + "ViewState")) {
                    lastViewState = rctx.get(lastStateKey + "ViewState");
                    lastViewId = (String) rctx.get(lastStateKey + "ViewId");
                    if (lastViewId != null) {
                        viewId = lastViewId;
                    }
                }
            } else {
                lastViewId = null;
                lastViewState = null;
            }

            handler.setExecutorViewRootId(context, executor.getRootId());
            executor.getRootContext().setLocal(EXECUTOR_CONTEXT_VIEW_PATH, viewId);

            UIViewRoot currentViewRoot = context.getViewRoot();
            if (currentViewRoot != null) {
                String currentViewId = currentViewRoot.getViewId();
                if (currentViewId.equals(viewId)) {
                    PartialViewContext pvc = context.getPartialViewContext();
                    if ((pvc != null && pvc.isAjaxRequest())) {
                        pvc.setRenderAll(true);
                    }
                    return;
                }
            }

            prevStateKey = "__@@Invoke:prev:" + invokeId + ":";
            prevViewId = currentViewRoot.getViewId();

            RenderKit renderKit = context.getRenderKit();
            ResponseStateManager rsm = renderKit.getResponseStateManager();
            prevViewState = rsm.getState(context, prevViewId);

            rctx.setLocal(prevStateKey + "ViewState", prevViewState);
            rctx.setLocal(prevStateKey + "ViewId", prevViewId);

            PartialViewContext pvc = context.getPartialViewContext();
            if ((redirect || (pvc != null && ajaxredirect && pvc.isAjaxRequest()))) {
                Context fctx = handler.getFlowContext(context);
                if (lastViewState != null) {
                    fctx.setLocal(FACES_VIEW_STATE, lastViewState);
                }

                Application application = context.getApplication();
                ViewHandler viewHandler = application.getViewHandler();
                String url = viewHandler.getRedirectURL(context, viewId, reqparams, false);
                clearViewMapIfNecessary(context.getViewRoot(), viewId);

                if (useflash) {
                    Flash flash = ec.getFlash();
                    flash.setKeepMessages(true);
                    flash.setRedirect(true);
                }

                updateRenderTargets(context, viewId);
                ec.redirect(url);

                context.responseComplete();
            } else {
                UIViewRoot viewRoot;
                if (lastViewState != null) {
                    context.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                    viewRoot = vh.restoreView(context, viewId);
                    context.setViewRoot(viewRoot);
                    context.setProcessingEvents(true);
                    vh.initView(context);
                } else {
                    viewRoot = null;
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(context, viewId);
                    }

                    if (viewRoot != null) {
                        if (!ViewMetadata.hasMetadata(viewRoot)) {
                            context.renderResponse();
                        }
                    }

                    if (vdl == null || metadata == null) {
                        context.renderResponse();
                    }

                    if (viewRoot == null) {
                        viewRoot = vh.createView(context, viewId);
                    }

                    viewRoot.setViewId(viewId);
                }
                context.setViewRoot(viewRoot);
            }

            if ((pvc != null && pvc.isAjaxRequest())) {
                pvc.setRenderAll(true);
            }
            context.renderResponse();

        } catch (FacesException | InvokerException ex) {
            throw ex;
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex.getMessage(), ex);
        } finally {
            context.setProcessingEvents(oldProcessingEvents);
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

    private boolean containsOnlyAlphaNumeric(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isLetterOrDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsOnlyDigits(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
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

    /**
     *
     * @param context
     * @param outcome
     * @return
     */
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
        //filter all multicast call event from started viewId by this invoker
        if (event.getType() == TriggerEvent.CALL_EVENT) {
            //fix view, if current view not equal request view, request redirect to current view
            if (event.getName().startsWith(BEFORE_APPLY_REQUEST_VALUES)) {
                if (context.getViewRoot() != null) {
                    String currentViewId = context.getViewRoot().getViewId();
                    if (!currentViewId.equals(viewId)) {
                        try {
                            ExternalContext ec = context.getExternalContext();
                            Application application = context.getApplication();
                            ViewHandler viewHandler = application.getViewHandler();
                            String url = viewHandler.getRedirectURL(context, viewId, reqparams, true);
                            clearViewMapIfNecessary(context.getViewRoot(), viewId);
                            updateRenderTargets(context, viewId);
                            ec.redirect(url);
                            if (lastViewState != null) {
                                StateFlowHandler handler = StateFlowHandler.getInstance();
                                Context fctx = handler.getFlowContext(context);
                                fctx.setLocal(FACES_VIEW_STATE, lastViewState);
                            }
                            context.responseComplete();
                        } catch (IOException ex) {
                            throw new InvokerException(ex);
                        }
                    }
                }
            }

            if (viewId.equals(event.getSendId())) {
                UIViewRoot viewRoot = context.getViewRoot();

                if (event.getName().startsWith(AFTER_PHASE_EVENT_PREFIX)) {
                    if (viewRoot != null) {
                        try {
                            StateChartExecuteContext viewContext = new StateChartExecuteContext(
                                    invokeId, executor, ictx.getContext());

                            StateFlowHandler handler = StateFlowHandler.getInstance();
                            handler.initViewContext(context, viewId, viewContext);
                        } catch (ModelException ex) {
                            throw new InvokerException(ex);
                        }
                    }

                    if (!resolved && !context.getResponseComplete()) {
                        applyParams(context, viewRoot, vieparams);
                        resolved = true;
                    }
                }

                if (event.getName().startsWith(AFTER_RENDER_VIEW)) {
                    if (viewRoot != null) {
                        lastViewId = viewRoot.getViewId();
                        RenderKit renderKit = context.getRenderKit();
                        ResponseStateManager rsm = renderKit.getResponseStateManager();
                        lastViewState = rsm.getState(context, lastViewId);
                    }
                }

                if (event.getName().startsWith(VIEW_EVENT_PREFIX)) {
                    ExternalContext ec = context.getExternalContext();

                    Map<String, String> params = new HashMap<>();
                    params.putAll(ec.getRequestParameterMap());

                    String outcome = event.getName().substring(VIEW_EVENT_PREFIX.length());
                    EventBuilder evb = new EventBuilder("view." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);

                    evb.data(params);
                    evb.sendId(invokeId);
                    executor.addEvent(evb.build());
                }
            }
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = context.getViewRoot();

        Context rctx = executor.getRootContext();

        if (viewRoot != null) {
            if (lastStateKey != null) {
                lastViewId = viewRoot.getViewId();
                RenderKit renderKit = context.getRenderKit();
                ResponseStateManager rsm = renderKit.getResponseStateManager();
                lastViewState = rsm.getState(context, lastViewId);

                rctx.setLocal(lastStateKey + "ViewState", lastViewState);
                rctx.setLocal(lastStateKey + "ViewId", lastViewId);
            }
        }

//        if (prevViewState != null) {
//            ViewHandler vh = context.getApplication().getViewHandler();
//
//            context.getAttributes().put(FACES_VIEW_STATE, prevViewState);
//            viewRoot = vh.restoreView(context, prevViewId);
//            context.setViewRoot(viewRoot);
//            context.setProcessingEvents(true);
//            vh.initView(context);
//        }
//
//        PartialViewContext pvc = context.getPartialViewContext();
//        if ((pvc != null && pvc.isAjaxRequest())) {
//            pvc.setRenderAll(true);
//        }
        StateFlowHandler handler = StateFlowHandler.getInstance();
        handler.setExecutorViewRootId(context, prevExecutorId);

        rctx.removeLocal(EXECUTOR_CONTEXT_VIEW_PATH);

        context.renderResponse();
    }
}
