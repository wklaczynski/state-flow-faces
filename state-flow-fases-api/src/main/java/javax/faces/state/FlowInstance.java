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
package javax.faces.state;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import javax.el.ELContext;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import static javax.faces.component.UIComponentBase.restoreAttachedState;
import static javax.faces.component.UIComponentBase.saveAttachedState;
import javax.faces.context.FacesContext;
import javax.faces.state.annotation.Statefull;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.invoke.InvokerException;
import javax.faces.state.model.Action;
import javax.faces.state.model.Datamodel;
import javax.faces.state.model.History;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import static javax.faces.state.model.StateChart.STATE_MACHINE_HINT;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.utils.StateFlowHelper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class FlowInstance extends ELContext {

    public static final String CURRENT_STACK_KEY = "javax.faces.state.CURRENT_STACK";
    public static final String FLOW_EL_CONTEXT_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();
    public static final String FLOW_ISTANCE_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();

    /**
     * The notification registry.
     */
    private FlowNotificationRegistry notificationRegistry;

    /**
     * The <code>Map</code> of <code>Context</code>s per
     * <code>TransitionTarget</code>.
     */
    private final Map<TransitionTarget, FlowContext> contexts;

    /**
     * The <code>Map</code> of last known configurations per
     * <code>History</code>.
     */
    private final Map<History, Set<TransitionTarget>> histories;

    /**
     * <code>Map</code> for recording the processInvoker to completion status of
     * composite states.
     */
    private final Map<TransitionTarget, Boolean> completions;

    /**
     * The <code>Invoker</code> classes <code>Map</code>, keyed by
     * &lt;invoke&gt; target types (specified using "targettype" attribute).
     */
    private final Map<String, Class> invokerClasses;

    /**
     * The <code>Map</code> of active <code>Invoker</code>s, keyed by (leaf)
     * <code>State</code>s.
     */
    private final Map<TransitionTarget, Invoker> invokers;

    /**
     * The evaluator for expressions.
     */
    private FlowEvaluator evaluator;

    /**
     * The root context.
     */
    private FlowContext rootContext;

    /**
     * The owning state machine executor.
     */
    private final StateFlowExecutor executor;

    /**
     * The root FacesContext.
     */
    private final FacesContext facesContext;

    /**
     * Constructor.
     *
     * @param executor The executor that this instance is attached to.
     * @param facesContext
     */
    public FlowInstance(final StateFlowExecutor executor, FacesContext facesContext) {
        this.notificationRegistry = new FlowNotificationRegistry();
        this.contexts = Collections.synchronizedMap(new HashMap());
        this.histories = Collections.synchronizedMap(new HashMap());
        this.invokerClasses = Collections.synchronizedMap(new HashMap());
        this.invokers = Collections.synchronizedMap(new HashMap());
        this.completions = Collections.synchronizedMap(new HashMap());
        this.evaluator = null;
        this.rootContext = null;
        this.executor = executor;
        this.facesContext = facesContext;
    }

    /**
     * Get the <code>FacesContext</code>.
     *
     * @return The FacesContext.
     */
    public FacesContext getFacesContext() {
        return facesContext;
    }

    /**
     * Get the <code>Evaluator</code>.
     *
     * @return The evaluator.
     */
    public FlowEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Set the <code>Evaluator</code>.
     *
     * @param evaluator The evaluator.
     */
    public void setEvaluator(final FlowEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Get the root context.
     *
     * @return The root context.
     */
    public FlowContext getRootContext() {
        if (rootContext == null && evaluator != null) {
            rootContext = evaluator.newContext(null, null);
        }
        return rootContext;
    }

    /**
     * Set the root context.
     *
     * @param context The root context.
     */
    void setRootContext(final FlowContext context) {
        this.rootContext = context;
    }

    /**
     * Get the notification registry.
     *
     * @return The notification registry.
     */
    public FlowNotificationRegistry getNotificationRegistry() {
        return notificationRegistry;
    }

    /**
     * Set the notification registry.
     *
     * @param notifRegistry The notification registry.
     */
    void setNotificationRegistry(final FlowNotificationRegistry notifRegistry) {
        this.notificationRegistry = notifRegistry;
    }

    /**
     * Get the <code>Context</code> for this <code>TransitionTarget</code>. If
     * one is not available it is created.
     *
     * @param transitionTarget The TransitionTarget.
     * @return The Context.
     */
    public FlowContext getContext(final TransitionTarget transitionTarget) {
        FlowContext context = contexts.get(transitionTarget);
        if (context == null) {
            TransitionTarget parent = transitionTarget.getParent();
            if (parent == null) {
                // docroot
                context = evaluator.newContext(transitionTarget, getRootContext());
            } else {
                context = evaluator.newContext(transitionTarget, getContext(parent));
            }
            Datamodel datamodel = transitionTarget.getDatamodel();
            StateFlowHelper.cloneDatamodel(datamodel, context, evaluator);
            contexts.put(transitionTarget, context);
        }
        return context;
    }

    /**
     * Get the <code>Context</code> for this <code>TransitionTarget</code>. May
     * return <code>null</code>.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The Context.
     */
    FlowContext lookupContext(final TransitionTarget transitionTarget) {
        return (FlowContext) contexts.get(transitionTarget);
    }

    /**
     * Set the <code>Context</code> for this <code>TransitionTarget</code>.
     *
     * @param transitionTarget The TransitionTarget.
     * @param context The Context.
     */
    void setContext(final TransitionTarget transitionTarget, final FlowContext context) {
        contexts.put(transitionTarget, context);
    }

    /**
     * Get the last configuration for this history.
     *
     * @param history The history.
     * @return Returns the lastConfiguration.
     */
    public Set<TransitionTarget> getLastConfiguration(final History history) {
        Set<TransitionTarget> lastConfiguration = (Set) histories.get(history);
        if (lastConfiguration == null) {
            lastConfiguration = new HashSet();
            histories.put(history, lastConfiguration);
        }
        return lastConfiguration;
    }

    /**
     * Set the last configuration for this history.
     *
     * @param history The history.
     * @param lc The lastConfiguration to set.
     */
    public void setLastConfiguration(final History history, final Set lc) {
        Set<TransitionTarget> lastConfiguration = getLastConfiguration(history);
        lastConfiguration.clear();
        lastConfiguration.addAll(lc);
    }

    /**
     * Check whether we have prior history.
     *
     * @param history The history.
     * @return Whether we have a non-empty last configuration
     */
    public boolean isEmpty(final History history) {
        Set<TransitionTarget> lastConfiguration = (Set) histories.get(history);
        return lastConfiguration == null || lastConfiguration.isEmpty();
    }

    /**
     * Resets the history state.
     *
     * @param history The history.
     * @see org.apache.commons.scxml.SCXMLExecutor#reset()
     */
    public void reset(final History history) {
        Set lastConfiguration = (Set) histories.get(history);
        if (lastConfiguration != null) {
            lastConfiguration.clear();
        }
    }

    /**
     * Get the {@link SCXMLExecutor} this instance is attached to.
     *
     * @return The SCXMLExecutor this instance is attached to.
     * @see org.apache.commons.scxml.SCXMLExecutor
     */
    public StateFlowExecutor getExecutor() {
        return executor;
    }

    /**
     * Register an {@link Invoker} class for this target type.
     *
     * @param targettype The target type (specified by "targettype" attribute of
     * &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    void registerInvokerClass(final String targettype, final Class invokerClass) {
        invokerClasses.put(targettype, invokerClass);
    }

    /**
     * Remove the {@link Invoker} class registered for this target type (if
     * there is one registered).
     *
     * @param targettype The target type (specified by "targettype" attribute of
     * &lt;invoke&gt; tag).
     */
    void unregisterInvokerClass(final String targettype) {
        invokerClasses.remove(targettype);
    }

    /**
     * Get the {@link Invoker} for this {@link TransitionTarget}. May return
     * <code>null</code>. A non-null <code>Invoker</code> will be returned if
     * and only if the <code>TransitionTarget</code> is currently active and
     * contains an &lt;invoke&gt; child.
     *
     * @param invoke
     * @param target
     * @return An {@link Invoker} for the specified type, if an invoker class is
     * registered against that type, <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot be
     * instantiated.
     */
    public Invoker newInvoker(final Invoke invoke, TransitionTarget target) throws InvokerException {
        String targettype = invoke.getTargettype();

        Class invokerClass = (Class) invokerClasses.get(targettype);
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for "
                    + "targettype \"" + targettype + "\"");
        }
        Invoker invoker = null;
        try {
            invoker = (Invoker) invokerClass.newInstance();

            invoker.setParentStateId(target.getId());
            invoker.setType(targettype);
            invoker.setInstance(this);

            if (invoker instanceof PathResolverHolder) {
                PathResolver pr = current(PathResolver.class);
                PathResolverHolder ph = (PathResolverHolder) invoker;
                ph.setPathResolver(pr);
            }

            postNewInvoker(invoke, invoker);

        } catch (InstantiationException | IllegalAccessException | IOException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        }
        return invoker;
    }

    public <V> V process(final State target, final Invoker invoker, final Callable<V> fn) throws InvokerException {
        Invoke invoke = target.getInvoke();
        try {
            return processInvoker(target, invoke, invoker, fn);
        } catch (Throwable th) {
            throw new InvokerException(th);
        }
    }

    protected abstract <V> V processInvoker(final State target, final Invoke invoke, final Invoker invoker, final Callable<V> fn) throws Exception;

    protected abstract void postNewInvoker(final Invoke invoke, final Invoker invoker) throws IOException;

    public <V> V execute(Action action, final Callable<V> fn) throws ModelException, FlowExpressionException {
        try {
            return processExecute(action, fn);
        } catch (ModelException | FlowExpressionException th) {
            throw th;
        } catch (Throwable th) {
            throw new FlowExpressionException(th);
        }
    }

    protected abstract <V> V processExecute(Action action, final Callable<V> fn) throws Exception;

    /**
     * Get the {@link Invoker} for this {@link TransitionTarget}. May return
     * <code>null</code>. A non-null {@link Invoker} will be returned if and
     * only if the {@link TransitionTarget} is currently active and contains an
     * &lt;invoke&gt; child.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The Invoker.
     */
    public Invoker getInvoker(final TransitionTarget transitionTarget) {
        return (Invoker) invokers.get(transitionTarget);
    }

    /**
     * Set the {@link Invoker} for this {@link TransitionTarget}.
     *
     * @param invoke
     * @param transitionTarget The TransitionTarget.
     * @param invoker The Invoker.
     */
    public void setInvoker(final TransitionTarget transitionTarget, final Invoke invoke, final Invoker invoker) {
        invokers.put(transitionTarget, invoker);
    }

    /**
     * Return the Map of {@link Invoker}s currently "active".
     *
     * @return The map of invokers.
     */
    public Map<TransitionTarget, Invoker> getInvokers() {
        return invokers;
    }

    /**
     * Get the completion status for this composite {@link TransitionTarget}.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @return The completion status.
     *
     * @since 0.7
     */
    public boolean isDone(final TransitionTarget transitionTarget) {
        Boolean done = completions.get(transitionTarget);
        if (done == null) {
            return false;
        } else {
            return done;
        }
    }

    /**
     * Set the completion status for this composite {@link TransitionTarget}.
     *
     * @param transitionTarget The TransitionTarget.
     * @param done The completion status.
     *
     * @since 0.7
     */
    public void setDone(final TransitionTarget transitionTarget, final boolean done) {
        completions.put(transitionTarget, done ? Boolean.TRUE : Boolean.FALSE);
    }

    public abstract void setVariableMapper(VariableMapper varMapper);

    public abstract void setFunctionMapper(FunctionMapper fnMapper);
    
    public abstract Object getAttribute(String name);
    
    public abstract void setAttribute(String name, Object value);
    
    
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[5];

        if (rootContext != null) {
            values[0] = rootContext.saveState(context);
        }

        values[1] = saveContextsState(context);
        values[2] = saveHistoriesState(context);
        values[3] = saveCompletionsState(context);
        values[4] = saveInvokersState(context);

        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        FlowContext rctx = getRootContext();
        rctx.restoreState(context, values[0]);

        restoreContextsState(context, values[1]);
        restoreHistoriesState(context, values[2]);
        restoreCompletionsState(context, values[3]);
        restoreInvokersState(context, values[4]);

    }

    private Object saveContextsState(FacesContext context) {
        Object state = null;
        if (null != contexts && contexts.size() > 0) {
            Object[] attached = new Object[contexts.size()];
            int i = 0;
            for (Map.Entry<TransitionTarget, FlowContext> entry : contexts.entrySet()) {
                Object values[] = new Object[2];
                values[0] = entry.getKey().getClientId();
                values[1] = entry.getValue().saveState(context);
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreContextsState(FacesContext context, Object state) {
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);
        contexts.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String ttid = (String) entry[0];
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                TransitionTarget target = (TransitionTarget) found;
                FlowContext tctx = getContext(target);
                tctx.restoreState(context, entry[1]);

            }
        }
    }

    private Object saveHistoriesState(FacesContext context) {
        Object state = null;
        if (null != histories && histories.size() > 0) {
            Object[] attached = new Object[histories.size()];
            int i = 0;
            for (Map.Entry<History, Set<TransitionTarget>> entry : histories.entrySet()) {
                Object values[] = new Object[2];
                values[0] = entry.getKey().getClientId();
                values[1] = saveTargetsState(context, entry.getValue());
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreHistoriesState(FacesContext context, Object state) {
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);
        histories.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String ttid = (String) entry[0];
                Object found = chart.findElement(ttid);

                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                History history = (History) found;

                Set<TransitionTarget> last = (Set) histories.get(history);
                if (last == null) {
                    last = new HashSet();
                    histories.put(history, last);
                }
                restoreTargetsState(context, chart, last, entry[1]);
            }
        }
    }

    private Object saveTargetsState(FacesContext context, Collection<TransitionTarget> targets) {
        Object state = null;
        if (null != targets && targets.size() > 0) {
            Object[] attached = new Object[targets.size()];
            int i = 0;
            for (TransitionTarget target : targets) {
                attached[i++] = target.getClientId();
            }
            state = attached;
        }
        return state;
    }

    private void restoreTargetsState(FacesContext context, StateChart chart, Set<TransitionTarget> targets, Object state) {
        targets.clear();
        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                String ttid = (String) value;
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                TransitionTarget tt = (TransitionTarget) found;
                targets.add(tt);
            }
        }
    }

    private Object saveCompletionsState(FacesContext context) {
        Object state = null;
        if (null != completions && completions.size() > 0) {
            Object[] attached = new Object[completions.size()];
            int i = 0;
            for (Map.Entry<TransitionTarget, Boolean> entry : completions.entrySet()) {
                Object values[] = new Object[2];
                values[0] = entry.getKey().getClientId();
                values[1] = entry.getValue();
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreCompletionsState(FacesContext context, Object state) {
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);
        completions.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String ttid = (String) entry[0];
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                TransitionTarget tt = (TransitionTarget) found;
                completions.put(tt, (Boolean) entry[1]);
            }
        }
    }

    private Object saveInvokersState(FacesContext context) {
        Object state = null;
        if (null != invokers && invokers.size() > 0) {
            Object[] attached = new Object[invokers.size()];
            int i = 0;
            for (Map.Entry<TransitionTarget, Invoker> entry : invokers.entrySet()) {
                Object values[] = new Object[3];
                Invoker invoker = entry.getValue();
                State tt = (State) entry.getKey();

                values[0] = entry.getKey().getClientId();
                values[1] = tt.getInvoke().getTargettype();
                values[2] = invoker.saveState(context);
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreInvokersState(FacesContext context, Object state) {
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);
        invokers.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String ttid = (String) entry[0];
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                TransitionTarget tt = (TransitionTarget) found;
                String type = (String) entry[1];

                State stt = (State) tt;
                Invoke invoke = stt.getInvoke();
                if (!invoke.getTargettype().equals(type)) {
                    throw new IllegalStateException(String.format("Bad invoker type %s in failed.", type, ttid));
                }

                Invoker invoker = null;
                try {

                    PathResolver pr = invoke.getPathResolver();

                    invoker = newInvoker(invoke, tt);

                    invoker.restoreState(context, entry[2]);
                } catch (InvokerException ex) {
                    throw new IllegalStateException(String.format("Restored invoker %s failed.", ttid), ex);
                } finally {

                }

                invokers.put(tt, invoker);
            }
        }
    }

    public static FlowInstance current() {
        return current(FlowInstance.class);
    }

    public static <T> T current(Class<T> type) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> elStack = getObjectStack(type, contextAttributes);
        return elStack.peek();
    }

    public static <T> void push(Class<T> type, T component) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> elStack = getObjectStack(type, contextAttributes);
        elStack.push(component);
    }

    public static <T> void pop(Class<T> type, T component) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> elStack = getObjectStack(type, contextAttributes);

        for (T topComponent = elStack.peek(); topComponent != component; topComponent = elStack.peek()) {
            pop(type, topComponent);
        }

        elStack.pop();
    }

    private static <T> ArrayDeque<T> getObjectStack(Class<T> type, Map<Object, Object> contextAttributes) {
        String keyName = CURRENT_STACK_KEY + ":" + type.getName();
        ArrayDeque<T> elStack = (ArrayDeque<T>) contextAttributes.get(keyName);

        if (elStack == null) {
            elStack = new ArrayDeque<>();
            contextAttributes.put(keyName, elStack);
        }
        return elStack;
    }

    public static Object saveStatefullState(FacesContext context, Class<?> clazz, Object instance) {
        if (clazz == null) {
            return null;
        }

        Object results[] = new Object[2];

        Field[] fields = clazz.getDeclaredFields();
        Object[] attached = new Object[fields.length];
        int i = 0;
        for (Field field : fields) {
            Object entry[] = null;
            if (field.isAnnotationPresent(Statefull.class)) {
                entry = new Object[2];
                Object value = null;
                boolean accessibility = field.isAccessible();
                try {
                    field.setAccessible(true);
                    value = field.get(instance);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalStateException(String.format("Save statefull field %s error.", field.getName()));
                } finally {
                    field.setAccessible(accessibility);
                }
                entry[0] = field.getName();
                entry[1] = saveValueState(context, field.getName(), value);
            }
            attached[i++] = entry;
        }
        results[0] = attached;
        results[1] = saveStatefullState(context, clazz.getSuperclass(), instance);

        return results;
    }

    public static void restoreStatefullState(FacesContext context, Object state, Class<?> clazz, Object instance) {
        if (clazz == null || null == state) {
            return;
        }

        Object[] states = (Object[]) state;

        Field[] fields = clazz.getDeclaredFields();
        Object[] attached = (Object[]) states[0];
        int i = 0;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Statefull.class)) {
                Object[] entry = (Object[]) attached[i];
                Object value = restoreValueState(context, field.getName(), entry[1]);
                boolean accessibility = field.isAccessible();
                try {
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalStateException(String.format("Restored statefull field %s error.", field.getName()));
                } finally {
                    field.setAccessible(accessibility);
                }
            }
            i++;
        }
        restoreStatefullState(context, state, clazz.getSuperclass(), instance);

    }

    public static Object saveValueState(FacesContext context, String name, Object value) {
        if (value == null) {
            return null;
        }
        value = saveAttachedState(context, value);
        return value;
    }

    public static Object restoreValueState(FacesContext context, String name, Object state) {
        if (state == null) {
            return null;
        }
        Object value = restoreAttachedState(context, state);
        return value;
    }

}
