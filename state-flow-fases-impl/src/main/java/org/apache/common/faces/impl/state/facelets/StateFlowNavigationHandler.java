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

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.model.ModelException;

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
                handler.close(context);
                wrappedNavigationHandler.handleNavigation(context, fromAction, outcome);
            } else {
                EventBuilder eb = new EventBuilder(
                        OUTCOME_EVENT_PREFIX + outcome,
                        TriggerEvent.CALL_EVENT);

                eb.sendId(context.getViewRoot().getViewId());

                SCXMLExecutor root = handler.getRootExecutor(context);
                try {
                    TriggerEvent ev = eb.build();
                    root.triggerEvent(ev);
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }

                if (context.getResponseComplete()) {
                    handler.writeState(context);
                }

            }
        } else {
            wrappedNavigationHandler.handleNavigation(context, fromAction, outcome);
        }
    }
}
