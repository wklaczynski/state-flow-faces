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
package org.apache.faces.impl.state;

import org.apache.faces.impl.state.evaluator.StateFlowEvaluator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.ClientWindow;
import org.apache.scxml.Context;
import org.apache.scxml.SCXMLExecutor;
import org.apache.faces.state.events.OnFinishEvent;
import org.apache.scxml.invoke.Invoker;
import org.apache.scxml.model.CustomAction;
import javax.servlet.ServletContext;
import static org.apache.faces.impl.state.StateFlowConstants.ANNOTATED_CLASSES;
import org.apache.faces.state.component.UIStateChartRoot;
import org.apache.faces.state.StateFlowHandler;
import org.apache.scxml.model.Action;
import org.apache.scxml.model.ModelException;
import org.apache.scxml.model.SCXML;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import static org.apache.faces.impl.state.StateFlowConstants.STATE_CHART_LOGSTEP_PARAM_NAME;
import static org.apache.faces.impl.state.StateFlowConstants.STATE_FLOW_STACK;
import org.apache.faces.impl.state.cdi.CdiUtil;
import org.apache.faces.impl.state.cdi.StateFlowCDIHelper;
import org.apache.faces.impl.state.cdi.StateFlowCDIListener;
import org.apache.faces.impl.state.config.StateWebConfiguration;
import org.apache.faces.impl.state.invokers.SubInvoker;
import org.apache.faces.impl.state.invokers.ViewInvoker;
import static org.apache.faces.state.StateFlow.BUILD_STATE_MACHINE_HINT;
import static org.apache.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static org.apache.faces.state.StateFlow.SKIP_START_STATE_MACHINE_HINT;
import static org.apache.faces.state.StateFlow.STATECHART_FACET_NAME;
import static org.apache.faces.state.StateFlow.STATE_MACHINE_HINT;
import org.apache.faces.state.annotation.StateChartInvoker;
import org.apache.faces.state.annotation.StateChartAction;
import org.apache.scxml.env.AbstractSCXMLListener;
import org.apache.scxml.env.SimpleSCXMLListener;
import org.apache.scxml.model.EnterableState;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateFlowHandlerImpl extends StateFlowHandler {

    private static final Logger log = Logger.getLogger(StateFlowHandler.class.getName());
    private final List<CustomAction> customActions = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Class<? extends Invoker>> customInvokers = Collections.synchronizedMap(new HashMap<>());

    public static final String LOGICAL_FLOW_MAP = StateFlowHandlerImpl.class.getName() + ".LogicalFlowMap";

    private Boolean logstep;

    public StateFlowHandlerImpl(ServletContext ctx) {
        super();

        customInvokers.put("view", ViewInvoker.class);
        customInvokers.put("scxml", SubInvoker.class);

        Set<Class<?>> annotatedClasses = (Set<Class<?>>) ctx.getAttribute(ANNOTATED_CLASSES);
        for (Class<?> type : annotatedClasses) {

            if (type.isAnnotationPresent(StateChartAction.class)) {
                StateChartAction a = type.getAnnotation(StateChartAction.class);
                Class<?> javaClass = type;
                CustomAction action = new CustomAction(a.namespaceURI(), a.value(), (Class<? extends Action>) javaClass);
                customActions.add(action);
            } else if (type.isAnnotationPresent(StateChartInvoker.class)) {
                StateChartInvoker a = type.getAnnotation(StateChartInvoker.class);
                Class<?> javaClass = type;
                customInvokers.put(a.value(), (Class<Invoker>) javaClass);
            }
        }
    }

    private boolean isLogstep() {
        if (logstep == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc.getApplication().getProjectStage() == ProjectStage.Production) {
                logstep = Boolean.FALSE;
            } else {
                StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
                String pname = wcfg.getOptionValue(STATE_CHART_LOGSTEP_PARAM_NAME, "false");
                logstep = Boolean.parseBoolean(pname);
            }
        }
        return logstep;
    }

    @Override
    public SCXML createStateMachine(FacesContext context, String path, String id) throws ModelException {
        if (path == null) {
            throw new NullPointerException("Parametr path can not be null!");
        }
        SCXML stateFlow = createStateFlow(context, path, id);
        return stateFlow;
    }

    private SCXML createStateFlow(FacesContext context, String viewId, String id) throws ModelException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Creating StateFlow for: {0}", viewId);
        }

        UIViewRoot currnetViewRoot = context.getViewRoot();
        try {
            context.getAttributes().put(SKIP_START_STATE_MACHINE_HINT, true);
            context.getAttributes().put(BUILD_STATE_MACHINE_HINT, id);

            SCXML stateChart = null;

            ViewHandler vh = context.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, viewId);

            ViewMetadata viewMetadata = vdl.getViewMetadata(context, viewId);

            UIViewRoot view = viewMetadata.createMetadataView(context);

            UIComponent facet = view.getFacet(STATECHART_FACET_NAME);
            if (facet != null) {
                UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent(id);
                if (uichart != null) {
                    stateChart = uichart.getStateChart();
                }
            }

            return stateChart;
        } finally {
            context.getAttributes().remove(BUILD_STATE_MACHINE_HINT);
            context.getAttributes().remove(SKIP_START_STATE_MACHINE_HINT);

            context.setViewRoot(currnetViewRoot);
        }

    }

    @Override
    public List<CustomAction> getCustomActions() {
        return Collections.unmodifiableList(customActions);
    }

    @Override
    public Map<String, Class<? extends Invoker>> getCustomInvokers() {
        return Collections.unmodifiableMap(customInvokers);
    }

    @Override
    public SCXMLExecutor getCurrentExecutor(FacesContext context) {
        SCXMLExecutor executor = (SCXMLExecutor) context.getAttributes().get(CURRENT_EXECUTOR_HINT);
        if (executor == null) {
            executor = getRootExecutor(context);
        }
        return executor;
    }

    @Override
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

    @Override
    public boolean isActive(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return false;
        }
        Stack<SCXMLExecutor> stack = fs.getRoots();
        return stack != null && !stack.isEmpty();
    }

    private SCXMLExecutor newRootExecutor(FacesContext context, SCXML scxml) throws ModelException {

        StateFlowEvaluator evaluator = new StateFlowEvaluator();
        StateFlowDispatcher dispatcher = new StateFlowDispatcher();
        StateFlowErrorReporter errorReporter = new StateFlowErrorReporter();

        Map tags = (Map) scxml.getMetadata().get("faces-tag-info");
        errorReporter.getTags().putAll(new HashMap<>(tags));

        SCXMLExecutor executor = new SCXMLExecutor(evaluator, dispatcher, errorReporter);
        executor.setStateMachine(scxml);
        executor.addListener(scxml, new StateFlowCDIListener(executor));

        if (context.getApplication().getProjectStage() == ProjectStage.Production) {
            executor.setCheckLegalConfiguration(false);
        } else {
            executor.setCheckLegalConfiguration(true);
            if (isLogstep()) {
                executor.addListener(scxml, new SimpleSCXMLListener());
            }
        }

        executor.addListener(scxml, new AbstractSCXMLListener() {
            @Override
            public void onExit(EnterableState state) {
                if (!executor.isRunning()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    close(fc, executor);
                }
            }
        });

        for (Map.Entry<String, Class<? extends Invoker>> entry : customInvokers.entrySet()) {
            executor.registerInvokerClass(entry.getKey(), entry.getValue());
        }

        Context rootCtx = executor.getRootContext();
        rootCtx.setLocal("scxml_has_parent", false);

        return executor;
    }

    private SCXMLExecutor newSlaveExecutor(FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException {

        StateFlowErrorReporter errorReporter = (StateFlowErrorReporter) parent.getErrorReporter();

        Map tags = (Map) scxml.getMetadata().get("faces-tag-info");
        errorReporter.getTags().putAll(new HashMap<>(tags));

        SCXMLExecutor executor = new SCXMLExecutor(parent, invokeId, scxml);
        executor.addListener(scxml, new StateFlowCDIListener(executor));

        if (context.getApplication().getProjectStage() == ProjectStage.Production) {
            executor.setCheckLegalConfiguration(false);
        } else {
            executor.setCheckLegalConfiguration(true);
            if (isLogstep()) {
                executor.addListener(scxml, new SimpleSCXMLListener());
            }
        }

        executor.addListener(scxml, new AbstractSCXMLListener() {
            @Override
            public void onExit(EnterableState state) {
                if (!executor.isRunning()) {
                    FacesContext context = FacesContext.getCurrentInstance();
                    close(context, executor);
                }
            }
        });

        for (Map.Entry<String, Class<? extends Invoker>> entry : customInvokers.entrySet()) {
            executor.registerInvokerClass(entry.getKey(), entry.getValue());
        }

        if (parent != null) {
            Context rootCtx = executor.getRootContext();
            rootCtx.setLocal("scxml_has_parent", true);
        }

        return executor;
    }

    @Override
    public SCXMLExecutor execute(SCXML scxml, Map<String, Object> params) {
        return execute(null, null, scxml, params);
    }

    @Override
    public SCXMLExecutor execute(SCXMLExecutor parent, String invokeId, SCXML scxml, Map<String, Object> params) {
        try {
            boolean root = parent == null;
            FacesContext context = FacesContext.getCurrentInstance();

            FlowDeque fs = getFlowDeque(context, true);

            Stack<SCXMLExecutor> stack = fs.getRoots();

            SCXMLExecutor executor;
            if (root) {
                executor = newRootExecutor(context, scxml);
                stack.push(executor);
            } else {
                executor = newSlaveExecutor(context, parent, invokeId, scxml);
            }

            Context rootCtx = executor.getEvaluator().newContext(null);
            executor.setRootContext(rootCtx);

            StateFlowCDIHelper.executorEntered(executor);

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

            return executor;
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void close(FacesContext context, SCXMLExecutor executor) {
        FlowDeque fs = getFlowDeque(context, true);
        Stack<SCXMLExecutor> stack = fs.getRoots();

        if (!stack.isEmpty()) {

            if (executor == null) {
                executor = stack.get(0);
            }

            if (stack.contains(executor)) {
                SCXMLExecutor last = stack.pop();
                while (!stack.empty()) {
                    if (last == executor) {
                        break;
                    }
                    StateFlowCDIHelper.executorExited(last);
                    last = stack.peek();
                }

                if (stack.isEmpty()) {
                    closeFlowDeque(context);
                    if (CdiUtil.isCdiAvailable(context)) {
                        BeanManager bm = CdiUtil.getCdiBeanManager(context);
                        bm.fireEvent(new OnFinishEvent(executor));
                    }
                    return;
                }

                if (executor == null) {
                    executor = stack.get(0);
                }
            }
        }

        if (executor != null) {
            StateFlowCDIHelper.executorExited(executor);
        }

        if (stack.isEmpty()) {
            closeFlowDeque(context);
        }
    }

    private FlowDeque getFlowDeque(FacesContext context, boolean create) {

        FlowDeque result = (FlowDeque) context.getAttributes()
                .get(STATE_FLOW_STACK);

        if (result != null) {
            return result;
        }

        ExternalContext ec = context.getExternalContext();
        Map<String, Object> sessionMap = ec.getSessionMap();
        Map<String, Object> flowMap = (Map<String, Object>) sessionMap.get(LOGICAL_FLOW_MAP);
        if (flowMap == null && create) {
            flowMap = new HashMap<>();
            sessionMap.put(LOGICAL_FLOW_MAP, flowMap);
        }

        if (flowMap == null) {
            return null;
        }

        ClientWindow clientWindow = ec.getClientWindow();
        if (clientWindow == null) {
            if (create) {
                throw new IllegalStateException("Client Window mode not found");
            }
            return null;
        }

        if (clientWindow.isClientWindowRenderModeEnabled(context) || create) {
            if (!clientWindow.isClientWindowRenderModeEnabled(context)) {
                clientWindow.enableClientWindowRenderMode(context);
            }
            String sessionKey = clientWindow.getId() + "_stateFlowStack";
            result = (FlowDeque) flowMap.get(sessionKey);
            if (null == result && create) {
                result = new FlowDeque(sessionKey);
            }

//            Object state = flowMap.get(sessionKey);
//            if (null == state && create) {
//                result = new FlowDeque(sessionKey);
//            } else {
//                result = restoreFlowDequeState(context, state, sessionKey);
//            }
        }

        context.getAttributes().put(STATE_FLOW_STACK, result);

        return result;
    }

    private void closeFlowDeque(FacesContext context) {
        ExternalContext extContext = context.getExternalContext();
        ClientWindow clientWindow = extContext.getClientWindow();
        if (clientWindow != null && clientWindow.isClientWindowRenderModeEnabled(context)) {
            String sessionKey = clientWindow.getId() + "_stateFlowStack";
            Map<String, Object> sessionMap = extContext.getSessionMap();
            if (sessionMap.containsKey(sessionKey)) {
                sessionMap.remove(sessionKey);
            }
            clientWindow.disableClientWindowRenderMode(context);
        }

        context.getAttributes().put(STATE_FLOW_STACK, new FlowDeque(null));
    }

    @Override
    public void writeState(FacesContext context) {

        FlowDeque flowStack = getFlowDeque(context, false);
        if (flowStack == null) {
            return;
        }

        ExternalContext ec = context.getExternalContext();
        Map<String, Object> sessionMap = ec.getSessionMap();
        Map<String, Object> flowMap = (Map<String, Object>) sessionMap.get(LOGICAL_FLOW_MAP);
        if (flowMap == null) {
            flowMap = new HashMap<>();
            sessionMap.put(LOGICAL_FLOW_MAP, flowMap);
        }

        ClientWindow clientWindow = ec.getClientWindow();
        if (clientWindow == null) {
            throw new IllegalStateException("Client Window mode not found");
        }

        if (!clientWindow.isClientWindowRenderModeEnabled(context)) {
            clientWindow.enableClientWindowRenderMode(context);
        }
        String sessionKey = clientWindow.getId() + "_stateFlowStack";

        //Object state = saveFlowDequeState(context, flowStack);
        flowMap.put(sessionKey, flowStack);

    }

    private Object saveFlowDequeState(FacesContext context, FlowDeque flowDeque) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object states[] = new Object[2];

        Stack<SCXMLExecutor> executors = flowDeque.getRoots();
        //Stack<Integer> roots = flowDeque.getRoots();

        if (null != executors && executors.size() > 0) {
            Object[] attached = new Object[executors.size()];
            int i = 0;
            for (SCXMLExecutor executor : executors) {
                Object values[] = new Object[4];
                SCXML stateMachine = executor.getStateMachine();

//                values[0] = stateMachine.getViewId();
//                values[1] = stateMachine.getId();
//                values[2] = executor.getStateId();
                context.getAttributes().put(STATE_MACHINE_HINT, stateMachine);
                try {
                    //values[3] = executor.saveState(context);
                } finally {
                    context.getAttributes().remove(STATE_MACHINE_HINT);
                }

                attached[i++] = values;
            }
            states[0] = attached;
        }

//        if (null != roots && roots.size() > 0) {
//            Object[] attached = new Object[roots.size()];
//            int i = 0;
//            for (Integer root : roots) {
//                attached[i++] = root;
//            }
//            states[1] = attached;
//        }
        return states;
    }

    private FlowDeque restoreFlowDequeState(FacesContext context, Object state, String sessionKey) {
        FlowDeque result = new FlowDeque(sessionKey);

        if (null != state) {
            Object[] blocks = (Object[]) state;
            if (blocks[0] != null) {
                Object[] entries = (Object[]) blocks[0];
                for (Object entry : entries) {
                    Object[] values = (Object[]) entry;

                    String viewId = (String) values[0];
                    String id = (String) values[1];
                    String stateId = (String) values[2];

                    SCXML stateMachine = null;
                    try {
                        stateMachine = createStateFlow(context, viewId, id);
                    } catch (ModelException ex) {
                        throw new FacesException(ex);
                    }

                    if (stateMachine == null) {
                        throw new FacesException(String.format("Restored state flow %s in %s not found.", viewId, id));
                    }

                    SCXMLExecutor executor = null;//newStateFlowExecutor(null, context, stateId, stateMachine);

                    context.getAttributes().put(STATE_MACHINE_HINT, stateMachine);
                    try {
                        //executor.restoreState(context, values[3]);
                    } finally {
                        context.getAttributes().remove(STATE_MACHINE_HINT);
                    }

                    result.getRoots().add(executor);
                }
            }

//            if (blocks[1] != null) {
//                Object[] entries = (Object[]) blocks[1];
//                for (Object entry : entries) {
//                    result.getRoots().add((Integer) entry);
//                }
//            }
        }

        return result;
    }

    private static class FlowDeque implements Serializable {

        private final Stack<SCXMLExecutor> executors;
        private final String key;

        public FlowDeque(final String sessionKey) {
            executors = new Stack<>();
            this.key = sessionKey;
        }

        public String getKey() {
            return key;
        }

        public Stack<SCXMLExecutor> getRoots() {
            return executors;
        }

    }

}
