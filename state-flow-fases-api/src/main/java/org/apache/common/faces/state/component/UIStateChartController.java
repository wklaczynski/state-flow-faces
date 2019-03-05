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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;
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
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.env.SimpleContext;
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

    public boolean processAction(ActionEvent event) throws AbortProcessingException {
        boolean consumed = false;

        ActionEvent ae = (ActionEvent) event;
        FacesContext context = FacesContext.getCurrentInstance();

        ELContext elContext = context.getELContext();
        
        SCXMLExecutor executor = getRootExecutor(context);
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
                close(context, executor);
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

            SCXMLExecutor executor = getRootExecutor(context);
            if (executor == null) {
                SCXML stateMachine = findStateMachine(context, getName());
                if (stateMachine != null) {
                    StateFlowHandler handler = StateFlowHandler.getInstance();
                    try {
                        String executorId = context.getViewRoot().getViewId() + ":" + getClientId(context);
                        executor = handler.createRootExecutor(executorId, context, stateMachine);
                    } catch (ModelException ex) {
                        throw new IOException(ex);
                    }
                    elContext.putContext(SCXMLExecutor.class, executor);
                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);

                    Map<String, Object> params = getParamsMap();
                    execute(context, executor, params);

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
            SCXMLExecutor executor = getRootExecutor(fc);
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

    public boolean isActive(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return false;
        }
        Stack<SCXMLExecutor> stack = fs.getRoots();
        return stack != null && !stack.isEmpty();
    }

    public boolean isFinal(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return false;
        }
        return fs.isClosed();
    }

    public SCXMLExecutor getRootExecutor(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return null;
        }

        Stack<SCXMLExecutor> stack = fs.getRoots();
        if (stack.isEmpty()) {
            return null;
        }

        return stack.peek();
    }

    public void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params) {
        try {
            StateFlowHandler handler = StateFlowHandler.getInstance();

            boolean root = executor.getParentSCXMLIOProcessor() == null;

            FlowDeque fs = getFlowDeque(context, true);

            if (fs.isClosed()) {
                throw new FacesException("Can not execute new executor in finished flow istance.");
            }

            Stack<SCXMLExecutor> stack = fs.getRoots();

            if (root) {
                stack.push(executor);
            }

            handler.executorEntered(executor);

            try {
                executor.go(params);
                executor.triggerEvents();
            } catch (ModelException me) {
                close(context, executor);
                throw new FacesException(me);
            }

            if (!executor.isRunning()) {
                close(context, executor);
            }
        } catch (FacesException ex) {
            throw new FacesException(ex);
        } catch (Throwable ex) {
            throw new FacesException(ex);
        }
    }

    public void close(FacesContext context, SCXMLExecutor executor) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        FlowDeque fs = getFlowDeque(context, true);
        Stack<SCXMLExecutor> stack = fs.getRoots();

        if (!stack.isEmpty()) {

            if (executor == null) {
                executor = stack.get(0);
            }

            if (stack.contains(executor)) {
                while (!stack.empty()) {
                    SCXMLExecutor last = stack.pop();
                    handler.executorExited(last);
                    if (last == executor) {
                        break;
                    }
                }
            } else {
                if (executor != null) {
                    handler.executorExited(executor);
                }
            }
        }

        if (stack.isEmpty()) {
            closeFlowDeque(context);
        }

    }

    private FlowDeque getFlowDeque(FacesContext context, boolean create) {
        FlowDeque result = (FlowDeque) getStateHelper()
                .eval(PropertyKeys.flowQueue, null);

        if (result != null || !create) {
            return result;
        }

        result = new FlowDeque();

        getStateHelper().put(PropertyKeys.flowQueue, result);

        return result;
    }

    private void closeFlowDeque(FacesContext context) {

        FlowDeque flowDeque = getFlowDeque(context, false);
        if (flowDeque != null) {
            flowDeque.close();
        }

        getStateHelper().remove(PropertyKeys.flowQueue);
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

    private static class FlowDeque implements Serializable {

        private final Stack<SCXMLExecutor> executors;
        private boolean closed;

        public FlowDeque() {
            executors = new Stack<>();
        }

        public Stack<SCXMLExecutor> getRoots() {
            return executors;
        }

        public boolean isClosed() {
            return closed;
        }

        public void close() {
            closed = true;
        }

        // ----------------------------------------------- Serialization Methods
        // This is dependent on serialization occuring with in a
        // a Faces request, however, since SCXMLExecutor.{save,restore}State()
        // doesn't actually serialize the FlowDeque, these methods are here
        // purely to be good citizens.
        private void writeObject(ObjectOutputStream out) throws IOException {
            Object[] states = new Object[2];

            states[1] = closed;

            if (null != executors && executors.size() > 0) {
                Object[] attached = new Object[executors.size()];
                int i = 0;
                for (SCXMLExecutor executor : executors) {
                    Object values[] = new Object[5];
                    SCXML stateMachine = executor.getStateMachine();

                    Context context = new SimpleContext();
                    Context.setCurrentInstance(context);

                    values[0] = executor.getId();
                    values[1] = stateMachine.getMetadata().get("faces-viewid");
                    values[2] = stateMachine.getMetadata().get("faces-chartid");
                    //values[3] = saveContext(context, executor.getRootContext());
                    values[4] = executor.saveState(context);
                    attached[i++] = values;
                }
                states[0] = attached;
            }

            //noinspection NonSerializableObjectPassedToObjectStream
            out.writeObject(states);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            //noinspection unchecked
            StateFlowHandler handler = StateFlowHandler.getInstance();
            FacesContext fc = FacesContext.getCurrentInstance();
            executors.clear();
            Object[] states = (Object[]) in.readObject();

            closed = (boolean) states[1];

            if (states[0] != null) {
                Object[] entries = (Object[]) states[0];
                for (Object entry : entries) {
                    Object[] values = (Object[]) entry;

                    String executorId = (String) values[0];
                    String viewId = (String) values[1];
                    String id = (String) values[2];

                    SCXML stateMachine = null;
                    try {
                        stateMachine = handler.createStateMachine(fc, viewId, id);
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }

                    if (stateMachine == null) {
                        throw new FacesException(String.format("Restored state flow %s in %s not found.", viewId, id));
                    }

                    SCXMLExecutor executor;
                    try {
                        executor = handler.createRootExecutor(executorId, fc, stateMachine);
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }

                    Context context = new SimpleContext();
                    Context.setCurrentInstance(context);

                    //restoreContext(context, executor.getRootContext(), values[3]);
                    executor.restoreState(context, values[4]);

                    executors.add(executor);
                }
            }
        }
    }

}