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
package org.apache.common.faces.impl.state.listener;

import com.sun.faces.context.StateContext;
import com.sun.faces.facelets.tag.ui.UIDebug;
import com.sun.faces.renderkit.RenderKitUtils;
import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import static com.sun.faces.util.Util.getStateManager;
import static com.sun.faces.util.Util.notNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import static javax.faces.component.UIViewRoot.COMPONENT_TYPE;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.render.ResponseStateManager;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.faces.state.component.UIStateChartDefinition;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.faces.impl.state.StateFlowParams;
import static org.apache.common.faces.impl.state.listener.StateFlowControllerListener.getControllerClientIds;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.DEFAULT_STATECHART_NAME;
import static org.apache.common.faces.state.StateFlow.SKIP_START_STATE_MACHINE_HINT;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.faces.state.task.FacesProcessHolder;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.EventDispatcher;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;
import static org.apache.common.faces.state.StateFlow.DECODE_DISPATCHER_EVENTS;
import org.apache.common.faces.state.component.UIStateChartController;
import org.apache.common.scxml.invoke.InvokerException;

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

            if (handler.isActive(facesContext)) {
                SCXMLExecutor executor = handler.getRootExecutor(facesContext);

                EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                        .sendId(facesContext.getViewRoot().getViewId());

                try {
                    executor.triggerEvent(eb.build());
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }

                if (!executor.isRunning()) {
                    handler.close(facesContext, executor);
                }

                Context.setCurrentInstance(
                        (Context) facesContext.getELContext().getContext(Context.class));

            }

            ArrayList<String> clientIds = getControllerClientIds(facesContext);
            if (clientIds != null && !clientIds.isEmpty()) {
                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartController) {
                        UIStateChartController controller = (UIStateChartController) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder veb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                .sendId(viewRoot.getViewId());

                        SCXMLExecutor executor = controller.getRootExecutor(facesContext);
                        if (executor != null) {
                            try {
                                executor.triggerEvent(veb.build());
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }

                            if (!executor.isRunning()) {
                                handler.close(facesContext, executor);
                            }
                        }

                    }
                    return VisitResult.ACCEPT;
                });
            }

            if (handler.isInWindow(facesContext)) {
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

                StateFlow.initViewContext(facesContext);

                if (handler.isActive(facesContext)) {

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

                    if (!executor.isRunning()) {
                        handler.close(facesContext, executor);
                    }

                }

                ArrayList<String> clientIds = getControllerClientIds(facesContext);
                if (clientIds != null && !clientIds.isEmpty()) {
                    Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                    VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                    viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                        if (target instanceof UIStateChartController) {
                            UIStateChartController controller = (UIStateChartController) target;
                            String controllerId = controller.getClientId(facesContext);

                            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                    .sendId(viewRoot.getViewId());

                            SCXMLExecutor executor = controller.getRootExecutor(facesContext);
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

                                if (!executor.isRunning()) {
                                    handler.close(facesContext, executor);
                                }
                            }
                        }
                        return VisitResult.ACCEPT;
                    });
                }

                StateFlow.resolveViewContext(facesContext);

            } else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                    && !facesContext.getResponseComplete()) {

                if (handler.isFinal(facesContext)) {
                    handler.close(facesContext);
                }
            }
        } else {
            StateFlowHandler handler = StateFlowHandler.getInstance();
            if (handler.isInWindow(facesContext)) {
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
        if (flowHandler.isActive(context)) {
            return;
        }

        UIComponent facet = viewRoot.getFacet(STATECHART_FACET_NAME);
        if (facet != null) {
            SCXML stateChart = null;

            String pname = StateFlowParams.getRequestParamatrChartId();

            String scxmlId = null;

            if (scxmlId == null) {
                scxmlId = context.getExternalContext().getRequestParameterMap().get(pname);
            }
            if (scxmlId == null) {
                scxmlId = DEFAULT_STATECHART_NAME;
            }

            UIStateChartDefinition uichart = (UIStateChartDefinition) facet.findComponent(scxmlId);
            if (uichart != null) {
                stateChart = uichart.getStateChart();
            }

            if (stateChart != null) {
                startStateMachine(context, stateChart);
            }
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

            StateFlowHandler flowHandler = StateFlowHandler.getInstance();

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

            String viewId = currentViewRoot.getViewId();

            String executorId = UUID.randomUUID().toString();
            SCXMLExecutor executor = flowHandler.createRootExecutor(executorId, context, stateFlow);

            flowHandler.execute(context, executor, params);
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
