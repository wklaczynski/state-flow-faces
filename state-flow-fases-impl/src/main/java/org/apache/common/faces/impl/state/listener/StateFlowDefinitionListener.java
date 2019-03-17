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
package org.apache.common.faces.impl.state.listener;

import java.util.ArrayList;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import static org.apache.common.faces.state.StateFlow.DEFINITION_SET_HINT;
import org.apache.common.faces.state.component.UIStateChartMachine;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowDefinitionListener implements SystemEventListener {

    @Override
    public void processEvent(SystemEvent cse) throws AbortProcessingException {
        if (!(cse.getSource() instanceof UIStateChartMachine)) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIStateChartMachine component = (UIStateChartMachine) cse.getSource();
        String clientId = ((UIComponent) component).getClientId(facesContext);
        if(facesContext.getViewRoot() == null) {
            return ;
        }

        ArrayList<String> clientIds = getDefinitionClientIds(facesContext);
        if (cse instanceof PostAddToViewEvent) {
            if (clientIds == null) {
                clientIds = new ArrayList<>();
                facesContext.getViewRoot().getAttributes().put(DEFINITION_SET_HINT, clientIds);
            }

            if (!clientIds.contains(clientId)) {
                clientIds.add(clientId);
            } else {
                clientIds.remove(clientId);
            }
        }

    }

    @Override
    public boolean isListenerForSource(Object o) {
        return o instanceof UIStateChartMachine;
    }

    public static ArrayList<String> getDefinitionClientIds(FacesContext context) {
        if(context.getViewRoot() == null) {
            return null;
        }
        return (ArrayList<String>) context.getViewRoot().getAttributes().get(DEFINITION_SET_HINT);
    }
}
