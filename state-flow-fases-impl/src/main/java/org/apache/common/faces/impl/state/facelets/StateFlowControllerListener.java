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
package org.apache.common.faces.impl.state.facelets;

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
import static org.apache.common.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.CONTROLLER_SET_HINT;
import static org.apache.common.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import org.apache.common.faces.state.component.UIStateChartController;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowControllerListener implements SystemEventListener {

    @Override
    public void processEvent(SystemEvent cse) throws AbortProcessingException {
        if (!(cse.getSource() instanceof UIStateChartController)) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIStateChartController component = (UIStateChartController) cse.getSource();
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

                String eventName = BEFORE_PHASE_EVENT_PREFIX
                        + PhaseId.RESTORE_VIEW.getName().toLowerCase();

                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                root.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartController) {
                        UIStateChartController controller = (UIStateChartController) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder eb = new EventBuilder(eventName, TriggerEvent.CALL_EVENT)
                                .sendId(controllerId);

                        SCXMLExecutor rootExecutor = controller.getRootExecutor(facesContext);
                        if (rootExecutor != null) {
                            try {
                                rootExecutor.triggerEvent(eb.build());
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                        }
                    }
                    return VisitResult.ACCEPT;
                });
            } else if (cse instanceof PreRenderViewEvent) {

                String eventName = ENCODE_DISPATCHER_EVENTS;

                Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
                VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
                root.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                    if (target instanceof UIStateChartController) {
                        UIStateChartController controller = (UIStateChartController) target;
                        String controllerId = controller.getClientId(facesContext);

                        EventBuilder eb = new EventBuilder(eventName, TriggerEvent.CALL_EVENT)
                                .sendId(controllerId);

                        SCXMLExecutor rootExecutor = controller.getRootExecutor(facesContext);
                        if (rootExecutor != null) {
                            try {
                                rootExecutor.triggerEvent(eb.build());
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
        return o instanceof UIStateChartController;
    }

    public static ArrayList<String> getControllerClientIds(FacesContext context) {
        return (ArrayList<String>) context.getViewRoot().getAttributes().get(CONTROLLER_SET_HINT);
    }
}
