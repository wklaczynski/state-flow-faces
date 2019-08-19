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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.ClientWindow;
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
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowPhaseListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext fc = event.getFacesContext();

        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            restoreStateFlow(fc);
        }

        UIViewRoot viewRoot = fc.getViewRoot();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (viewRoot != null) {

            String name = AFTER_PHASE_EVENT_PREFIX
                    + event.getPhaseId().getName().toLowerCase();

            if (handler.hasViewRoot(fc)) {
                SCXMLExecutor executor = handler.getViewExecutor(fc);

                EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                        .sendId(fc.getViewRoot().getViewId());

                try {
                    executor.triggerEvent(eb.build());
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }
            }

            ArrayList<String> clientIds = getControllerClientIds(fc);
            if (clientIds != null && !clientIds.isEmpty()) {
                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(fc, clientIds, hints);
                viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartExecutor) {
                        UIStateChartExecutor controller = (UIStateChartExecutor) target;
                        String controllerId = controller.getClientId(fc);

                        EventBuilder veb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                .sendId(viewRoot.getViewId());

                        SCXMLExecutor executor = null;
                        String executorId = controller.getExecutorId();
                        if (executorId != null) {
                            executor = handler.getRootExecutor(fc, executorId);
                        }

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

            if (event.getPhaseId() == PhaseId.RENDER_RESPONSE || fc.getResponseComplete()) {
                handler.writeState(fc);
            }

        }

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext fc = event.getFacesContext();

        if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
            UIViewRoot viewRoot = fc.getViewRoot();
            StateFlowHandler handler = StateFlowHandler.getInstance();

            if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
                handler.writeState(fc);
            }

            if (viewRoot != null) {

                String name = BEFORE_PHASE_EVENT_PREFIX
                        + event.getPhaseId().getName().toLowerCase();

                if (handler.hasViewRoot(fc)) {

                    EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = handler.getRootExecutor(fc);
                    try {
                        executor.triggerEvent(eb.build());
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }

                    if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                            && !fc.getResponseComplete()) {
                        EventDispatcher ed = executor.getEventdispatcher();
                        if (ed instanceof FacesProcessHolder) {
                            try {
                                EventBuilder eeb = new EventBuilder(DECODE_DISPATCHER_EVENTS,
                                        TriggerEvent.CALL_EVENT)
                                        .sendId(viewRoot.getViewId());

                                executor.triggerEvent(eeb.build());
                                ((FacesProcessHolder) ed).processDecodes(fc);
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }
                    }

                }

                ArrayList<String> clientIds = getControllerClientIds(fc);
                if (clientIds != null && !clientIds.isEmpty()) {
                    Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                    VisitContext visitContext = VisitContext.createVisitContext(fc, clientIds, hints);
                    viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                        if (target instanceof UIStateChartExecutor) {
                            UIStateChartExecutor controller = (UIStateChartExecutor) target;
                            String controllerId = controller.getClientId(fc);

                            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                                    .sendId(viewRoot.getViewId());

                            SCXMLExecutor executor = null;
                            String executorId = controller.getExecutorId();
                            if (executorId != null) {
                                executor = handler.getRootExecutor(fc, executorId);
                            }

                            if (executor != null) {
                                try {
                                    executor.triggerEvent(eb.build());
                                } catch (ModelException ex) {
                                    throw new FacesException(ex);
                                }

                                if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                                        && !fc.getResponseComplete()) {

                                    EventDispatcher ed = executor.getEventdispatcher();
                                    if (ed instanceof FacesProcessHolder) {
                                        try {
                                            EventBuilder eeb = new EventBuilder(
                                                    DECODE_DISPATCHER_EVENTS,
                                                    TriggerEvent.CALL_EVENT)
                                                    .sendId(viewRoot.getViewId());

                                            executor.triggerEvent(eeb.build());
                                            ((FacesProcessHolder) ed).processDecodes(fc);
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
                    && !fc.getResponseComplete()) {

            }
        } else {
            if (fc.isPostback()) {
                return;
            }

            StateFlowHandler handler = StateFlowHandler.getInstance();
            String executorId = null;

            if (executorId == null) {
                executorId = fc.getExternalContext().getRequestParameterMap().get("exid");
            }

            if(executorId == null) {
                ExternalContext ec = fc.getExternalContext();
                Flash flash = ec.getFlash();
                executorId = (String) flash.get("exid");
            }
            
            Context ctx = handler.getFlowContext(fc, executorId);
            if (ctx != null) {
                executorId = (String) ctx.get(FACES_EXECUTOR_VIEW_ROOT_ID);
                if (executorId != null) {
                    fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
                    ctx.removeLocal(FACES_EXECUTOR_VIEW_ROOT_ID);
                }

                Object lastViewState = ctx.get(FACES_VIEW_STATE);
                if (lastViewState != null) {
                    try {
                        ctx.removeLocal(FACES_VIEW_STATE);
                        if (!fc.isPostback()) {
                            fc.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                            String viewId = fc.getExternalContext().getRequestPathInfo();
                            UIViewRoot viewRoot = restoreView(fc, viewId);
                            fc.setViewRoot(viewRoot);
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
                        String uuid = (String) state.get(FACES_EXECUTOR_VIEW_ROOT_ID);
                        if (uuid != null) {
                            viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, uuid);
                        }

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

        String executorId = (String) viewRoot.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
        if (executorId != null) {
            SCXMLExecutor executor = flowHandler.getRootExecutor(context, executorId);
            if (executor != null) {

                ExecuteContextManager manager = ExecuteContextManager.getManager(context);
                String executePath = executor.getId();
                Context ectx = executor.getRootContext();
                ExecuteContext executeContext = new ExecuteContext(
                        executePath, executor, ectx);

                manager.initExecuteContext(context, executePath, executeContext);
                manager.push(executeContext);

                return;
            }
        }

        String pname = StateFlowParams.getRequestParamatrChartId();

        String scxmlId = null;

        if (scxmlId == null) {
            scxmlId = context.getExternalContext().getRequestParameterMap().get(pname);
        }
        if (scxmlId == null) {
            scxmlId = DEFAULT_STATE_MACHINE_NAME;
        }

        viewRoot.pushComponentToEL(context, viewRoot);
        try {
            SCXML scxml = flowHandler.findStateMachine(context, scxmlId);
            if (scxml != null) {
                SCXMLExecutor executor = startStateMachine(executorId, context, scxml);

                if (executor != null) {

                    ExecuteContextManager manager = ExecuteContextManager.getManager(context);
                    String executePath = executor.getId();
                    Context ectx = executor.getRootContext();
                    ExecuteContext executeContext = new ExecuteContext(
                            executePath, executor, ectx);

                    manager.initExecuteContext(context, executePath, executeContext);
                    manager.push(executeContext);
                }

            }
        } catch (ModelException ex) {
            throw new FacesException(ex);
        } finally {
            viewRoot.popComponentFromEL(context);
        }

    }

    /**
     *
     * @param executorId
     * @param context
     * @param stateFlow
     * @return
     */
    public SCXMLExecutor startStateMachine(String executorId, FacesContext context, SCXML stateFlow) {
        UIViewRoot currentViewRoot = context.getViewRoot();
        Map<String, Object> currentViewMapShallowCopy = Collections.emptyMap();
        SCXMLExecutor executor = null;

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

            executor = handler.createRootExecutor(executorId, context, stateFlow);
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
        return executor;
    }

}
