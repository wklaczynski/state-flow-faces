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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import java.util.Map;
import javax.faces.state.execute.ExecuteContext;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import javax.faces.state.execute.ExecuteContextManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowActionListener implements ActionListener {

    private final ActionListener base;

    /**
     *
     * @param base
     */
    public StateFlowActionListener(ActionListener base) {
        this.base = base;
    }

    @Override
    @SuppressWarnings("FinallyDiscardsException")
    public void processAction(ActionEvent event) throws AbortProcessingException {
        boolean pushed = false;
        UIComponent source = event.getComponent();
        FacesContext facesContext = FacesContext.getCurrentInstance();

        ExecuteContextManager manager = ExecuteContextManager.getManager(facesContext);

        try {
            String sorceId = source.getClientId(facesContext);

            Map<Object, Object> attrs = facesContext.getAttributes();
            attrs.put(CURRENT_COMPONENT_HINT, sorceId);

            ExecuteContext executeContext = manager.findExecuteContextByComponent(facesContext, source);
            if (executeContext != null) {
                pushed = manager.push(executeContext);
            }

            base.processAction(event);
        } finally {
            if(pushed) {
                manager.pop();
            }
            facesContext.getAttributes().remove(CURRENT_COMPONENT_HINT);
        }
    }

}
