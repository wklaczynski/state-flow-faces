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

import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
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
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import org.apache.common.faces.impl.state.StateFlowParams;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static org.apache.common.faces.state.StateFlow.AFTER_RESTORE_VIEW;
import static org.apache.common.faces.state.StateFlow.CURRENT_INVOKED_VIEW_ID;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_CONTROLLER;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_FACET;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_VIEW_ID;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.PORTLET_CONTROLLER_TYPE;
import static org.apache.common.faces.state.StateFlow.VIEW_CONTROLLER_TYPE;
import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.SCXMLIOProcessor;
import org.apache.common.faces.state.scxml.TriggerEvent;
import org.apache.common.faces.state.scxml.invoke.Invoker;
import org.apache.common.faces.state.scxml.invoke.InvokerException;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.StateFlowViewContext;
import org.apache.common.faces.state.scxml.EventBuilder;
import org.apache.common.faces.state.scxml.InvokeContext;
import org.apache.common.faces.state.component.UIStateChartController;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.model.ModelException;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document may not
 * contain external namespace elements, further invokes etc.
 */
public class FacetInvoker implements Invoker, Serializable {

    public static final String RENDER_FACET_SRC = FacetInvoker.class.getName() + ":RENDER_FACET_SRC";

    private final static Logger logger = Logger.getLogger(FacetInvoker.class.getName());

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

