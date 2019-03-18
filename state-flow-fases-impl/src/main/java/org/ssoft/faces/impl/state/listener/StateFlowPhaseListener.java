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
package org.ssoft.faces.impl.state.listener;

import com.sun.faces.context.StateContext;
import com.sun.faces.renderkit.RenderKitUtils;
import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.model.SCXML;
import org.ssoft.faces.impl.state.StateFlowParams;
import static org.ssoft.faces.impl.state.listener.StateFlowControllerListener.getControllerClientIds;
import static javax.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.SKIP_START_STATE_MACHINE_HINT;
import javax.faces.state.task.FacesProcessHolder;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;
import static javax.faces.state.StateFlow.DECODE_DISPATCHER_EVENTS;
import javax.faces.state.component.UIStateChartExecutor;
import static javax.faces.state.StateFlow.VIEWROOT_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.FACES_CHART_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.FACES_CHART_EXECUTOR_VIEW_ID;
import static javax.faces.state.StateFlow.DEFAULT_STATE_MACHINE_NAME;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowPhaseListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();

        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            restoreStateFlow(facesContext);
        }

        UIViewRoot viewRoot = facesContext.getViewRoot();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (viewRoot != null) {

            String name = AFTER_PHASE_EVENT_PREFIX
                    + event.getPhaseId().getName().toLowerCase();

            if (handler.hasViewRoot(facesContext)) {
                SCXMLExecutor executor = handler.getRootExecutor(facesContext);

                EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                        .sendId(facesContext.getViewRoot().getViewId());

                try {
                    executor.triggerEvent(eb.build());
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }

                Context.setCurrentInstance(
                        (Context) facesContext.getELContext().getContext(Context.class));

            }

            ArrayList<String> clientIds = getControllerClientIds(facesContext);
            if (clientIds != null && !clientIds.isEmpty()) {
                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartExecutor) {
                        UIStateChartExecutor controller = (UIStateChartExecutor) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder veb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                .sendId(viewRoot.getViewId());

                        SCXMLExecutor executor = controller.getExecutor();
                        if (executor != null) {
                            try {
                                executor.triggerEvent(veb.build());
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }

                    }
                    return VisitResult.ACCEPT;
                });
            }

            if (handler.isActive(facesContext)) {
                if (event.getPhaseId() == PhaseId.RENDER_RESPONSE || facesContext.getResponseComplete()) {
                    handler.writeState(facesContext);
                }
            }

        }

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext facesContext = event.getFacesContext();

        if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
            UIViewRoot viewRoot = facesContext.getViewRoot();
            StateFlowHandler handler = StateFlowHandler.getInstance();
            if (viewRoot != null) {

                String name = BEFORE_PHASE_EVENT_PREFIX
                        + event.getPhaseId().getName().toLowerCase();

                if (handler.hasViewRoot(facesContext)) {

                    EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = handler.getRootExecutor(facesContext);
                    try {
                        executor.triggerEvent(eb.build());
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }

                    if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                            && !facesContext.getResponseComplete()) {
                        EventDispatcher ed = executor.getEventdispatcher();
                        if (ed instanceof FacesProcessHolder) {
                            try {
                                EventBuilder eeb = new EventBuilder(DECODE_DISPATCHER_EVENTS,
                                        TriggerEvent.CALL_EVENT)
                                        .sendId(viewRoot.getViewId());

                                executor.triggerEvent(eeb.build());
                                ((FacesProcessHolder) ed).processDecodes(facesContext);
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }
                    }

                }

                ArrayList<String> clientIds = getControllerClientIds(facesContext);
                if (clientIds != null && !clientIds.isEmpty()) {
                    Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                    VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                    viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                        if (target instanceof UIStateChartExecutor) {
                            UIStateChartExecutor controller = (UIStateChartExecutor) target;
                            String controllerId = controller.getClientId(facesContext);
                            
                            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                    .sendId(viewRoot.getViewId());

                            SCXMLExecutor executor = controller.getExecutor();
                            if (executor != null) {
                                try {
                                    executor.triggerEvent(eb.build());
                                } catch (ModelException ex) {
                                    throw new FacesException(ex);
                                }

                                if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                                        && !facesContext.getResponseComplete()) {

                                    EventDispatcher ed = executor.getEventdispatcher();
                                    if (ed instanceof FacesProcessHolder) {
                                        try {
                                            EventBuilder eeb = new EventBuilder(
                                                    DECODE_DISPATCHER_EVENTS,
                                                    TriggerEvent.CALL_EVENT)
                                                    .sendId(viewRoot.getViewId());

                                            executor.triggerEvent(eeb.build());
                                            ((FacesProcessHolder) ed).processDecodes(facesContext);
                                        } catch (ModelException ex) {
                                            throw new FacesException(ex);
                                        }
                                    }
                                }
                            }
                        }
                        return VisitResult.ACCEPT;
                    });
                }

            } else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                    && !facesContext.getResponseComplete()) {

            }
        } else {
            StateFlowHandler handler = StateFlowHandler.getInstance();
            if (handler.isActive(facesContext)) {
                Context ctx = handler.getFlowContext(facesContext);
                Object lastViewState = ctx.get(FACES_VIEW_STATE);
                if (lastViewState != null) {
                    try {
                        ctx.removeLocal(FACES_VIEW_STATE);
                        if (!facesContext.isPostback()) {
                            facesContext.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                            String viewId = facesContext.getExternalContext().getRequestPathInfo();
                            UIViewRoot viewRoot = restoreView(facesContext, viewId);
                            facesContext.setViewRoot(viewRoot);
                        }
                    } catch (Throwable ex) {
                        throw new FacesException(ex);
                    }

                }
            }
        }
    }

    public UIViewRoot restoreView(FacesContext context, String viewId) {
        UIViewRoot viewRoot;

        ViewHandler vh = context.getApplication().getViewHandler();

        /*
         * Check if we are stateless.
         */
        ViewHandler outerViewHandler = context.getApplication().getViewHandler();
        String renderKitId = outerViewHandler.calculateRenderKitId(context);
        ResponseStateManager rsm;

        if (StateContext.getStateContext(context).isPartialStateSaving(context, viewId)) {
            try {
                context.setProcessingEvents(false);
                ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, viewId);
                viewRoot = vdl.getViewMetadata(context, viewId).createMetadataView(context);
                context.setViewRoot(viewRoot);
                outerViewHandler = context.getApplication().getViewHandler();
                renderKitId = outerViewHandler.calculateRenderKitId(context);
                rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
                Object[] rawState = (Object[]) rsm.getState(context, viewId);

                if (rawState != null) {
                    Map<String, Object> state = (Map<String, Object>) rawState[1];
                    if (state != null) {
                        String cid = viewRoot.getClientId(context);
                        Object stateObj = state.get(cid);
                        if (stateObj != null) {
                            context.getAttributes().put("com.sun.faces.application.view.restoreViewScopeOnly", true);
                            viewRoot.restoreState(context, stateObj);
                            context.getAttributes().remove("com.sun.faces.application.view.restoreViewScopeOnly");
                        }
                    }
                }

                context.setProcessingEvents(true);
                vdl.buildView(context, viewRoot);
            } catch (IOException ioe) {
                throw new FacesException(ioe);
            }
        }

        StateManager stateManager = context.getApplication().getStateManager();

        UIViewRoot root = stateManager.restoreView(context, viewId, renderKitId);

        ViewHandler viewHandler = context.getApplication().getViewHandler();
        ViewDeclarationLanguage vdl = viewHandler.getViewDeclarationLanguage(context, viewId);
        context.setResourceLibraryContracts(vdl.calculateResourceLibraryContracts(context, viewId));

        StateContext stateCtx = StateContext.getStateContext(context);
        stateCtx.startTrackViewModifications(context, root);

        return root;
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private void restoreStateFlow(FacesContext context) {
        if (context.isPostback()) {
            return;
        }

        Boolean skip = (Boolean) context.getAttributes().get(SKIP_START_STATE_MACHINE_HINT);
        if (skip != null && skip) {
            return;
        }

        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot == null) {
            return;
        }

        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        if (flowHandler.hasViewRoot(context)) {
            return;
        }

        String pname = StateFlowParams.getRequestParamatrChartId();

        String scxmlId = null;

        if (scxmlId == null) {
            scxmlId = context.getExternalContext().getRequestParameterMap().get(pname);
        }
        if (scxmlId == null) {
            scxmlId = DEFAULT_STATE_MACHINE_NAME;
        }

        try {
            SCXML scxml = flowHandler.findStateMachine(context, scxmlId);
            if (scxml != null) {
                startStateMachine(context, scxml);
            }
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }

    }

    /**
     *
     * @param context
     * @param stateFlow
     */
    public void startStateMachine(FacesContext context, SCXML stateFlow) {
        UIViewRoot currentViewRoot = context.getViewRoot();
        Map<String, Object> currentViewMapShallowCopy = Collections.emptyMap();

        try {
            context.setProcessingEvents(false);

            StateFlowHandler handler = StateFlowHandler.getInstance();

            Flash flash = context.getExternalContext().getFlash();
            Map<String, Object> params = new LinkedHashMap<>();
            Set<String> keySet = flash.keySet();
            for (String key : keySet) {
                params.put(key, flash.get(key));
            }
            Map<String, String> pmap = context.getExternalContext().getRequestParameterMap();
            for (String key : pmap.keySet()) {
                params.put(key, pmap.get(key));
            }

            String executorId = handler.getExecutorViewRootId(context);

            SCXMLExecutor executor = handler.createRootExecutor(executorId, context, stateFlow);
            Context sctx = executor.getRootContext();
            sctx.setLocal(FACES_CHART_CONTROLLER_TYPE, VIEWROOT_CONTROLLER_TYPE);

            if (null != currentViewRoot) {
                String viewId = currentViewRoot.getViewId();
                sctx.setLocal(FACES_CHART_EXECUTOR_VIEW_ID, viewId);
            }

            handler.execute(context, executor, params);
            UIViewRoot result = context.getViewRoot();

            if (null != currentViewRoot) {
                Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                if (null != currentViewMap && !currentViewMap.isEmpty()) {
                    currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                    Map<String, Object> resultViewMap = result.getViewMap(true);
                    resultViewMap.putAll(currentViewMapShallowCopy);

                }
            }
        } catch (ModelException ex) {
            Logger.getLogger(StateFlowPhaseListener.class
                    .getName()).log(Level.SEVERE, null, ex);
        } finally {
            context.setProcessingEvents(true);
            if (!currentViewMapShallowCopy.isEmpty()) {
                currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                currentViewMapShallowCopy.clear();
            }
        }

    }

}
