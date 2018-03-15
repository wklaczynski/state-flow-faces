/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.impl;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;
import com.sun.faces.util.RequestStateManager;
import static com.sun.faces.util.RequestStateManager.FACELET_FACTORY;
import org.ssoft.faces.state.cdi.StateFlowCDIListener;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.inject.spi.BeanManager;
import static javax.faces.application.StateManager.IS_BUILDING_INITIAL_STATE;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowStatus;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.events.FlowOnFinalEvent;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.model.CustomAction;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.Facelet;
import javax.servlet.ServletContext;
import static org.ssoft.faces.state.FlowConstants.ANNOTATED_CLASSES;
import org.ssoft.faces.state.cdi.StateChartScopeCDIContex;
import org.ssoft.faces.state.invokers.SubInvoker;
import org.ssoft.faces.state.invokers.ViewInvoker;
import org.ssoft.faces.state.utils.AsyncTrigger;
import org.ssoft.faces.state.utils.Util;
import javax.faces.state.annotation.FlowAction;
import javax.faces.state.annotation.FlowInvoker;
import javax.faces.state.component.UIStateChartRoot;
import org.ssoft.faces.state.StateFlowExecutorImpl;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateFlowHandlerImpl extends StateFlowHandler {

    private static final Logger log = Logger.getLogger(StateFlowHandler.class.getName());
    private List<CustomAction> customActions = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Class<?>> customInvokers = Collections.synchronizedMap(new HashMap<>());
    private final ServletContext ctx;

    public StateFlowHandlerImpl(ServletContext ctx) {
        super();
        this.ctx = ctx;

        customInvokers.put("view", ViewInvoker.class);
        customInvokers.put("scxml", SubInvoker.class);

        Set<Class<?>> annotatedClasses = (Set<Class<?>>) ctx.getAttribute(ANNOTATED_CLASSES);
        for (Class<?> type : annotatedClasses) {

            if (type.isAnnotationPresent(FlowAction.class)) {
                FlowAction a = type.getAnnotation(FlowAction.class);
                Class<?> javaClass = type;
                CustomAction action = new CustomAction(a.namespaceURI(), a.value(), javaClass);
                customActions.add(action);
            } else if (type.isAnnotationPresent(FlowInvoker.class)) {
                FlowInvoker a = type.getAnnotation(FlowInvoker.class);
                Class<?> javaClass = type;
                customInvokers.put(a.value(), (Class<Invoker>) javaClass);
            }
        }

    }

    @Override
    public StateChart createStateMachine(FacesContext context, String path) throws ModelException {
        if (path == null) {
            throw new NullPointerException("Parametr path can not be null!");
        }
        StateChart stateFlow = createStateFlow(context, path);
        return stateFlow;
    }

    private StateChart createStateFlow(FacesContext context, String path) throws ModelException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Creating StateFlow for: {0}", path);
        }
        StateChart stateFlow = null;
        if (path.endsWith(".scxml")) {
            stateFlow = createStateMachineFromScxml(context, path);
        } else {
            throw new ModelException(String.format("Unknow state machine format %s.", path));
        }
        return stateFlow;

    }

    public List<CustomAction> getCustomActions() {
        return customActions;
    }

    public void setCustomActions(List<CustomAction> customActions) {
        this.customActions = customActions;
    }

    public Map<String, Class<?>> getCustomInvokers() {
        return customInvokers;
    }

    @Override
    public StateFlowExecutor getExecutor(FacesContext context, StateFlowExecutor parent) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, false);
        if (fs == null) {
            return null;
        }

        Stack<StateFlowExecutor> stack = fs.getExecutors();

        if (stack.isEmpty()) {
            return null;
        }

        StateFlowExecutor result = stack.peek();

        if (parent == null) {
            return result;
        }

        for (int i = stack.size() - 1; i > 0; i--) {
            if (stack.get(i - 1) == parent) {
                result = stack.get(i);
                break;
            }
        }

        return result;
    }

    @Override
    public StateFlowExecutor getRootExecutor(FacesContext context) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, false);
        if (fs == null) {
            return null;
        }

        Stack<StateFlowExecutor> stack = fs.getExecutors();
        Stack<Integer> roots = fs.getRoots();

        if (!roots.isEmpty()) {
            Integer id = roots.peek();
            return stack.get(id);
        } else {
            return null;
        }
    }

    public Stack<StateFlowExecutor> getExecutorStack(FacesContext context) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, false);
        if (fs == null) {
            return null;
        }

        Stack<StateFlowExecutor> stack = fs.getExecutors();
        return stack;
    }

    public void pushExecutor(FacesContext context, StateFlowExecutor executor) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, true);
        Stack<StateFlowExecutor> stack = fs.getExecutors();
        Stack<Integer> roots = fs.getRoots();
        if (stack.isEmpty()) {
            roots.push(0);
        }
        stack.push(executor);
    }

    public void popExecutor(FacesContext context) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, false);
        if (fs == null) {
            return;
        }

        Stack<StateFlowExecutor> stack = fs.getExecutors();
        Stack<Integer> roots = fs.getRoots();

        int id = stack.size() - 1;
        while (roots.peek() > id) {
            roots.pop();
        }
        stack.pop();
        if (stack.isEmpty()) {
            roots.clear();
        }
    }

    @Override
    public boolean isActive(FacesContext context) {
        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, false);
        if (fs == null) {
            return false;
        }

        Stack<StateFlowExecutor> stack = fs.getExecutors();
        return stack != null && !stack.isEmpty();
    }

    @Override
    public StateFlowExecutor startExecutor(FacesContext context, StateChart stateMachine, Map params, boolean root) {
        try {
            FlowDeque<StateFlowExecutor> fs = getFlowStack(context, true);
            Stack<StateFlowExecutor> stack = fs.getExecutors();
            Stack<Integer> roots = fs.getRoots();

            StateFlowExecutor parent = getExecutor(context);

            StateFlowExecutor executor;

            executor = new StateFlowExecutorImpl();
            FlowContext rootCtx = executor.getEvaluator().newContext(null, null);
            if (params != null) {
                for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    rootCtx.setLocal((String) entry.getKey(), entry.getValue());
                }
            }
            if (parent != null) {
                FlowContext parentCtx = parent.getRootContext();
                rootCtx.setLocal("flow_has_parent", true);
            }

            executor.setRootContext(rootCtx);
            executor.setStateMachine(stateMachine);
            executor.addListener(stateMachine, new StateFlowCDIListener());

            for (Map.Entry<String, Class<?>> entry : customInvokers.entrySet()) {
                executor.registerInvokerClass(entry.getKey(), entry.getValue());
            }
            pushExecutor(context, executor);
            if (root) {
                int id = stack.size() - 1;
                if (roots.peek() != id) {
                    roots.push(id);
                }
                StateChartScopeCDIContex.flowExecutorEntered(executor);
            }

            try {
                executor.go();
            } catch (ModelException me) {
            }
            if (executor.getCurrentStatus().isFinal()) {
                stopExecutor(context, parent);
                executor = null;
            }
            return executor;
        } catch (Throwable ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public void stopExecutor(FacesContext context, StateFlowExecutor to) {
        if (!isActive(context)) {
            throw new IllegalStateException("Instance SCXML has not yet been started");
        }

        boolean chroot = false;

        FlowDeque<StateFlowExecutor> fs = getFlowStack(context, true);
        Stack<StateFlowExecutor> stack = fs.getExecutors();
        Stack<Integer> roots = fs.getRoots();

        StateFlowExecutor executor = stack.pop();
        StateFlowExecutor parent = null;

        while (!stack.empty()) {
            parent = stack.peek();
            if (parent == to) {
                break;
            }
            executor = stack.pop();
            parent = null;
        }

        int id = stack.size() - 1;
        while (roots.peek() > id) {
            roots.pop();
            chroot = true;
        }

        if (parent == null) {
            closeFlowStack(context);
            FacesContext fc = FacesContext.getCurrentInstance();
            if (Util.isCdiAvailable(fc)) {
                BeanManager bm = Util.getCdiBeanManager(fc);
                bm.fireEvent(new FlowOnFinalEvent());
            }

        } else if (!chroot) {

            AsyncTrigger trigger = new AsyncTrigger(parent);

            FlowContext fctx = executor.getRootContext();
            FlowStatus pstatus = parent.getCurrentStatus();
            for (State pstate : pstatus.getStates()) {
                String eventPrefix = pstate.getId() + ".invoke.";

                boolean stop = false;
                FlowStatus status = executor.getCurrentStatus();
                for (State state : status.getStates()) {
                    if (state.isFinal()) {
                        FlowTriggerEvent te = new FlowTriggerEvent(eventPrefix + state.getId(), FlowTriggerEvent.SIGNAL_EVENT);
                        trigger.add(te);
                        stop = true;
                    }
                }
                if (!stop) {
                    FlowTriggerEvent te = new FlowTriggerEvent(eventPrefix + "close", FlowTriggerEvent.SIGNAL_EVENT);
                    trigger.add(te);
                }
                FlowTriggerEvent te = new FlowTriggerEvent(eventPrefix + "done", FlowTriggerEvent.SIGNAL_EVENT);
                trigger.add(te);
            }

            trigger.start();
        }

        StateChartScopeCDIContex.flowExecutorExited(executor);
    }

    static FlowDeque<StateFlowExecutor> getFlowStack(FacesContext context, boolean create) {
        FlowDeque<StateFlowExecutor> result = null;
        ExternalContext extContext = context.getExternalContext();
        ClientWindow clientWindow = extContext.getClientWindow();
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
            Map<String, Object> sessionMap = extContext.getSessionMap();
            result = (FlowDeque<StateFlowExecutor>) sessionMap.get(sessionKey);
            if (null == result && create) {
                result = new FlowDeque<>(sessionKey);
                sessionMap.put(sessionKey, result);
            }
        }

        return result;
    }

    static void closeFlowStack(FacesContext context) {
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
    }

    static class FlowDeque<E> implements Serializable {

        private final Stack<StateFlowExecutor> executors;
        private final Stack<Integer> roots;
        private final String sessionKey;

        public FlowDeque(final String sessionKey) {
            executors = new Stack<>();
            roots = new Stack<>();
            this.sessionKey = sessionKey;
        }

        public String getSessionKey() {
            return sessionKey;
        }

        public Stack<StateFlowExecutor> getExecutors() {
            return executors;
        }

        public Stack<Integer> getRoots() {
            return roots;
        }

    }

    private DefaultFaceletFactory faceletFactory;

    private StateChart createStateMachineFromScxml(FacesContext ctx, String path) throws ModelException {
        try {
            StateChart stateChart = null;

            UIViewRoot view = new UIViewRoot();
            view.setViewId(path);
            
            if (faceletFactory == null) {
                ApplicationAssociate associate = ApplicationAssociate.getInstance(ctx.getExternalContext());
                faceletFactory = associate.getFaceletFactory();
                assert (faceletFactory != null);
            }
            RequestStateManager.set(ctx, FACELET_FACTORY, faceletFactory);
            Facelet facelet = faceletFactory.getFacelet(ctx, view.getViewId());

            // populate UIViewRoot
            try {
                ctx.getAttributes().put(IS_BUILDING_INITIAL_STATE, Boolean.TRUE);
                facelet.apply(ctx, view);
                
                UIComponent facet = view.getFacet(StateChart.STATECHART_FACET_NAME);
                if(facet != null) {
                    UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent("main");
                    if(uichart != null) {
                        stateChart = uichart.getStateChart();                   
                    }
                }
            } finally {
                ctx.getAttributes().remove(IS_BUILDING_INITIAL_STATE);
            }

            return stateChart;
        } catch (IOException ex) {
            throw new ModelException(ex);
        }

    }

}
