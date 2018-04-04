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
package org.apache.common.faces.impl.state.facelets;

import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import org.apache.common.faces.impl.state.StateFlowContext;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.faces.state.component.UIStateChartRoot;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.faces.impl.state.StateFlowParams;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
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
import org.apache.common.faces.state.StateFlowViewContext;
import org.apache.common.scxml.env.EffectiveContextMap;
import static org.apache.common.faces.state.StateFlow.VIEW_INVOKE_CONTEXT;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowPhaseListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();

        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            restoreStateFlow(context);
        }

        UIViewRoot viewRoot = context.getViewRoot();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (viewRoot != null && handler.isActive(context)) {

            SCXMLExecutor executor = handler.getRootExecutor(context);

            String name = AFTER_PHASE_EVENT_PREFIX
                    + event.getPhaseId().getName().toLowerCase();

            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                    .sendId(context.getViewRoot().getViewId());

            try {
                executor.triggerEvent(eb.build());
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }

            if (!executor.isRunning()) {
                handler.close(context, executor);
            }

            Context.setCurrentInstance(
                    (Context) context.getELContext().getContext(Context.class));

            if (event.getPhaseId() == PhaseId.RENDER_RESPONSE || context.getResponseComplete()) {
                handler.writeState(context);
            }

        }

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();

        if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
            UIViewRoot viewRoot = context.getViewRoot();
            StateFlowHandler handler = StateFlowHandler.getInstance();
            if (viewRoot != null && handler.isActive(context)) {

                StateFlowViewContext viewContext = (StateFlowViewContext) context.getAttributes()
                        .get(VIEW_INVOKE_CONTEXT.get(viewRoot.getViewId()));

                if (viewContext != null) {
                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, viewContext.getExecutor());
                    context.getELContext().putContext(SCXMLExecutor.class, viewContext.getExecutor());
                    Context stateContext = getEffectiveContext(viewContext.getContext());
                    context.getELContext().putContext(Context.class, stateContext);
                }

                String name = BEFORE_PHASE_EVENT_PREFIX
                        + event.getPhaseId().getName().toLowerCase();

                EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                        .sendId(viewRoot.getViewId());

                SCXMLExecutor executor = handler.getRootExecutor(context);
                try {
                    executor.triggerEvent(eb.build());
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }

                if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                        && !context.getResponseComplete()) {
                    EventDispatcher ed = executor.getEventdispatcher();
                    if (ed instanceof FacesProcessHolder) {
                        try {
                            EventBuilder eeb = new EventBuilder(DECODE_DISPATCHER_EVENTS,
                                    TriggerEvent.CALL_EVENT)
                                    .sendId(viewRoot.getViewId());

                            executor.triggerEvent(eeb.build());
                            ((FacesProcessHolder) ed).processDecodes(context);
                        } catch (ModelException ex) {
                            throw new FacesException(ex);
                        }
                    }
                }

                if (!executor.isRunning()) {
                    handler.close(context, executor);
                }

            } else if (event.getPhaseId() == PhaseId.APPLY_REQUEST_VALUES
                    && !context.getResponseComplete()) {

                if (handler.isFinal(context)) {
                    handler.close(context);
                }
            }

        } else {
            StateFlowHandler handler = StateFlowHandler.getInstance();
            if (handler.isActive(context)) {
                SCXMLExecutor executor = handler.getRootExecutor(context);
                Context ctx = executor.getRootContext();
                Object lastViewState = ctx.get(FACES_VIEW_STATE);
                ctx.getVars().remove(FACES_VIEW_STATE);
                if (lastViewState != null) {
                    context.getAttributes().put(FACES_VIEW_STATE, lastViewState);
                }
            }
        }
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    protected StateFlowContext getEffectiveContext(final Context nodeCtx) {
        return new StateFlowContext(nodeCtx, new EffectiveContextMap(nodeCtx));
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

            UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent(scxmlId);
            if (uichart != null) {
                stateChart = uichart.getStateChart();
            }

            if (stateChart != null) {
                startStateMachine(context, stateChart);
            }
        }
    }

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

            SCXMLExecutor executor = flowHandler.createRootExecutor(context, stateFlow);

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
