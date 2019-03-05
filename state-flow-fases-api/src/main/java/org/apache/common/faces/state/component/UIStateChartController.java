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
import javax.el.ELContext;
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
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;
import org.apache.common.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartController extends UIPanel {

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
    private UIComponent curentRenderComponent;

    enum PropertyKeys {
        name,
        required,
        flowQueue,
        facetId
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

    public java.lang.String getName() {
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

    public String getFacetId() {
        return (String) getStateHelper().get(PropertyKeys.facetId);
    }

    public void setFacetId(java.lang.String _facetId) {
        curentRenderComponent = null;
        getStateHelper().put(PropertyKeys.facetId, _facetId);
    }

    public String getExecutorId(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }

        String executorId = context.getViewRoot().getViewId() + ":" + getClientId(context);

        return executorId;
    }

    public boolean processAction(ActionEvent event) throws AbortProcessingException {
        boolean consumed = false;

        FacesContext context = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        ELContext elContext = context.getELContext();

        String executorId = getExecutorId(context);
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor == null) {
            return false;
        }

        SCXMLExecutor oldexecutor = (SCXMLExecutor) context.getAttributes()
                .get(CURRENT_EXECUTOR_HINT);
        Object oldelexecutor = elContext.getContext(SCXMLExecutor.class);
        try {
            context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
            elContext.putContext(SCXMLExecutor.class, executor);

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

            eb.sendId(getClientId(context));

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
            if (oldelexecutor != null) {
                elContext.putContext(SCXMLExecutor.class, oldelexecutor);
            }
            if (oldexecutor != null) {
                context.getAttributes().put(CURRENT_EXECUTOR_HINT, oldexecutor);
            } else {
                context.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            }
        }

        return consumed;

    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        ELContext elContext = context.getELContext();

        SCXMLExecutor oldexecutor = (SCXMLExecutor) context.getAttributes()
                .get(CURRENT_EXECUTOR_HINT);

        Object oldelexecutor = elContext.getContext(SCXMLExecutor.class);
        try {
            StateFlowHandler handler = StateFlowHandler.getInstance();

            String executorId = getExecutorId(context);
            SCXMLExecutor executor = handler.getRootExecutor(context, executorId);

            if (executor == null) {
                SCXML stateMachine = findStateMachine(context, getName());
                if (stateMachine != null) {
                    try {
                        executor = handler.createRootExecutor(executorId, context, stateMachine);
                    } catch (ModelException ex) {
                        throw new IOException(ex);
                    }
                    elContext.putContext(SCXMLExecutor.class, executor);
                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);

                    Map<String, Object> params = getParamsMap();
                    handler.execute(context, executor, params, true);
                }
            }
            UIComponent renderComponent = getCurentRenderComponent(context);
            if (renderComponent != null) {
                context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                elContext.putContext(SCXMLExecutor.class, executor);
                renderComponent.encodeAll(context);
            }
        } finally {
            if (oldelexecutor != null) {
                elContext.putContext(SCXMLExecutor.class, oldelexecutor);
            }

            if (oldexecutor != null) {
                context.getAttributes().put(CURRENT_EXECUTOR_HINT, oldexecutor);
            } else {
                context.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            }
        }

        super.encodeEnd(context);
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        FacesContext fc = context.getFacesContext();
        ELContext elContext = fc.getELContext();

        SCXMLExecutor oldfcexecutor = (SCXMLExecutor) fc.getAttributes()
                .get(CURRENT_EXECUTOR_HINT);

        Object oldelexecutor = elContext.getContext(SCXMLExecutor.class);
        try {
            StateFlowHandler handler = StateFlowHandler.getInstance();

            String executorId = getExecutorId(fc);
            SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);
            if (executor != null) {
                elContext.putContext(SCXMLExecutor.class, executor);
                fc.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
            } else {
                fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            }

            return super.visitTree(context, callback);
        } finally {
            if (oldelexecutor != null) {
                elContext.putContext(SCXMLExecutor.class, oldelexecutor);
            }

            if (oldfcexecutor != null) {
                fc.getAttributes().put(CURRENT_EXECUTOR_HINT, oldfcexecutor);
            } else {
                fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            }
        }
    }

    public UIComponent getCurentRenderComponent(FacesContext context) {
        if (curentRenderComponent == null) {
            String facetId = getFacetId();
            if (facetId != null) {
                curentRenderComponent = context.getViewRoot().findComponent(facetId);
            }
        }
        return curentRenderComponent;
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

    public SCXML findStateMachine(FacesContext context, String nameValue) throws IOException {
        UIComponent root = context.getViewRoot();
        UIComponent stateContiner = null;

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(context);
        if (compositeParent != null) {
            stateContiner = null;
            Map<String, UIComponent> facetMap = compositeParent.getFacets();
            UIComponent panel = facetMap.get(UIComponent.COMPOSITE_FACET_NAME);
            if (panel.getFacetCount() > 0) {
                stateContiner = panel.getFacets().get(STATECHART_FACET_NAME);
            }
        } else {
            if (root.getFacetCount() > 0) {
                stateContiner = root.getFacets().get(STATECHART_FACET_NAME);
            }
        }

        if (stateContiner == null && stateContiner.getChildCount() == 0) {
            return null;
        }

        UIStateChartDefinition uichart = (UIStateChartDefinition) stateContiner.findComponent(nameValue);

        SCXML stateChart = uichart.getStateChart();
        return stateChart;
    }

    public SCXMLExecutor getRootExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId(context);
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

}
