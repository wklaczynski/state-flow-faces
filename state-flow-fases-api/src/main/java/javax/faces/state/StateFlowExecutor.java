/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Datamodel;
import javax.faces.state.model.History;
import javax.faces.state.model.State;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.utils.StateFlowHelper;
import javax.faces.state.semantics.StateChartSemantics;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowExecutor {

    /**
     * The Logger for the StateFlowExecutor.
     */
    protected static final Logger log = Logger.getLogger(StateFlowExecutor.class.getName());

    protected static final String EVENT_DATA = "_eventdata";

    public final static StateFlowExecutor getIstance() {
        return new StateFlowExecutor();
    }

    /**
     * The special variable for storing event data / payload, when multiple
     * events are triggered, keyed by event name.
     */
    protected static final String EVENT_DATA_MAP = "_eventdatamap";

    /**
     * SCXMLExecutor put into motion without setting a model (state machine).
     */
    protected static final String ERR_NO_STATE_MACHINE = "StateFlowExecutor: State machine not set";

    /**
     * The stateMachine being executed.
     */
    protected StateChart stateMachine;

    /**
     * The current status of the stateMachine.
     */
    protected FlowStatus currentStatus;

    /**
     * Interpretation semantics.
     */
    protected StateChartSemantics semantics;

    /**
     * The FlowInstance.
     */
    protected FlowInstance flowInstance;

    /**
     * The event dispatcher to interface with external documents etc.
     */
    protected FlowEventDispatcher eventdispatcher;

    /**
     * The environment specific error reporter.
     */
    protected FlowErrorReporter errorReporter = null;

    /**
     * Run-to-completion.
     */
    protected boolean superStep = true;

    /**
     * The worker method. Re-evaluates current status whenever any events are
     * triggered.
     *
     * @param evts an array of external events which triggered during the last
     * time quantum
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    public synchronized void triggerEvents(final FlowTriggerEvent[] evts) throws ModelException {
        // Set event data, saving old values
        Object[] oldData = setEventData(evts);

        // Forward events (external only) to any existing invokes,
        // and finalize processing
        semantics.processInvokes(evts, errorReporter, flowInstance);

        List evs = new ArrayList(Arrays.asList(evts));
        FlowStep step = null;

        do {
            // CreateStep
            step = new FlowStep(evs, currentStatus);
            // EnumerateReachableTransitions
            semantics.enumerateReachableTransitions(stateMachine, step, errorReporter);
            // FilterTransitionSet
            semantics.filterTransitionsSet(step, eventdispatcher, errorReporter, flowInstance);
            // FollowTransitions
            semantics.followTransitions(step, errorReporter, flowInstance);
            // UpdateHistoryStates
            semantics.updateHistoryStates(step, errorReporter, flowInstance);
            // ExecuteActions
            semantics.executeActions(step, stateMachine, eventdispatcher, errorReporter, flowInstance);
            // AssignCurrentStatus
            updateStatus(step);
            // ***Cleanup external events if superStep
            if (superStep) {
                evs.clear();
            }
        } while (superStep && currentStatus.getEvents().size() > 0);

        // InitiateInvokes only after state machine has stabilized
        semantics.initiateInvokes(step, errorReporter, flowInstance);

        // Restore event data
        restoreEventData(oldData);
        logState();
    }

    /**
     * Convenience method when only one event needs to be triggered.
     *
     * @param evt the external events which triggered during the last time
     * quantum
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    public void triggerEvent(final FlowTriggerEvent evt)
            throws ModelException {
        triggerEvents(new FlowTriggerEvent[]{evt});
    }

    /**
     * Clear all state and begin from &quot;initialstate&quot; indicated on root
     * flow element.
     *
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    public synchronized void reset() throws ModelException {
        // Reset all variable contexts
        FlowContext rootCtx = flowInstance.getRootContext();
        // Clone root datamodel
        if (stateMachine == null) {
            throw new ModelException(ERR_NO_STATE_MACHINE);
        } else {
            Datamodel rootdm = stateMachine.getDatamodel();
            StateFlowHelper.cloneDatamodel(rootdm, rootCtx, flowInstance.getEvaluator());
        }
        // all states and parallels, only states have variable contexts
        for (TransitionTarget tt : stateMachine.getTargets().values()) {
            if (tt instanceof State) {
                FlowContext context = flowInstance.lookupContext(tt);
                if (context != null) {
                    context.reset();
                    Datamodel dm = tt.getDatamodel();
                    if (dm != null) {
                        StateFlowHelper.cloneDatamodel(dm, context, flowInstance.getEvaluator());
                    }
                }
            } else if (tt instanceof History) {
                flowInstance.reset((History) tt);
            }
        }
        // CreateEmptyStatus
        currentStatus = new FlowStatus();
        FlowStep step = new FlowStep(null, currentStatus);
        // DetermineInitialStates
        semantics.determineInitialStates(stateMachine,
                step.getAfterStatus().getStates(),
                step.getEntryList(), errorReporter, flowInstance);
        // ExecuteActions
        semantics.executeActions(step, stateMachine, eventdispatcher,
                errorReporter, flowInstance);
        // AssignCurrentStatus
        updateStatus(step);
        // Execute Immediate Transitions
        if (superStep && currentStatus.getEvents().size() > 0) {
            this.triggerEvents(new FlowTriggerEvent[0]);
        } else {
            // InitiateInvokes only after state machine has stabilized
            semantics.initiateInvokes(step, errorReporter, flowInstance);
            logState();
        }
    }

    /**
     * Get the current status.
     *
     * @return The current Status
     */
    public synchronized FlowStatus getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Set the expression evaluator.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param evaluator The evaluator to set.
     */
    public void setEvaluator(final FlowEvaluator evaluator) {
        this.flowInstance.setEvaluator(evaluator);
    }

    /**
     * Get the expression evaluator in use.
     *
     * @return Evaluator The evaluator in use.
     */
    public FlowEvaluator getEvaluator() {
        return flowInstance.getEvaluator();
    }

    /**
     * Set the root context for this execution.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param rootContext The Context that ties to the host environment.
     */
    public void setRootContext(final FlowContext rootContext) {
        this.flowInstance.setRootContext(rootContext);
    }

    /**
     * Get the root context for this execution.
     *
     * @return Context The root context.
     */
    public FlowContext getRootContext() {
        return flowInstance.getRootContext();
    }

    /**
     * Get the state machine that is being executed.
     * <b>NOTE:</b> This is the state machine definition or model used by this
     * executor instance. It may be shared across multiple executor instances
     * and as a best practice, should not be altered. Also note that
     * manipulation of instance data for the executor should happen through its
     * root context or state contexts only, never through the direct
     * manipulation of any {@link Datamodel}s associated with this state machine
     * definition.
     *
     * @return Returns the stateMachine.
     */
    public StateChart getStateMachine() {
        return stateMachine;
    }

    /**
     * Set the state machine to be executed.
     * <b>NOTE:</b> Should only be used before the executor is set in motion.
     *
     * @param stateMachine The stateMachine to set.
     */
    public void setStateMachine(final StateChart stateMachine) {
        // NormalizeStateMachine
        StateChart sm = semantics.normalizeStateMachine(stateMachine, errorReporter);
        // StoreStateMachine
        this.stateMachine = sm;
    }

    /**
     * Initiate state machine execution.
     *
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    public void go() throws ModelException {
        // same as reset
        this.reset();
    }

    /**
     * Get the environment specific error reporter.
     *
     * @return Returns the errorReporter.
     */
    public FlowErrorReporter getErrorReporter() {
        return errorReporter;
    }

    /**
     * Set the environment specific error reporter.
     *
     * @param errorReporter The errorReporter to set.
     */
    public void setErrorReporter(final FlowErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
    }

    /**
     * Get the event dispatcher.
     *
     * @return Returns the eventdispatcher.
     */
    public FlowEventDispatcher getEventdispatcher() {
        return eventdispatcher;
    }

    /**
     * Set the event dispatcher.
     *
     * @param eventdispatcher The eventdispatcher to set.
     */
    public void setEventdispatcher(final FlowEventDispatcher eventdispatcher) {
        this.eventdispatcher = eventdispatcher;
    }

    /**
     * Use &quot;super-step&quot;, default is <code>true</code> (that is,
     * run-to-completion is default).
     *
     * @return Returns the superStep property.
     * @see #setSuperStep(boolean)
     */
    public boolean isSuperStep() {
        return superStep;
    }

    /**
     * Set the super step.
     *
     * @param superStep if true, the internal derived events are also processed
     * (run-to-completion); if false, the internal derived events are stored in
     * the CurrentStatus property and processed within the next triggerEvents()
     * invocation, also the immediate (empty event) transitions are deferred
     * until the next step
     */
    public void setSuperStep(final boolean superStep) {
        this.superStep = superStep;
    }

    /**
     * Add a listener to the document root.
     *
     * @param flow The document root to attach listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final StateChart flow, final StateFlowListener listener) {
        Object observable = flow;
        flowInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener from the document root.
     *
     * @param flow The document root.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final StateChart flow, final StateFlowListener listener) {
        Object observable = flow;
        flowInstance.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Add a listener to this transition target.
     *
     * @param transitionTarget The <code>TransitionTarget</code> to attach
     * listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final TransitionTarget transitionTarget, final StateFlowListener listener) {
        Object observable = transitionTarget;
        flowInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener for this transition target.
     *
     * @param transitionTarget The <code>TransitionTarget</code>.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final TransitionTarget transitionTarget, final StateFlowListener listener) {
        Object observable = transitionTarget;
        flowInstance.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Add a listener to this transition.
     *
     * @param transition The <code>Transition</code> to attach listener to.
     * @param listener The SCXMLListener.
     */
    public void addListener(final Transition transition, final StateFlowListener listener) {
        Object observable = transition;
        flowInstance.getNotificationRegistry().addListener(observable, listener);
    }

    /**
     * Remove this listener for this transition.
     *
     * @param transition The <code>Transition</code>.
     * @param listener The SCXMLListener to be removed.
     */
    public void removeListener(final Transition transition, final StateFlowListener listener) {
        Object observable = transition;
        flowInstance.getNotificationRegistry().removeListener(observable, listener);
    }

    /**
     * Register an <code>Invoker</code> for this target type.
     *
     * @param targettype The target type (specified by "targettype" attribute of
     * &lt;invoke&gt; tag).
     * @param invokerClass The <code>Invoker</code> <code>Class</code>.
     */
    public void registerInvokerClass(final String targettype, final Class invokerClass) {
        flowInstance.registerInvokerClass(targettype, invokerClass);
    }

    /**
     * Remove the <code>Invoker</code> registered for this target type (if there
     * is one registered).
     *
     * @param targettype The target type (specified by "targettype" attribute of
     * &lt;invoke&gt; tag).
     */
    public void unregisterInvokerClass(final String targettype) {
        flowInstance.unregisterInvokerClass(targettype);
    }

    /**
     * Get the state chart instance for this executor.
     *
     * @return The SCInstance for this executor.
     */
    FlowInstance getFlowInstance() {
        return flowInstance;
    }

    /**
     * Log the current set of active states.
     */
    protected void logState() {
        if (log.isLoggable(Level.FINEST)) {
            Iterator si = currentStatus.getStates().iterator();
            StringBuilder sb = new StringBuilder("Current States: [");
            while (si.hasNext()) {
                State s = (State) si.next();
                sb.append(s.getId());
                if (si.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(']');
            log.log(Level.FINEST, sb.toString());
        }
    }

    /**
     * @param step The most recent Step
     */
    private void updateStatus(final FlowStep step) {
        currentStatus = step.getAfterStatus();
        flowInstance.getRootContext().setLocal("_ALL_STATES", StateFlowHelper.getAncestorClosure(currentStatus.getStates(), null));
        setEventData((FlowTriggerEvent[]) currentStatus.getEvents().toArray(new FlowTriggerEvent[0]));
    }

    /**
     * @param evts The events being triggered.
     * @return Object[] Previous values.
     */
    private Object[] setEventData(final FlowTriggerEvent[] evts) {
        FlowContext rootCtx = flowInstance.getRootContext();
        Object[] oldData = {rootCtx.get(EVENT_DATA), rootCtx.get(EVENT_DATA_MAP)};
        int len = evts.length;
        if (len > 0) { // 0 has retry semantics (eg: see usage in reset())
            Object eventData = null;
            Map payloadMap = new HashMap();
            for (int i = 0; i < len; i++) {
                FlowTriggerEvent te = evts[i];
                payloadMap.put(te.getName(), te.getPayload());
            }
            if (len == 1) {
                // we have only one event
                eventData = evts[0].getPayload();
            }
            rootCtx.setLocal(EVENT_DATA, eventData);
            rootCtx.setLocal(EVENT_DATA_MAP, payloadMap);
        }
        return oldData;
    }

    /**
     * @param oldData The old values to restore to.
     */
    private void restoreEventData(final Object[] oldData) {
        flowInstance.getRootContext().setLocal(EVENT_DATA, oldData[0]);
        flowInstance.getRootContext().setLocal(EVENT_DATA_MAP, oldData[1]);
    }

    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[3];

        values[0] = superStep;
        values[1] = flowInstance.saveState(context);
        if (currentStatus != null) {
            values[2] = currentStatus.saveState(context);
        }

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
        
        if (values[0] != null) {
            superStep = (boolean) values[0];
        }
        if (values[1] != null) {
            flowInstance.restoreState(context, values[1]);
        }
        currentStatus = new FlowStatus();
        if (values[2] != null) {
            currentStatus.restoreState(context, values[1]);
        }
    }

}
