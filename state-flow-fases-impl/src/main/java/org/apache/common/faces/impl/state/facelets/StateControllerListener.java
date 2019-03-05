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
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.PreRenderComponentEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import static org.apache.common.faces.state.StateFlow.CONTROLLER_SET_HINT;
import org.apache.common.faces.state.component.UIStateChartController;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateControllerListener implements SystemEventListener {

    @Override
    public void processEvent(SystemEvent cse) throws AbortProcessingException {
        if (!(cse.getSource() instanceof UIStateChartController)) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        UIStateChartController component = (UIStateChartController) cse.getSource();
        String clientId = ((UIComponent) component).getClientId(context);

        if (cse instanceof PostAddToViewEvent) {
            ArrayList<String> clientIds = getControllerClientIds(context);
            if (clientIds == null) {
                clientIds = new ArrayList<>();
                context.getViewRoot().getAttributes().put(CONTROLLER_SET_HINT, clientIds);
            }

            if (!clientIds.contains(clientId)) {
                clientIds.add(clientId);
            } else {
                clientIds.remove(clientId);
            }
        }


        else if (cse instanceof PreRenderComponentEvent) {
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
