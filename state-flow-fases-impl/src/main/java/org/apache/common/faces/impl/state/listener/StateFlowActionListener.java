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

import org.apache.common.faces.impl.state.listener.StateFlowControllerListener;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import org.apache.common.faces.impl.state.utils.ComponentUtils;
import org.apache.common.faces.state.component.UIStateChartController;
import static org.apache.common.faces.state.StateFlow.CURRENT_COMPONENT_HINT;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowActionListener implements ActionListener {

    private final ActionListener base;
    private final ThreadLocal<Boolean> consumed = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     *
     * @param base
     */
    public StateFlowActionListener(ActionListener base) {
        this.base = base;
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    public void processAction(ActionEvent event) throws AbortProcessingException {

        consumed.set(false);

        UIComponent source = event.getComponent();
        FacesContext facesContext = FacesContext.getCurrentInstance();

        String sorceId = source.getClientId(facesContext);

        Map<Object, Object> attrs = facesContext.getAttributes();
        attrs.put(CURRENT_COMPONENT_HINT, sorceId);

        Collection<String> controllers = StateFlowControllerListener.getControllerClientIds(facesContext);
        UIViewRoot root = facesContext.getViewRoot();
        if (root != null && controllers != null && !controllers.isEmpty()) {

            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, controllers, hints);
            root.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartController) {
                    if (ComponentUtils.isInOrEqual(target, source)) {
                        UIStateChartController controller = (UIStateChartController) target;
                        if (controller.processAction(event)) {
                            consumed.set(true);
                            return VisitResult.COMPLETE;
                        }
                    }
                }
                return VisitResult.ACCEPT;
            });
        }

        if (!consumed.get()) {
            base.processAction(event);
        }
    }

}