    private String facetId;
    private Map<String, Object> facetparams;
    private Map<String, List<String>> reqparams;
    private String viewId;
    private String stateKey;
    private String lastViewId;
    private Object viewState;
    private String path;

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
        ExternalContext ec = context.getExternalContext();
        try {
            Context sctx = executor.getRootContext();
            viewId = (String) sctx.get(FACES_CHART_VIEW_ID);
            path = viewId + "!" + executor.getId();

            reqparams = new HashMap<>();
            Map<String, Object> options = new HashMap();
            facetparams = new HashMap();
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
                if (skey.startsWith("@facet.param.")) {
                    skey = skey.substring(13);
                    options.put(skey, value.toString());
                } else if (skey.startsWith("@redirect.param.")) {
                    skey = skey.substring(16);
                    reqparams.put(skey, Collections.singletonList(value.toString()));
                } else if (value != null) {
                    facetparams.put(skey, value.toString());
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

            boolean redirect = false;
            boolean ajaxredirect = StateFlowParams.isDefaultAjaxRedirect();
            boolean useflash = StateFlowParams.isDefaultUseFlashInRedirect();

            if (options.containsKey("redirect")) {
                Object val = options.get("redirect");
                if (val instanceof String) {
                    redirect = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    redirect = (Boolean) val;
                }
                ajaxredirect = redirect;
            }

            if (options.containsKey("flash")) {
                Object val = options.get("flash");
                if (val instanceof String) {
                    useflash = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    useflash = (Boolean) val;
                }
            }

            if (!transientState) {
                stateKey = "__@@Invoke:" + invokeId + ":";
            }

            if (stateKey != null) {
                stateKey = "__@@Invoke:" + invokeId + ":";

                Context stateContext = executor.getGlobalContext();
                viewState = stateContext.get(stateKey + "ViewState");
                lastViewId = (String) stateContext.get(stateKey + "LastViewId");
                if (lastViewId != null) {
                    viewId = lastViewId;
                }
            } else {
                lastViewId = null;
                viewState = null;
            }

            UIViewRoot currentViewRoot = context.getViewRoot();
            if (currentViewRoot != null) {
                String currentViewId = currentViewRoot.getViewId();
                if (currentViewId.equals(viewId)) {
                    executor.getRootContext().setLocal(CURRENT_INVOKED_VIEW_ID, viewId);
                    setRenderFacet(context, currentViewRoot, source);

                    StateFlowViewContext viewContext = new StateFlowViewContext(
                            invokeId, executor, ictx.getContext());

                    StateFlow.setViewContext(context, path, viewContext);

                    return;
                }
            }

            ViewHandler vh = context.getApplication().getViewHandler();

            boolean ajaxr = StateFlowParams.isDefaultAjaxRedirect();
            PartialViewContext pvc = context.getPartialViewContext();
            if ((redirect || (pvc != null && ajaxr && pvc.isAjaxRequest()))) {
                if (viewState != null) {
                    Context flowContext = handler.getFlowContext(context);
                    flowContext.setLocal(FACES_VIEW_STATE, viewState);
                }
                Context rootContext = executor.getRootContext();
                rootContext.setLocal(RENDER_FACET_SRC, source);

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
                if (viewState != null) {
                    context.getAttributes().put(FACES_VIEW_STATE, viewState);
                    viewRoot = vh.restoreView(context, viewId);
                    context.setViewRoot(viewRoot);
                    context.setProcessingEvents(true);
                    vh.initView(context);
                } else {
                    viewRoot = null;
                    if (viewRoot == null) {
                        viewRoot = vh.createView(context, viewId);
                    }
                    viewRoot.setViewId(viewId);
                }
                context.setViewRoot(viewRoot);
                setRenderFacet(context, viewRoot, source);
                context.renderResponse();
            }

            StateFlowViewContext viewContext = new StateFlowViewContext(
                    invokeId, executor, ictx.getContext());

            StateFlow.setViewContext(context, path, viewContext);

            if ((pvc != null && pvc.isAjaxRequest())) {
                pvc.setRenderAll(true);
            }

            executor.getRootContext().setLocal(CURRENT_INVOKED_VIEW_ID, viewId);
        } catch (FacesException | InvokerException ex) {
            throw ex;
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex.getMessage(), ex);
        }
    }

    private void setRenderFacet(FacesContext context, UIViewRoot viewRoot, String source) throws InvokerException {

        Context sctx = executor.getRootContext();

        String controllerType = (String) sctx.get(FACES_CHART_CONTROLLER);
        if (controllerType == null) {
            controllerType = VIEW_CONTROLLER_TYPE;
        }

        if (source.startsWith("@controller:")) {
            
            if(!controllerType.equals(PORTLET_CONTROLLER_TYPE)) {
                throwRequiredControllerException(context, source);
            }
            
            sctx.setLocal(FACES_CHART_FACET, source);
        } else {
            throwUknowTypeException(context, source);
        }
    }

    public static String getRenderFacetId(FacesContext context, UIViewRoot viewRoot, UIStateChartController controller, String source) throws InvokerException {
        String result = null;
        return result;
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

    private boolean containsOnlyDigits(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
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

            if (viewId.equals(event.getSendId())) {
                UIViewRoot viewRoot = context.getViewRoot();

                if (event.getName().startsWith(AFTER_PHASE_EVENT_PREFIX)) {
                    if (viewRoot != null) {
                        try {
                            StateFlowViewContext viewContext = new StateFlowViewContext(
                                    invokeId, executor, ictx.getContext());

                            StateFlow.setViewContext(context, path, viewContext);
                        } catch (ModelException ex) {
                            throw new InvokerException(ex);
                        }
                    }
                }

                if (event.getName().startsWith(AFTER_RESTORE_VIEW)) {
                    if (viewRoot != null) {
                        Context rctx = executor.getRootContext();
                        String source = (String) rctx.get(RENDER_FACET_SRC);
                        if (source != null) {
                            setRenderFacet(context, viewRoot, source);
                            rctx.getVars().remove(RENDER_FACET_SRC);
                        }
                    }

                }

                if (event.getName().startsWith(AFTER_RENDER_VIEW)) {
                    if (viewRoot != null) {
                        lastViewId = viewRoot.getViewId();
                        RenderKit renderKit = context.getRenderKit();
                        ResponseStateManager rsm = renderKit.getResponseStateManager();
                        viewState = rsm.getState(context, lastViewId);
                    }
                }

                if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
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
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext context = FacesContext.getCurrentInstance();

        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot != null) {
            if (stateKey != null) {
                lastViewId = viewRoot.getViewId();
                RenderKit renderKit = context.getRenderKit();
                ResponseStateManager rsm = renderKit.getResponseStateManager();
                viewState = rsm.getState(context, lastViewId);
                Context storeContext = executor.getGlobalContext();

                storeContext.setLocal(stateKey + "ViewState", viewState);
                storeContext.setLocal(stateKey + "LastViewId", lastViewId);
            }
        }
        
        Context ctx = executor.getRootContext();
        
        ctx.removeLocal(CURRENT_INVOKED_VIEW_ID);
        ctx.removeLocal(FACES_CHART_FACET);
        
        context.renderResponse();

    }

    private static void throwRequiredControllerException(FacesContext ctx, String name) {

        throw new IllegalStateException(
                "can not invoke facet named \"" + name + "\" in not controller execution");

    }

    private static void throwUknowTypeException(FacesContext ctx, String name) throws InvokerException {

        throw new IllegalStateException(
                "unable to find facet name '" + name + "' type mus start with @controller:");

    }

}
