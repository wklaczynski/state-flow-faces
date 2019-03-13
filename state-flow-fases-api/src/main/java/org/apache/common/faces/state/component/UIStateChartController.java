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
import java.util.LinkedHashMap;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIParameter;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.faces.view.Location;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_FACET;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartController extends UIPanel {

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

            if (!executor.isRunning()) {
                handler.close(context, executor);
            }

            if (context.getResponseComplete()) {
                handler.writeState(context);
            }

        } finally {
            StateFlow.popExecutorFromEL(context);
        }

        return consumed;

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

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        pushExecutor(context);
        try {
            super.encodeBegin(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        pushExecutor(context);
        try {
            super.encodeChildren(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        pushExecutor(context);
        try {
            UIComponent renderComponent = getCurentRenderComponent(context);
            if (renderComponent != null) {
                renderComponent.encodeAll(context);
            }
            super.encodeEnd(context);
        } finally {
            pushExecutor(context);
        }
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        FacesContext fc = context.getFacesContext();
        String executorId = getExecutorId();
        if (executorId == null) {
            return super.visitTree(context, callback);
        }

        pushExecutor(fc);
        try {
            return super.visitTree(context, callback);
        } finally {
            popExecutor(fc);
        }
    }

    @Override
    public void processDecodes(FacesContext context) {
        pushExecutor(context);
        try {
            super.processDecodes(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void processRestoreState(FacesContext context, Object state) {
        pushExecutor(context);
        try {
            super.processRestoreState(context, state);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public Object processSaveState(FacesContext context) {
        pushExecutor(context);
        try {
            return super.processSaveState(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void processUpdates(FacesContext context) {
        pushExecutor(context);
        try {
            super.processUpdates(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        pushExecutor(context);
        try {
            super.processValidators(context);
        } finally {
            popExecutor(context);
        }
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        super.restoreState(context, state);
    }

    public UIComponent getCurentRenderComponent(FacesContext context) {
        UIComponent facet = null;

        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor != null) {
            Context sctx = executor.getRootContext();
            String source = (String) sctx.get(FACES_CHART_FACET);
            if (source != null) {
                if (source.startsWith("@controller:")) {
                    String name = source.substring(12);
                    facet = getFacet(name);
                    if (facet == null) {
                        throwRequiredFacetException(context, name);
                    }
                }
            }
        }
        return facet;
    }

    public UIComponent getRenderNamingContainer(FacesContext context) {
        UIComponent namingContainer = getCurentRenderComponent(context);
        while (namingContainer != null) {
            if (namingContainer instanceof NamingContainer) {
                return namingContainer;
            }
            namingContainer = namingContainer.getParent();
        }
        return null;
    }

    public SCXMLExecutor getRootExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        return executor;
    }

    private Map<String, Object> getParamsMap() {
        Map<String, Object> params = new LinkedHashMap<>();
        int childCount = getChildCount();
        if (childCount > 0) {
            for (UIComponent kid : getChildren()) {
                if (kid instanceof UIParameter) {
                    UIParameter uiParam = (UIParameter) kid;
                    String key = uiParam.getName();
                    if (key == null) {
                        key = uiParam.getId();
                    }
                    if (key == null) {
                        key = uiParam.getClientId();
                    }
                    Object value = uiParam.getValue();
                    params.put(key, value);
                }
            }
        }
        return params;
    }

    private void throwRequiredFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in controller component "
                + " with id \"" + getClientId(ctx) + "\"");

    }

}
