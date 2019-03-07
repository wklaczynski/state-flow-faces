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
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.apache.common.faces.impl.state.StateFlowParams;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static org.apache.common.faces.state.StateFlow.AFTER_RESTORE_VIEW;
import static org.apache.common.faces.state.StateFlow.CURRENT_INVOKED_VIEW_ID;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import static org.apache.common.faces.state.StateFlow.VIEW_INVOKE_CONTEXT;
import org.apache.common.faces.state.StateFlowViewContext;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.InvokeContext;
import org.apache.common.faces.state.component.UIStateChartController;
import org.apache.common.faces.state.component.UIStateChartDefinition;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.model.ModelException;
import org.apache.common.scxml.model.SCXML;

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
    private String controllerId;
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
        ExternalContext ec = context.getExternalContext();
        try {
            Context sctx = executor.getRootContext();
            controllerId = (String) sctx.get(UIStateChartController.COMPONENT_ID);
            viewId = (String) sctx.get(UIStateChartController.VIEW_ID);
            path = viewId + "!" + controllerId;
            

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
                    skey = skey.substring(12);
                    options.put(skey, value.toString());
                } else if (skey.startsWith("@redirect.param.")) {
                    skey = skey.substring(16);
                    reqparams.put(skey, Collections.singletonList(value.toString()));
                } else if (skey.startsWith("@view.param.")) {
                    skey = skey.substring(12);
                    options.put(skey, value.toString());
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

                    context.getAttributes().put(
                            VIEW_INVOKE_CONTEXT.get(path), viewContext);

                    return;
                }
            }

            ViewHandler vh = context.getApplication().getViewHandler();
            PartialViewContext pvc = context.getPartialViewContext();
            if (redirect || (pvc != null && pvc.isAjaxRequest())) {
                Flash flash = ec.getFlash();
                flash.setKeepMessages(true);
                Context rootContext = executor.getRootContext();
                if (viewState != null) {
                    rootContext.setLocal(FACES_VIEW_STATE, viewState);
                }
                rootContext.setLocal(RENDER_FACET_SRC, viewState);

                Application application = context.getApplication();
                ViewHandler viewHandler = application.getViewHandler();
                String url = viewHandler.getRedirectURL(context, viewId, reqparams, false);
                clearViewMapIfNecessary(context.getViewRoot(), viewId);
                flash.setRedirect(true);
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
                    SCXML stateChart = null;
                    viewRoot = null;
                    ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, viewId);
                    ViewMetadata metadata = null;
                    if (vdl != null) {
                        metadata = vdl.getViewMetadata(context, viewId);

                        if (metadata != null) {
                            viewRoot = metadata.createMetadataView(context);
                            UIComponent facet = viewRoot.getFacet(STATECHART_FACET_NAME);
                            if (facet != null) {
                                UIStateChartDefinition uichart = (UIStateChartDefinition) facet.findComponent("main");
                                if (uichart != null) {
                                    stateChart = uichart.getStateChart();
                                }
                            }
                        }
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
                setRenderFacet(context, viewRoot, source);
                context.renderResponse();
            }

            StateFlowViewContext viewContext = new StateFlowViewContext(
                    invokeId, executor, ictx.getContext());

            context.getAttributes().put(
                    VIEW_INVOKE_CONTEXT.get(path), viewContext);

            executor.getRootContext().setLocal(CURRENT_INVOKED_VIEW_ID, viewId);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex);
        }
    }

    private void setRenderFacet(FacesContext context, UIViewRoot viewRoot, String source) throws InvokerException {
        UIStateChartController controller = (UIStateChartController) viewRoot.findComponent(controllerId);
        facetId = getRenderFacetId(context, viewRoot, controller, source);
        controller.setFacetId(facetId);

    }

    public static String getRenderFacetId(FacesContext context, UIViewRoot viewRoot, UIStateChartController controller, String source) throws InvokerException {
        String result = null;
        if (source.startsWith("@controller:")) {
            String name = source.substring(12);
            UIComponent facet = controller.getFacet(name);
            if (facet == null) {
                throwRequiredControllerException(context, name, controller);
            }
            result = facet.getClientId(context);
        } else {
            throwUknowTypeException(context, source, controller);
        }
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

            if (controllerId.equals(event.getSendId())) {

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

            if (viewId.equals(event.getSendId())) {
                UIViewRoot viewRoot = context.getViewRoot();

                if (event.getName().startsWith(AFTER_PHASE_EVENT_PREFIX)) {
                    if (viewRoot != null) {
                        try {
                            StateFlowViewContext viewContext = new StateFlowViewContext(
                                    invokeId, executor, ictx.getContext());
                            context.getAttributes().put(
                                    VIEW_INVOKE_CONTEXT.get(path), viewContext);

                        } catch (ModelException ex) {
                            throw new InvokerException(ex);
                        }
                    }

                }

                if (event.getName().startsWith(AFTER_RESTORE_VIEW)) {
                    if (viewRoot != null) {
                        Context rctx = executor.getRootContext();
                        String source = (String) rctx.get(RENDER_FACET_SRC);
                        setRenderFacet(context, viewRoot, source);
                        rctx.getVars().remove(RENDER_FACET_SRC);
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

//                if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
//                    ExternalContext ec = context.getExternalContext();
//
//                    Map<String, String> params = new HashMap<>();
//                    params.putAll(ec.getRequestParameterMap());
//
//                    String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
//                    EventBuilder evb = new EventBuilder("view.action." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);
//
//                    evb.data(params);
//                    evb.sendId(invokeId);
//                    executor.addEvent(evb.build());
//                }
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
        UIComponent current = UIComponent.getCurrentComponent(context);
        UIStateChartController controller = (UIStateChartController) current;
        controller.setFacetId(facetId);

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
        executor.getRootContext().getVars().remove(CURRENT_INVOKED_VIEW_ID, viewId);
        context.renderResponse();

    }

    private static void throwRequiredCompositeException(FacesContext ctx,
            String name,
            UIComponent compositeParent) {

        throw new IllegalStateException(
                "Unable to find facet named '"
                + name
                + "' in parent composite component with id '"
                + compositeParent.getClientId(ctx)
                + '\'');

    }

    private static void throwRequiredControllerException(FacesContext ctx,
            String name,
            UIComponent compositeParent) {

        throw new IllegalStateException(
                "Unable to find facet named '"
                + name
                + "' in controller component with id '"
                + compositeParent.getClientId(ctx)
                + '\'');

    }

    private static void throwRequiredThisException(FacesContext ctx,
            String name,
            UIComponent parent) throws InvokerException {

        throw new IllegalStateException(
                "Unable to find facet named '"
                + name
                + "' in component with id '"
                + parent.getClientId(ctx)
                + '\'');

    }

    private static void throwRequiredInRootException(FacesContext ctx,
            String name,
            UIComponent root) throws InvokerException {

        throw new IllegalStateException(
                "Unable to find facet named '"
                + name
                + "' in view component with id '"
                + root.getClientId(ctx)
                + '\'');

    }

    private static void throwUknowTypeException(FacesContext ctx,
            String name,
            UIComponent root) throws InvokerException {

        throw new IllegalStateException(
                "Unable to find facet name '"
                + name
                + "' type mus start with @controller: before <facet name> in controller component with id '"
                + root.getClientId(ctx)
                + '\'');

    }

}
