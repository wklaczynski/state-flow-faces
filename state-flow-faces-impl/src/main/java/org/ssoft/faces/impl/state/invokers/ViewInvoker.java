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
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.application.NavigationCase;
import jakarta.faces.application.StateManager;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewParameter;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.context.PartialViewContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.ResponseStateManager;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewMetadata;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.ssoft.faces.impl.state.StateFlowParams;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import static javax.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static javax.faces.state.StateFlow.VIEW_EVENT_PREFIX;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.model.ModelException;
import static javax.faces.state.StateFlow.EXECUTOR_CONTEXT_VIEW_PATH;
import static javax.faces.state.StateFlow.FACES_CHART_EXECUTOR_VIEW_ID;
import static javax.faces.state.StateFlow.VIEW_RESTORED_HINT;
import javax.faces.state.execute.ExecuteContextManager;
import static javax.faces.state.StateFlow.FACES_VIEW_ROOT_EXECUTOR_ID;

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
    private String path;
    private boolean resolved;
    private Map<String, Object> vieparams;
    private Map<String, List<String>> reqparams = new LinkedHashMap<>();
    ;

    private String lastStateKey;
    private String lastViewId;
    private Object lastViewState;

    private String prevRootExecutorId;
    private String prevViewExecutorId;
    private String prevStateKey;
    private String prevViewId;
    private String prevcId;
    private Object prevViewState;
    private boolean usewindow;
    private boolean useflash;

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
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        boolean oldProcessingEvents = fc.isProcessingEvents();
        try {
            fc.setProcessingEvents(false);
            ExternalContext ec = fc.getExternalContext();
            ViewHandler vh = fc.getApplication().getViewHandler();

            if (source.equals("@this")) {
                String machineViewId = (String) executor
                        .getStateMachine().getMetadata().get("faces-viewid");

                source = machineViewId;
            }

            NavigationCase navCase = findNavigationCase(fc, source);
            viewId = source;
            try {
                viewId = navCase.getToViewId(fc);
            } catch (NullPointerException th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source));
            } catch (Throwable th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source), th);
            }
            viewId = vh.deriveLogicalViewId(fc, viewId);
            prevRootExecutorId = handler.getViewExecutorId(fc);

            String oldInvokeViewId = (String) executor.getRootContext().get(EXECUTOR_CONTEXT_VIEW_PATH);
            if (oldInvokeViewId != null) {
                throw new InvokerException(String.format(
                        "can not start invoke new view: \"%s\", in other view: \"%s\".",
                        viewId, oldInvokeViewId));
            }

            Map<String, Object> options = new HashMap();

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
            useflash = StateFlowParams.isDefaultUseFlashInRedirect();

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

            if (ajaxredirect && fc.getAttributes().containsKey(VIEW_RESTORED_HINT)) {
                ajaxredirect = (boolean) fc.getAttributes().get(VIEW_RESTORED_HINT);
            }

            path = executor.getId() + ":" + viewId;
            executor.getRootContext().setLocal(EXECUTOR_CONTEXT_VIEW_PATH, viewId);

            ExecuteContext viewContext = new ExecuteContext(
                    path, invokeId, executor, ictx.getContext());

            ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
            manager.initExecuteContext(fc, path, viewContext);

            prevStateKey = "__@@Invoke:prev:" + invokeId + ":";

            UIViewRoot currentViewRoot = fc.getViewRoot();
            if (currentViewRoot != null) {
                prevViewId = currentViewRoot.getViewId();

                if (ViewMetadata.hasMetadata(currentViewRoot)) {
                    VisitContext vc = VisitContext.createVisitContext(fc);
                    currentViewRoot.visitTree(vc, (VisitContext ivc, UIComponent target) -> {
                        Map<String, String[]> values = fc.getExternalContext().getRequestParameterValuesMap();

                        if (target instanceof UIViewParameter) {
                            UIViewParameter parametr = (UIViewParameter) target;
                            String name = parametr.getName();

                            String[] value = values.get(name);
                            if (value != null) {
                                reqparams.put(name, Arrays.asList(value));
                            }

                            if (params.containsKey(name)) {
                                parametr.setValue(params.get(name));
                            }
                        }
                        return VisitResult.ACCEPT;
                    });
                }

                String executePath = prevRootExecutorId + ":" + prevViewId;
                ExecuteContext prevExecuteContext = manager.findExecuteContextByPath(fc, executePath);

                if (prevExecuteContext != null) {
                    prevViewExecutorId = prevExecuteContext.getExecutor().getId();
                }

                UIComponent cc = UIComponent.getCurrentComponent(fc);
                if (cc != null) {
                    prevcId = cc.getClientId();
                }

                StateManager sm = fc.getApplication().getStateManager();
                prevViewState = sm.saveView(fc);

                rctx.setLocal(prevStateKey + "ViewState", prevViewState);
                rctx.setLocal(prevStateKey + "ViewId", prevViewId);
            }

            usewindow = false;
            if (StateFlowParams.isUseWindowMode()) {
                ClientWindow cl = ec.getClientWindow();
                if (cl != null) {
                    usewindow = true;
                }
            }

            if (fc.getResponseComplete()) {
                return;
            }

            if (redirect && fc.getAttributes().containsKey(VIEW_RESTORED_HINT)) {
                redirect = (boolean) fc.getAttributes().get(VIEW_RESTORED_HINT);
                if (!redirect) {
                    if (currentViewRoot != null) {
                        String currentViewId = currentViewRoot.getViewId();
                        if (!currentViewId.equals(viewId)) {
                            redirect = true;
                        }
                    }
                }
            }

            PartialViewContext pvc = fc.getPartialViewContext();
            if ((redirect || (pvc != null && ajaxredirect && pvc.isAjaxRequest()))) {

                Context fctx = handler.getFlowContext(fc, executor.getId());
                fctx.setLocal(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                if (lastViewState != null) {
                    fctx.setLocal(FACES_VIEW_STATE, lastViewState);
                }

                Map<String, List<String>> rparams = new HashMap<>();
                rparams.putAll(reqparams);
                if (!usewindow) {
                    if (useflash) {
                        Flash flash = ec.getFlash();
                        flash.put("exid", executor.getId());
                        flash.setKeepMessages(true);
                        flash.setRedirect(true);
                    } else {
                        rparams.put("exid", Arrays.asList(executor.getId()));
                    }
                }

                Application application = fc.getApplication();
                ViewHandler viewHandler = application.getViewHandler();
                String url = viewHandler.getRedirectURL(fc, viewId, rparams, false);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);

                updateRenderTargets(fc, viewId);
                ec.redirect(url);

                fc.responseComplete();
            } else {
                if (currentViewRoot != null) {
                    String currentViewId = currentViewRoot.getViewId();
                    if (currentViewId.equals(viewId)) {
                        if ((pvc != null && (pvc.isAjaxRequest() || pvc.isPartialRequest()))) {
                            pvc.setRenderAll(true);
                        }
                        return;
                    }
                }

                UIViewRoot viewRoot;
                if (lastViewState != null) {
                    fc.getAttributes().put(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                    fc.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                    viewRoot = vh.restoreView(fc, viewId);
                    fc.setViewRoot(viewRoot);
                    fc.setProcessingEvents(true);
                    vh.initView(fc);
                } else {
                    fc.getAttributes().put(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                    viewRoot = null;
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(fc, viewId);
                    }

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
            }

            if ((pvc != null && pvc.isAjaxRequest())) {
                pvc.setRenderAll(true);
            }
            fc.renderResponse();

        } catch (FacesException | InvokerException ex) {
            throw ex;
        } catch (IOException | ParseException | ModelException ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex.getMessage(), ex);
        } finally {
            fc.setProcessingEvents(oldProcessingEvents);
        }
    }

    public void changeUrl(final InvokeContext ictx, String source, final Map<String, Object> params) {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        boolean oldProcessingEvents = fc.isProcessingEvents();
        try {
            fc.setProcessingEvents(false);
            ExternalContext ec = fc.getExternalContext();
            ViewHandler vh = fc.getApplication().getViewHandler();

            if (source.equals("@this")) {
                String machineViewId = (String) executor
                        .getStateMachine().getMetadata().get("faces-viewid");

                source = machineViewId;
            }

            NavigationCase navCase = findNavigationCase(fc, source);
            viewId = source;
            try {
                viewId = navCase.getToViewId(fc);
            } catch (NullPointerException th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source));
            } catch (Throwable th) {
                throw new IOException(String.format("invoke source \"%s\" not found", source), th);
            }
            viewId = vh.deriveLogicalViewId(fc, viewId);
            prevRootExecutorId = handler.getViewExecutorId(fc);

            String oldInvokeViewId = (String) executor.getRootContext().get(EXECUTOR_CONTEXT_VIEW_PATH);
            if (oldInvokeViewId == null) {
                throw new InvokerException(String.format(
                        "Can not change invoke to view: \"%s\", when invoke view can not started.",
                        viewId));
            }

            Map<String, Object> options = new HashMap();

            reqparams = new LinkedHashMap<>();
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
                    if (ec.getRequestParameterMap().containsKey(skey)) {
                        value = ec.getRequestParameterMap().get(skey);
                    }
                    reqparams.put(skey, Collections.singletonList(value.toString()));
                } else if (skey.startsWith("@view.param.")) {
                    skey = skey.substring(12);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    vieparams.put(skey, value.toString());
                }
            }

            boolean redirect = StateFlowParams.isDefaultViewRedirect();
            boolean ajaxredirect = StateFlowParams.isDefaultAjaxRedirect();
            useflash = StateFlowParams.isDefaultUseFlashInRedirect();

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

            if (ajaxredirect && fc.getAttributes().containsKey(VIEW_RESTORED_HINT)) {
                ajaxredirect = (boolean) fc.getAttributes().get(VIEW_RESTORED_HINT);
            }

            path = executor.getId() + ":" + viewId;
            executor.getRootContext().setLocal(EXECUTOR_CONTEXT_VIEW_PATH, viewId);
            executor.getRootContext().setLocal(FACES_CHART_EXECUTOR_VIEW_ID, viewId);

            ExecuteContext viewContext = new ExecuteContext(
                    path, invokeId, executor, ictx.getContext());

            ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
            manager.initExecuteContext(fc, path, viewContext);

            prevStateKey = "__@@Invoke:prev:" + invokeId + ":";

            UIViewRoot currentViewRoot = fc.getViewRoot();
            if (currentViewRoot != null) {
                prevViewId = currentViewRoot.getViewId();
                
                if (ViewMetadata.hasMetadata(currentViewRoot)) {
                    VisitContext vc = VisitContext.createVisitContext(fc);
                    currentViewRoot.visitTree(vc, (VisitContext ivc, UIComponent target) -> {
                        Map<String, String[]> values = fc.getExternalContext().getRequestParameterValuesMap();

                        if (target instanceof UIViewParameter) {
                            UIViewParameter parametr = (UIViewParameter) target;
                            String name = parametr.getName();

                            String[] value = values.get(name);
                            if (value != null) {
                                reqparams.put(name, Arrays.asList(value));
                            }

                            if (params.containsKey(name)) {
                                parametr.setValue(params.get(name));
                            }
                        }
                        return VisitResult.ACCEPT;
                    });
                }

                String executePath = prevRootExecutorId + ":" + prevViewId;
                ExecuteContext prevExecuteContext = manager.findExecuteContextByPath(fc, executePath);

                if (prevExecuteContext != null) {
                    prevViewExecutorId = prevExecuteContext.getExecutor().getId();
                }

                UIComponent cc = UIComponent.getCurrentComponent(fc);
                if (cc != null) {
                    prevcId = cc.getClientId();
                }

                StateManager sm = fc.getApplication().getStateManager();
                prevViewState = sm.saveView(fc);

                rctx.setLocal(prevStateKey + "ViewState", prevViewState);
                rctx.setLocal(prevStateKey + "ViewId", prevViewId);
            }

            usewindow = false;
            if (StateFlowParams.isUseWindowMode()) {
                ClientWindow cl = ec.getClientWindow();
                if (cl != null) {
                    usewindow = true;
                }
            }

            if (fc.getResponseComplete()) {
                return;
            }

            if (redirect && fc.getAttributes().containsKey(VIEW_RESTORED_HINT)) {
                redirect = (boolean) fc.getAttributes().get(VIEW_RESTORED_HINT);
                if (!redirect) {
                    if (currentViewRoot != null) {
                        String currentViewId = currentViewRoot.getViewId();
                        if (!currentViewId.equals(viewId)) {
                            redirect = true;
                        }
                    }
                }
            }

            PartialViewContext pvc = fc.getPartialViewContext();
            if ((redirect || (pvc != null && ajaxredirect && pvc.isAjaxRequest()))) {

                Context fctx = handler.getFlowContext(fc, executor.getId());
                fctx.setLocal(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                if (lastViewState != null) {
                    fctx.setLocal(FACES_VIEW_STATE, lastViewState);
                }

                Map<String, List<String>> rparams = new HashMap<>();
                rparams.putAll(reqparams);
                if (!usewindow) {
                    if (useflash) {
                        Flash flash = ec.getFlash();
                        flash.put("exid", executor.getId());
                        flash.setKeepMessages(true);
                        flash.setRedirect(true);
                    } else {
                        rparams.put("exid", Arrays.asList(executor.getId()));
                    }
                }

                Application application = fc.getApplication();
                ViewHandler viewHandler = application.getViewHandler();
                String url = viewHandler.getRedirectURL(fc, viewId, rparams, false);
                clearViewMapIfNecessary(fc.getViewRoot(), viewId);

                updateRenderTargets(fc, viewId);
                ec.redirect(url);

                fc.responseComplete();
            } else {
                if (currentViewRoot != null) {
                    String currentViewId = currentViewRoot.getViewId();
                    if (currentViewId.equals(viewId)) {
                        if ((pvc != null && (pvc.isAjaxRequest() || pvc.isPartialRequest()))) {
                            pvc.setRenderAll(true);
                        }
                        return;
                    }
                }

                UIViewRoot viewRoot;
                if (lastViewState != null) {
                    fc.getAttributes().put(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                    fc.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                    viewRoot = vh.restoreView(fc, viewId);
                    fc.setViewRoot(viewRoot);
                    fc.setProcessingEvents(true);
                    vh.initView(fc);
                } else {
                    fc.getAttributes().put(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getId());
                    viewRoot = null;
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(fc, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(fc, viewId);
                    }

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
            }

            if ((pvc != null && pvc.isAjaxRequest())) {
                pvc.setRenderAll(true);
            }
            fc.renderResponse();

        } catch (FacesException ex) {
            throw ex;
        } catch (IOException | ParseException | InvokerException | ModelException ex) {
            throw new FacesException(ex);
        } finally {
            fc.setProcessingEvents(oldProcessingEvents);
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
//            if (event.getName().startsWith(BEFORE_APPLY_REQUEST_VALUES)) {
//                if (context.getViewRoot() != null) {
//                    String currentViewId = context.getViewRoot().getViewId();
//                    if (!currentViewId.equals(viewId)) {
//                        try {
//                            ExternalContext ec = context.getExternalContext();
//                            Application application = context.getApplication();
//                            ViewHandler viewHandler = application.getViewHandler();
//                            String url = viewHandler.getRedirectURL(context, viewId, reqparams, true);
//                            clearViewMapIfNecessary(context.getViewRoot(), viewId);
//                            updateRenderTargets(context, viewId);
//
//                            if (!usewindow) {
//                                if (useflash) {
//                                    Flash flash = ec.getFlash();
//                                    flash.put("exid", executor.getRootId());
//
//                                    flash.setKeepMessages(true);
//                                    flash.setRedirect(true);
//                                } else {
//                                    reqparams.put("exid", Arrays.asList(executor.getRootId()));
//                                }
//                            }
//
//                            ec.redirect(url);
//                            StateFlowHandler handler = StateFlowHandler.getInstance();
//                            Context fctx = handler.getFlowContext(context, executor.getRootId());
//                            fctx.setLocal(FACES_VIEW_ROOT_EXECUTOR_ID, executor.getRootId());
//                            if (lastViewState != null) {
//                                fctx.setLocal(FACES_VIEW_STATE, lastViewState);
//                            }
//                            context.responseComplete();
//                        } catch (IOException ex) {
//                            throw new InvokerException(ex);
//                        }
//                    }
//                }
//            }

            try {
                ExecuteContext viewContext = new ExecuteContext(
                        path, invokeId, executor, ictx.getContext());

                ExecuteContextManager manager = ExecuteContextManager.getManager(context);
                manager.initExecuteContext(context, path, viewContext);
            } catch (ModelException ex) {
                throw new InvokerException(ex);
            }

            if (viewId.equals(event.getSendId())) {
                UIViewRoot viewRoot = context.getViewRoot();

                if (!resolved && !context.getResponseComplete()) {
                    applyParams(context, viewRoot, vieparams);
                    resolved = true;
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
                    String outcome = event.getName().substring(VIEW_EVENT_PREFIX.length());
                    if (!executor.hasPendingEvents("view." + outcome)) {
                        ExternalContext ec = context.getExternalContext();

                        Map<String, String> params = new HashMap<>();
                        params.putAll(ec.getRequestParameterMap());

                        EventBuilder evb = new EventBuilder("view." + outcome, TriggerEvent.SIGNAL_EVENT);

                        evb.data(params);
                        evb.sendId(invokeId);
                        executor.addEvent(evb.build());
                    }
                }
            }
        }

        if (event.getType() == TriggerEvent.SIGNAL_EVENT
                && event.getInvokeId() != null
                && event.getInvokeId().endsWith(invokeId)) {

            if (event.getName().startsWith("view.change.url")) {
                Map<String, Object> params = new HashMap<>();
                Object data = event.getData();
                if (data instanceof Map) {
                    params.putAll((Map) data);
                }

                String src = (String) params.get("src");
                changeUrl(ictx, src, params);
            }
        }

    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();

        Context rctx = executor.getRootContext();

        if (viewRoot != null) {
            if (lastStateKey != null) {
                lastViewId = viewRoot.getViewId();
                StateManager sm = fc.getApplication().getStateManager();
                lastViewState = sm.saveView(fc);

                rctx.setLocal(lastStateKey + "ViewState", lastViewState);
                rctx.setLocal(lastStateKey + "ViewId", lastViewId);
            }
        }

        StateFlowHandler handler = StateFlowHandler.getInstance();

        //handler.setExecutorViewRootId(context, prevRootExecutorId);
        PartialViewContext pvc = fc.getPartialViewContext();
        if ((pvc != null && pvc.isAjaxRequest())) {
            pvc.setRenderAll(true);
        }

        //restorPrevView();
        rctx.removeLocal(EXECUTOR_CONTEXT_VIEW_PATH);

        fc.renderResponse();
    }
}
