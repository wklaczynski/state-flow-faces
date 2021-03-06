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
package org.ssoft.faces.impl.state.facelets;

import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import static javax.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartFacetRender;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.utils.ComponentUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowNavigationHandler extends ConfigurableNavigationHandler {

    private final static Logger logger = Logger.getLogger(StateFlowNavigationHandler.class.getName());
    private final NavigationHandler wrappedNavigationHandler;

    /**
     *
     * @param navigationHandler
     */
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
    public void handleNavigation(FacesContext facesContext, String fromAction, String outcome) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        boolean consumed = false;

        SCXMLExecutor executor = handler.getRootExecutor(facesContext);
        if (executor != null) {
            if (outcome == null) {
                return;
            }
            if (outcome.endsWith(".xhtml")) {
                handler.closeAll(facesContext);
            } else {
                if (executor != null) {
                    consumed = true;

                    UIViewRoot viewRoot = facesContext.getViewRoot();
                    String sendId = viewRoot.getViewId();

                    String sorceId = (String) facesContext.getAttributes().get(CURRENT_COMPONENT_HINT);
                    if (sorceId != null) {
                        UIComponent source = viewRoot.findComponent(sorceId);
                        if (source != null) {
                            UIStateChartFacetRender render = ComponentUtils.passed(UIStateChartFacetRender.class, source);
                            if (render != null) {
                                sendId = render.getExecutePath(facesContext);
                            }
                        }
                    }

                    EventBuilder eb = new EventBuilder(
                            OUTCOME_EVENT_PREFIX + outcome,
                            TriggerEvent.CALL_EVENT);

                    eb.sendId(sendId);

                    try {
                        TriggerEvent ev = eb.build();
                        executor.triggerEvent(ev);
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }
                    
                    if (facesContext.getResponseComplete()) {
                        handler.writeState(facesContext);
                    }
                }
            }
        }

        if (!consumed) {
            wrappedNavigationHandler.handleNavigation(facesContext, fromAction, outcome);
        }
    }
}
