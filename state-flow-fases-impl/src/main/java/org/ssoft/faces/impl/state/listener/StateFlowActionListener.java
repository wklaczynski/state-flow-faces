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

import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.component.UIStateChartFacetRender;
import javax.faces.state.utils.ComponentUtils;

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
        UIComponent source = event.getComponent();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = facesContext.getViewRoot();
        UIStateChartFacetRender render = null;
        UIStateChartExecutor executor = null;
        try {
            String sorceId = source.getClientId(facesContext);

            Map<Object, Object> attrs = facesContext.getAttributes();
            attrs.put(CURRENT_COMPONENT_HINT, sorceId);

            if (viewRoot != null) {
                executor = ComponentUtils.assigned(UIStateChartExecutor.class, source);
                if (executor != null) {
                    executor.pushExecutorToEl(facesContext, executor);
                }
                render = ComponentUtils.assigned(UIStateChartFacetRender.class, source);
                if (render != null) {
                    render.pushRendererToEl(facesContext, render);
                }
            }
            base.processAction(event);
        } finally {
            if (render != null) {
                render.popRendererFromEl(facesContext);
            }
            if (executor != null) {
                executor.popExecutorFromEl(facesContext);
            }
            facesContext.getAttributes().remove(CURRENT_COMPONENT_HINT);
        }
    }

}
