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
package org.apache.common.faces.state.component;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.view.Location;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.scxml.EventBuilder;
import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.TriggerEvent;
import org.apache.common.faces.state.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartController extends UIPanel {

    public static final String CONTROLLER_FACET_NAME = "javax.faces.component.CONTROLLER_FACET_NAME";

    private String _executorId;

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_FAMILY = "org.apache.common.faces.StateFlow";

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_TYPE = "org.apache.common.faces.UIStateChartController";

    enum PropertyKeys {
        name,
        required,
    }

    /**
     *
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartController() {
        super();
        setRendererType(null);
        setTransient(false);
        setRendered(true);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getExecutorId() {
        return _executorId;
    }

    public void setExecutorId(String executorId) {
        this._executorId = executorId;
    }

    public String getName() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.name, null);
    }

    public void setName(java.lang.String _name) {
        getStateHelper().put(PropertyKeys.name, _name);
    }

    public boolean isRequired() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.required, true);
    }

    public void setRequired(boolean _required) {
        getStateHelper().put(PropertyKeys.required, _required);
    }

    public String getPath(FacesContext context) {
        String path = context.getViewRoot().getViewId() + "!" + getClientId(context);
        return path;
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        super.broadcast(event);
    }

    public boolean processAction(ActionEvent event) throws AbortProcessingException {
        boolean consumed = false;

        FacesContext context = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor == null) {
            return false;
        }

        try {
            StateFlow.pushExecutorToEL(context, executor, getPath(context));

            UIComponent source = event.getComponent();
            ActionSource actionSource = (ActionSource) source;

            Object invokeResult;
            String outcome = null;
            MethodBinding binding;

            binding = actionSource.getAction();
            if (binding != null) {
                try {
                    if (null != (invokeResult = binding.invoke(context, null))) {
                        outcome = invokeResult.toString();
                    }
                    // else, default to null, as assigned above.
                } catch (MethodNotFoundException e) {
                    throw new FacesException(binding.getExpressionString() + ": " + e.getMessage(),
                            e);
                } catch (EvaluationException e) {
                    throw new FacesException(binding.getExpressionString() + ": " + e.getMessage(),
                            e);
                }
            }

            EventBuilder eb = new EventBuilder(
                    OUTCOME_EVENT_PREFIX + outcome,
                    TriggerEvent.CALL_EVENT);

            String viewId = context.getViewRoot().getViewId();

            eb.sendId(viewId);

            try {
                TriggerEvent ev = eb.build();
                executor.triggerEvent(ev);
                consumed = true;
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }

            if (context.getResponseComplete()) {
                handler.writeState(context);
            }

        } finally {
            StateFlow.popExecutorFromEL(context);
        }

        return consumed;

    }

    @Override
    public void pushComponentToEL(FacesContext context, UIComponent component) {
        pushExecutor(context);
        super.pushComponentToEL(context, component);
//        UIComponent renderComponent = getFacet(CONTROLLER_FACET_NAME);
//        if (renderComponent != null) {
//            renderComponent.pushComponentToEL(context, component);
//        }
    }

    @Override
    public void popComponentFromEL(FacesContext context) {
//        UIComponent renderComponent = getFacet(CONTROLLER_FACET_NAME);
//        if (renderComponent != null) {
//            renderComponent.popComponentFromEL(context);
//        }
        super.popComponentFromEL(context);
        popExecutor(context);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        UIComponent renderComponent = getFacet(CONTROLLER_FACET_NAME);
        if (renderComponent != null) {
            renderComponent.encodeAll(context);
        }
        super.encodeEnd(context);
    }

    private void pushExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor != null) {
            StateFlow.pushExecutorToEL(context, executor, getPath(context));
        }
    }

    private void popExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor != null) {
            StateFlow.popExecutorFromEL(context);
        }
    }

    public SCXMLExecutor getRootExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        return executor;
    }

    private void throwRequiredFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in controller component "
                + " with id \"" + getClientId(ctx) + "\"");

    }

}
