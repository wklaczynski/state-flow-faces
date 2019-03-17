/*
 * Copyright 2019 Waldemar Kłaczyński.
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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PhaseId;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import static javax.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.CONTROLLER_SET_HINT;
import static javax.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowControllerListener implements SystemEventListener {

    @Override
    public void processEvent(SystemEvent cse) throws AbortProcessingException {
        if (!(cse.getSource() instanceof UIStateChartExecutor)) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIStateChartExecutor component = (UIStateChartExecutor) cse.getSource();
        String clientId = ((UIComponent) component).getClientId(facesContext);

        ArrayList<String> clientIds = getControllerClientIds(facesContext);
        UIViewRoot root = facesContext.getViewRoot();

        if (cse instanceof PostAddToViewEvent) {
            if (clientIds == null) {
                clientIds = new ArrayList<>();
                facesContext.getViewRoot().getAttributes().put(CONTROLLER_SET_HINT, clientIds);
            }

            if (!clientIds.contains(clientId)) {
                clientIds.add(clientId);
            } else {
                clientIds.remove(clientId);
            }
        }

        if (root != null && clientIds != null && !clientIds.isEmpty()) {

            if (cse instanceof PostRestoreStateEvent) {
                StateFlowHandler handler = StateFlowHandler.getInstance();

                String eventName = BEFORE_PHASE_EVENT_PREFIX
                        + PhaseId.RESTORE_VIEW.getName().toLowerCase();

                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                root.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartExecutor) {
                        UIStateChartExecutor controller = (UIStateChartExecutor) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder eb = new EventBuilder(eventName, TriggerEvent.CALL_EVENT)
                                .sendId(controllerId);

                        SCXMLExecutor executor = controller.getRootExecutor(facesContext);
                        if (executor != null) {
                            try {
                                executor.triggerEvent(eb.build());
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }
                    }
                    return VisitResult.ACCEPT;
                });
            } else if (cse instanceof PreRenderViewEvent) {
                StateFlowHandler handler = StateFlowHandler.getInstance();

                String eventName = ENCODE_DISPATCHER_EVENTS;

                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                root.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartExecutor) {
                        UIStateChartExecutor controller = (UIStateChartExecutor) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder eb = new EventBuilder(eventName, TriggerEvent.CALL_EVENT)
                                .sendId(controllerId);

                        SCXMLExecutor executor = controller.getRootExecutor(facesContext);
                        if (executor != null) {
                            try {
                                executor.triggerEvent(eb.build());
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }
                    }
                    return VisitResult.ACCEPT;
                });
            }

        }
    }

    @Override
    public boolean isListenerForSource(Object o) {
        return o instanceof UIStateChartExecutor;
    }

    public static ArrayList<String> getControllerClientIds(FacesContext context) {
        if (context.getViewRoot() == null) {
            return null;
        }
        return (ArrayList<String>) context.getViewRoot().getAttributes().get(CONTROLLER_SET_HINT);
    }
}
