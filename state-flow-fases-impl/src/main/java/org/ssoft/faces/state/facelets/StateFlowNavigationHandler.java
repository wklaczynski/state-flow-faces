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
package org.ssoft.faces.state.facelets;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.StateFlowHandler;
import org.ssoft.faces.state.invokers.ViewInvoker;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowNavigationHandler extends ConfigurableNavigationHandler {

    private final static Logger logger = Logger.getLogger(StateFlowNavigationHandler.class.getName());
    private final NavigationHandler wrappedNavigationHandler;

    public StateFlowNavigationHandler(NavigationHandler navigationHandler) {
        this.wrappedNavigationHandler = navigationHandler;
    }

    @Override
    public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome) {
        ConfigurableNavigationHandler wrappedConfigurableNavigationHandler = (ConfigurableNavigationHandler) wrappedNavigationHandler;
        return wrappedConfigurableNavigationHandler.getNavigationCase(context, fromAction, outcome);
    }

    @Override
    public Map<String, Set<NavigationCase>> getNavigationCases() {
        ConfigurableNavigationHandler wrappedConfigurableNavigationHandler = (ConfigurableNavigationHandler) wrappedNavigationHandler;
        return wrappedConfigurableNavigationHandler.getNavigationCases();
    }

    @Override
    public void handleNavigation(FacesContext context, String fromAction, String outcome) {
            StateFlowHandler handler = StateFlowHandler.getInstance();
        if (handler.isActive(context)) {
            if (outcome == null) {
                return;
            }
            if (outcome.endsWith(".xhtml")) {
                handler.stopExecutor(context);
                wrappedNavigationHandler.handleNavigation(context, fromAction, outcome);
            } else {
                StateFlowExecutor executor = handler.getRootExecutor(context);
                try {
                    executor.triggerEvent(new FlowTriggerEvent(ViewInvoker.OUTCOME_EVENT_PREFIX + outcome, FlowTriggerEvent.SIGNAL_EVENT));
                } catch (ModelException ex) {
                    logger.log(Level.SEVERE, ex.getMessage(), ex);
                }
                executor = handler.getRootExecutor(context);
                if (executor.getCurrentStatus().isFinal()) {
                    handler.stopExecutor(context);
                }
            }
        } else {
            wrappedNavigationHandler.handleNavigation(context, fromAction, outcome);
        }
    }
}
