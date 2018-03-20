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
package org.ssoft.faces.state.semantics;

import javax.faces.state.semantics.ErrorConstants;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.EventDispatcher;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.faces.state.ModelException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.invoke.InvokerException;
import javax.faces.state.model.Action;
import javax.faces.state.model.Finalize;
import javax.faces.state.model.History;
import javax.faces.state.model.Initial;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.OnExit;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.Path;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.utils.StateFlowHelper;
import javax.faces.state.FlowStep;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowNotificationRegistry;
import javax.faces.state.PathResolver;
import javax.faces.state.model.Param;
import static javax.faces.state.semantics.ErrorConstants.INVOKE_ERROR;
import javax.faces.state.semantics.StateChartSemantics;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChartSemanticsImpl implements StateChartSemantics, Serializable {

    /**
     * SCXML Logger for the application.
     */
    public static final Logger log = FlowLogger.FLOW.getLogger();

    /**
     * The TransitionTarget comparator.
     */
    private final TransitionTargetComparator targetComparator
            = new TransitionTargetComparator();

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";

    /**
     * Suffix for error event that are triggered in reaction to invalid data
     * model locations.
     */
    private static final String ERR_ILLEGAL_ALLOC = ".error.illegalalloc";

    /**
     * @param input SCXML state machine
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     * @param errRep ErrorReporter callback
     */
    @Override
    public StateChart normalizeStateMachine(final StateChart input,
            final FlowErrorReporter errRep) {
        //it is a no-op for now
        return input;
    }

    /**
     * @param input SCXML state machine [in]
     * @param targets a set of initial targets to populate [out]
     * @param entryList a list of States and Parallels to enter [out]
     * @param errRep ErrorReporter callback [inout]
     * @param scInstance The state chart instance [in]
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    @Override
    public void determineInitialStates(final StateChart input, final Set targets,
            final List entryList, final FlowErrorReporter errRep,
            final FlowInstance scInstance)
            throws ModelException {
        TransitionTarget tmp = input.getInitialTarget();
        if (tmp == null) {
            errRep.onError(ErrorConstants.NO_INITIAL,
                    "SCXML initialstate is missing!", input, null);
        } else {
            targets.add(tmp);
            determineTargetStates(targets, errRep, scInstance);
            //set of ALL entered states (even if initialState is a jump-over)
            Set onEntry = StateFlowHelper.getAncestorClosure(targets, null);
            // sort onEntry according state hierarchy
            Object[] oen = onEntry.toArray();
            onEntry.clear();
            Arrays.sort(oen, getTTComparator());
            // we need to impose reverse order for the onEntry list
            List entering = Arrays.asList(oen);
            Collections.reverse(entering);
            entryList.addAll(entering);

        }
    }

    /**
     * Executes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param step provides EntryList, TransitList, ExitList gets updated its
     * AfterStatus/Events
     * @param stateMachine state machine - SCXML instance
     * @param evtDispatcher the event dispatcher - EventDispatcher instance
     * @param errRep error reporter
     * @param scInstance The state chart instance
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    @Override
    public void executeActions(final FlowStep step, final StateChart stateMachine,
            final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance)
            throws ModelException {
        FlowNotificationRegistry nr = scInstance.getNotificationRegistry();
        Collection internalEvents = step.getAfterStatus().getEvents();
        Map<TransitionTarget, Invoker> invokers = scInstance.getInvokers();
        // ExecutePhaseActions / OnExit
        for (TransitionTarget tt : step.getExitList()) {
            OnExit oe = tt.getOnExit();
            if (oe != null) {
                try {
                    for (Iterator onExitIter = oe.getActions().iterator(); onExitIter.hasNext();) {
                        final Action action = (Action) onExitIter.next();
                        scInstance.process(action, () -> {
                            action.execute(evtDispatcher, errRep, scInstance, step.getAfterStatus().getEvents());
                        });
                    }
                } catch (FlowExpressionException e) {
                    errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(),
                            oe, e);
                }
            }
            // check if invoke is active in this state
            if (invokers.containsKey(tt)) {
                Invoker toCancel = (Invoker) invokers.get(tt);
                try {
                    scInstance.process((State) tt, toCancel, () -> toCancel.cancel());
                } catch (InvokerException ie) {
                    FlowTriggerEvent te = new FlowTriggerEvent(
                            tt.getId() + "invoke.cancel.failed",
                            FlowTriggerEvent.ERROR_EVENT);
                    internalEvents.add(te);
                }
                // done here, don't wait for cancel response
                invokers.remove(tt);
            }
            nr.fireOnExit(tt, tt);
            nr.fireOnExit(stateMachine, tt);
            FlowTriggerEvent te = new FlowTriggerEvent(tt.getId() + ".exit",
                    FlowTriggerEvent.CHANGE_EVENT);
            internalEvents.add(te);
        }
        // ExecutePhaseActions / Transitions
        for (Transition transition : step.getTransitList()) {
            try {
                for (Iterator transitIter = transition.getActions().iterator(); transitIter.hasNext();) {
                    final Action action = (Action) transitIter.next();
                    scInstance.process(action, () -> {
                        action.execute(evtDispatcher, errRep, scInstance, step.getAfterStatus().getEvents());
                    });
                }
            } catch (FlowExpressionException e) {
                errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), transition, e);
            }
            List rtargets = transition.getRuntimeTargets();
            for (int j = 0; j < rtargets.size(); j++) {
                TransitionTarget tt = (TransitionTarget) rtargets.get(j);
                nr.fireOnTransition(transition, transition.getParent(), tt, transition);
                nr.fireOnTransition(stateMachine, transition.getParent(), tt, transition);
            }
        }
        // ExecutePhaseActions / OnEntry
        for (TransitionTarget tt : step.getEntryList()) {
            OnEntry oe = tt.getOnEntry();
            if (oe != null) {
                try {
                    for (Iterator onEntryIter = oe.getActions().iterator(); onEntryIter.hasNext();) {
                        final Action action = (Action) onEntryIter.next();
                        scInstance.process(action, () -> {
                            action.execute(evtDispatcher, errRep, scInstance, step.getAfterStatus().getEvents());
                        });
                    }
                } catch (FlowExpressionException e) {
                    errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), oe, e);
                }
            }
            nr.fireOnEntry(tt, tt);
            nr.fireOnEntry(stateMachine, tt);
            FlowTriggerEvent te = new FlowTriggerEvent(tt.getId() + ".entry", FlowTriggerEvent.CHANGE_EVENT);
            internalEvents.add(te);
            // actions in initial transition (if any) and .done events
            if (tt instanceof State) {
                State ts = (State) tt;
                Initial ini = ts.getInitial();
                if (ts.isComposite() && ini != null) {
                    try {
                        for (Iterator iniIter = ini.getTransition().getActions().iterator(); iniIter.hasNext();) {
                            final Action action = (Action) iniIter.next();
                            scInstance.process(action, () -> {
                                action.execute(evtDispatcher, errRep, scInstance, step.getAfterStatus().getEvents());
                            });
                        }
                    } catch (FlowExpressionException e) {
                        errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                                e.getMessage(), ini, e);
                    }
                }
                if (ts.isFinal()) {
                    State parent = (State) ts.getParent();
                    String prefix = "";
                    if (parent != null) {
                        prefix = parent.getId();
                    }
                    te = new FlowTriggerEvent(prefix + ".done", FlowTriggerEvent.CHANGE_EVENT);
                    internalEvents.add(te);
                    if (parent != null) {
                        scInstance.setDone(parent, true);
                    }
                    if (parent != null && parent.isRegion()) {
                        //3.4 we got a region, which is finalized
                        //let's check its siblings too
                        Parallel p = (Parallel) parent.getParent();
                        int finCount = 0;
                        int pCount = p.getChildren().size();
                        for (Map.Entry<String, TransitionTarget> entry : p.getChildren().entrySet()) {
                            State reg = (State) entry.getValue();
                            if (scInstance.isDone(reg)) {
                                finCount++;
                            }
                        }
                        if (finCount == pCount) {
                            te = new FlowTriggerEvent(p.getId() + ".done", FlowTriggerEvent.CHANGE_EVENT);
                            internalEvents.add(te);
                            scInstance.setDone(p, true);
//                            if (stateMachine.isLegacy()) {
//                                te = new FlowTriggerEvent(p.getParent().getId() + ".done", FlowTriggerEvent.CHANGE_EVENT);
//                                internalEvents.add(te);
//                                //this is not in the specs, but is makes sense
//                                scInstance.setDone(p.getParentState(), true);
//                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param stateMachine a SM to traverse [in]
     * @param step with current status and list of transitions to populate
     * [inout]
     * @param errRep ErrorReporter callback [inout]
     */
    @Override
    public void enumerateReachableTransitions(final StateChart stateMachine,
            final FlowStep step, final FlowErrorReporter errRep) {
        // prevents adding the same transition multiple times
        Set transSet = new HashSet();
        // prevents visiting the same state multiple times
        Set stateSet = new HashSet(step.getBeforeStatus().getStates());
        // breath-first search to-do list
        LinkedList todoList = new LinkedList(stateSet);
        while (!todoList.isEmpty()) {
            TransitionTarget tt = (TransitionTarget) todoList.removeFirst();
            for (Transition transition : tt.getTransitionsList()) {
                if (!transSet.contains(transition)) {
                    transSet.add(transition);
                    step.getTransitList().add(transition);
                }
            }
            TransitionTarget parent = tt.getParent();
            if (parent != null && !stateSet.contains(parent)) {
                stateSet.add(parent);
                todoList.addLast(parent);
            }
        }
        transSet.clear();
        stateSet.clear();
        todoList.clear();
    }

    /**
     * @param step [inout]
     * @param evtDispatcher The {@link EventDispatcher} [in]
     * @param errRep ErrorReporter callback [inout]
     * @param istance The state chart instance [in]
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    @Override
    public void filterTransitionsSet(final FlowStep step,
            final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance istance)
            throws ModelException {
        /*
         * - filter transition set by applying events
         * (step/beforeStatus/events + step/externalEvents) (local check)
         * - evaluating guard conditions for
         * each transition (local check) - transition precedence (bottom-up)
         * as defined by Flow specs
         */
        Set<FlowTriggerEvent> allEvents = new HashSet(step.getBeforeStatus()
                .getEvents().size() + step.getExternalEvents().size());
        allEvents.addAll(step.getBeforeStatus().getEvents());
        allEvents.addAll(step.getExternalEvents());
        // Finalize invokes, if applicable
        for (Iterator iter = istance.getInvokers().keySet().iterator(); iter.hasNext();) {
            State s = (State) iter.next();
            if (finalizeMatch(s.getId(), allEvents)) {
                Finalize fn = s.getInvoke().getFinalize();
                if (fn != null) {
                    try {
                        for (Iterator fnIter = fn.getActions().iterator(); fnIter.hasNext();) {
                            final Action action = (Action) fnIter.next();
                            istance.process(action, () -> action.execute(
                                    evtDispatcher, errRep, istance, 
                                    step.getAfterStatus().getEvents()));
                        }
                    } catch (FlowExpressionException e) {
                        errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), fn, e);
                    }
                }
            }
        }
        //remove list (filtered-out list)
        List removeList = new LinkedList();
        //iterate over non-filtered transition set
        for (Transition t : step.getTransitList()) {
            // event check
            String event = t.getEvent();
            if (!eventMatch(event, allEvents)) {
                // t has a non-empty event which is not triggered
                removeList.add(t);
                continue; //makes no sense to eval guard cond.
            }
            // guard condition check
            Boolean rslt;
            ValueExpression expr = t.getCond();
            if (expr == null) {
                rslt = Boolean.TRUE;
            } else {
                try {
                    rslt = (Boolean) istance.eval(t, t.getCond());
                } catch (FlowExpressionException e) {
                    rslt = Boolean.FALSE;
                    errRep.onError(ErrorConstants.EXPRESSION_ERROR, e.getMessage(), t, e);
                }
            }
            if (!rslt) {
                // guard condition has not passed
                removeList.add(t);
            }
        }
        // apply event + guard condition filter
        step.getTransitList().removeAll(removeList);
        // cleanup temporary structures
        allEvents.clear();
        removeList.clear();
        // optimization - global precedence potentially applies
        // only if there are multiple enabled transitions
        if (step.getTransitList().size() > 1) {
            // global transition precedence check
            Object[] trans = step.getTransitList().toArray();
            // non-determinism candidates
            Set nonDeterm = new LinkedHashSet();
            for (int i = 0; i < trans.length; i++) {
                Transition t = (Transition) trans[i];
                TransitionTarget tsrc = t.getParent();
                for (int j = i + 1; j < trans.length; j++) {
                    Transition t2 = (Transition) trans[j];
                    TransitionTarget t2src = t2.getParent();
                    if (StateFlowHelper.isDescendant(t2src, tsrc)) {
                        //t2 takes precedence over t
                        removeList.add(t);
                        break; //it makes no sense to waste cycles with t
                    } else if (StateFlowHelper.isDescendant(tsrc, t2src)) {
                        //t takes precendence over t2
                        removeList.add(t2);
                    } else {
                        //add both to the non-determinism candidates
                        nonDeterm.add(t);
                        nonDeterm.add(t2);
                    }
                }
            }
            // check if all non-deterministic situations have been resolved
            nonDeterm.removeAll(removeList);
            if (nonDeterm.size() > 0) {
                // if not, first one in each state / region (which is also
                // first in document order) wins
                Set regions = new HashSet();
                Iterator iter = nonDeterm.iterator();
                while (iter.hasNext()) {
                    Transition t = (Transition) iter.next();
                    TransitionTarget parent = t.getParent();
                    if (regions.contains(parent)) {
                        removeList.add(t);
                    } else {
                        regions.add(parent);
                    }
                }
            }
            // apply global and document order transition filter
            step.getTransitList().removeAll(removeList);
        }
    }

    /**
     * Populate the target set.
     * <ul>
     * <li>take targets of selected transitions</li>
     * <li>take exited regions into account and make sure every active parallel
     * region has all siblings active [that is, explicitly visit or sibling
     * regions in case of newly visited (revisited) orthogonal states]</li>
     * </ul>
     *
     * @param residual [in]
     * @param transitList [in]
     * @param errRep ErrorReporter callback [inout]
     * @return Set The target set
     */
    public Set seedTargetSet(final Set<State> residual, final List<Transition> transitList,
            final FlowErrorReporter errRep) {
        Set<TransitionTarget> seedSet = new HashSet();
        Set<TransitionTarget> regions = new HashSet();
        for (Transition t : transitList) {
            //iterate over transitions and add target states
            if (t.getTargets().size() > 0) {
                seedSet.addAll(t.getTargets());
            }
            //build a set of all entered regions
            List paths = t.getPaths();
            for (int j = 0; j < paths.size(); j++) {
                Path p = (Path) paths.get(j);
                if (p.isCrossRegion()) {
                    List regs = p.getRegionsEntered();
                    for (Iterator k = regs.iterator(); k.hasNext();) {
                        State region = (State) k.next();
                        regions.addAll(((Parallel) region.getParent()).
                                getChildren().values());
                    }
                }
            }
        }
        //check whether all active regions have their siblings active too
        Set<TransitionTarget> allStates = new HashSet(residual);
        allStates.addAll(seedSet);
        allStates = StateFlowHelper.getAncestorClosure(allStates, null);
        regions.removeAll(allStates);
        //iterate over inactive regions and visit them implicitly using initial
        for (Iterator i = regions.iterator(); i.hasNext();) {
            State reg = (State) i.next();
            seedSet.add(reg);
        }
        return seedSet;
    }

    /**
     * @param states a set seeded in previous step [inout]
     * @param errRep ErrorReporter callback [inout]
     * @param scInstance The state chart instance [in]
     * @throws ModelException On illegal configuration
     * @see #seedTargetSet(Set, List, ErrorReporter)
     */
    public void determineTargetStates(final Set states,
            final FlowErrorReporter errRep, final FlowInstance scInstance)
            throws ModelException {
        LinkedList<TransitionTarget> wrkSet = new LinkedList(states);
        // clear the seed-set - will be populated by leaf states
        states.clear();
        while (!wrkSet.isEmpty()) {
            TransitionTarget tt = (TransitionTarget) wrkSet.removeFirst();
            if (tt instanceof State) {
                State st = (State) tt;
                //state can either have parallel or substates w. initial
                //or it is a leaf state
                // NOTE: Digester has to verify this precondition!
                if (st.isSimple()) {
                    states.add(st); //leaf
                } else if (st.isOrthogonal()) { //TODO: Remove else if in v1.0
                    wrkSet.addLast(st.getParallel()); //parallel
                } else {
                    // composite state
                    List<TransitionTarget> initialStates = st.getInitial().getTransition().getTargets();
                    wrkSet.addAll(initialStates);
                }
            } else if (tt instanceof Parallel) {
                Parallel prl = (Parallel) tt;
                for (Map.Entry<String, TransitionTarget> entry : prl.getChildren().entrySet()) {
                    //fork
                    wrkSet.addLast(entry.getValue());
                }
            } else if (tt instanceof History) {
                History h = (History) tt;
                if (scInstance.isEmpty(h)) {
                    wrkSet.addAll(h.getTransition().getRuntimeTargets());
                } else {
                    wrkSet.addAll(scInstance.getLastConfiguration(h));
                }
            } else {
                throw new ModelException("Unknown TransitionTarget subclass:"
                        + tt.getClass().getName());
            }
        }
    }

    /**
     * Go over the exit list and update history information for relevant states.
     *
     * @param step [inout]
     * @param errRep ErrorReporter callback [inout]
     * @param scInstance The state chart instance [inout]
     */
    @Override
    @SuppressWarnings("UnusedAssignment")
    public void updateHistoryStates(final FlowStep step,
            final FlowErrorReporter errRep, final FlowInstance scInstance) {
        Set oldState = step.getBeforeStatus().getStates();
        for (Iterator i = step.getExitList().iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State s = (State) o;
                if (s.hasHistory()) {
                    Set shallow = null;
                    Set deep = null;
                    for (History history : s.getHistory()) {
                        if (history.isDeep()) {
                            if (deep == null) {
                                //calculate deep history for a given state once
                                deep = new HashSet();
                                Iterator k = oldState.iterator();
                                while (k.hasNext()) {
                                    State os = (State) k.next();
                                    if (StateFlowHelper.isDescendant(os, s)) {
                                        deep.add(os);
                                    }
                                }
                            }
                            scInstance.setLastConfiguration(history, deep);
                        } else {
                            if (shallow == null) {
                                //calculate shallow history for a given state
                                // once
                                shallow = new HashSet();
                                shallow.addAll(s.getChildren().values());
                                shallow.retainAll(StateFlowHelper
                                        .getAncestorClosure(oldState, null));
                            }
                            scInstance.setLastConfiguration(history, shallow);
                        }
                    }
                    shallow = null;
                    deep = null;
                }
            }
        }
    }

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     * @param scInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal SCXML object model
     * problem.
     */
    @Override
    public void followTransitions(final FlowStep step,
            final FlowErrorReporter errorReporter, final FlowInstance scInstance)
            throws ModelException {
        Set<State> currentStates = step.getBeforeStatus().getStates();
        List<Transition> transitions = step.getTransitList();
        // DetermineExitedStates (currentStates, transitList) -> exitedStates
        Set<State> exitedStates = new HashSet();
        for (Transition t : transitions) {
            Set ext = StateFlowHelper.getStatesExited(t, currentStates);
            exitedStates.addAll(ext);
        }
        // compute residual states - these are preserved from the previous step
        Set<State> residual = new HashSet<>(currentStates);
        residual.removeAll(exitedStates);
        // SeedTargetSet (residual, transitList) -> seedSet
        Set seedSet = seedTargetSet(residual, transitions, errorReporter);
        // DetermineTargetStates (initialTargetSet) -> targetSet
        Set targetSet = step.getAfterStatus().getStates();
        targetSet.addAll(seedSet); //copy to preserve seedSet
        determineTargetStates(targetSet, errorReporter, scInstance);
        // BuildOnEntryList (targetSet, seedSet) -> entryList
        Set entered = StateFlowHelper.getAncestorClosure(targetSet, seedSet);
        seedSet.clear();
        for (Transition t : transitions) {
            List paths = t.getPaths();
            for (int j = 0; j < paths.size(); j++) {
                Path p = (Path) paths.get(j);
                entered.addAll(p.getDownwardSegment());
            }
            // If target is a History pseudo state, remove from entered list
            List rtargets = t.getRuntimeTargets();
            for (int j = 0; j < rtargets.size(); j++) {
                TransitionTarget tt = (TransitionTarget) rtargets.get(j);
                if (tt instanceof History) {
                    entered.remove(tt);
                }
            }
        }
        // Check whether the computed state config is legal
        targetSet.addAll(residual);
        residual.clear();
        if (!StateFlowHelper.isLegalConfig(targetSet, errorReporter)) {
            throw new ModelException("Illegal state machine configuration!");
        }
        // sort onEntry and onExit according state hierarchy
        State[] oex = exitedStates.toArray(new State[exitedStates.size()]);
        exitedStates.clear();
        Object[] oen = entered.toArray();
        entered.clear();
        Arrays.sort(oex, getTTComparator());
        Arrays.sort(oen, getTTComparator());
        step.getExitList().addAll(Arrays.asList(oex));
        // we need to impose reverse order for the onEntry list
        List entering = Arrays.asList(oen);
        Collections.reverse(entering);
        step.getEntryList().addAll(entering);
        // reset 'done' flag
        for (Iterator reset = entering.iterator(); reset.hasNext();) {
            Object o = reset.next();
            if (o instanceof State) {
                scInstance.setDone((State) o, false);
            }
        }
    }

    /**
     * Process any existing invokes, includes forwarding external events, and
     * executing any finalize handlers.
     *
     * @param events The events to be forwarded
     * @param errRep ErrorReporter callback
     * @param scInstance The state chart instance
     * @throws ModelException in case there is a fatal Flow object model
     * problem.
     */
    @Override
    public void processInvokes(final FlowTriggerEvent[] events,
            final FlowErrorReporter errRep, final FlowInstance scInstance)
            throws ModelException {

        Set allEvents = new HashSet();
        allEvents.addAll(Arrays.asList(events));
        for (Map.Entry<TransitionTarget, Invoker> iEntry : scInstance.getInvokers().entrySet()) {
            TransitionTarget tt = iEntry.getKey();
            String parentId = tt.getId();
            if (!finalizeMatch(parentId, allEvents)) { // prevent cycles
                Invoker inv = (Invoker) iEntry.getValue();
                try {
                    scInstance.process((State) tt, inv, () -> inv.parentEvents(events));
                } catch (InvokerException ie) {
                    log.log(Level.SEVERE, ie.getMessage(), ie);
                    throw new ModelException(ie.getMessage(), ie.getCause());
                }
            }
        }

    }

    /**
     * Initiate any new invokes.
     *
     * @param step The current Step
     * @param errRep ErrorReporter callback
     * @param istance The state chart instance
     */
    @Override
    public void initiateInvokes(final FlowStep step, final FlowErrorReporter errRep, final FlowInstance istance) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FlowEvaluator eval = istance.getEvaluator();
        Collection internalEvents = step.getAfterStatus().getEvents();
        for (State state : step.getAfterStatus().getStates()) {
            FlowContext ctx = istance.getContext(state);
            Invoke invoke = state.getInvoke();

            if (invoke != null && istance.getInvoker(state) == null) {
                try {
                    final Invoker inv = istance.newInvoker(invoke, state);

                    istance.process(state, inv, () -> {

                        ValueExpression src = invoke.getSrc();
                        String source = (String) src.getValue(istance);

                        PathResolver pr = invoke.getPathResolver();
                        if (pr != null) {
                            source = invoke.getPathResolver().resolvePath(fc, source);
                        }

                        List params = invoke.params();
                        Map args = new HashMap();

                        for (Iterator pIter = params.iterator(); pIter.hasNext();) {
                            Param p = (Param) pIter.next();
                            ValueExpression argExpr = p.getExpr();
                            Object argValue = null;
                            ctx.setLocal(NAMESPACES_KEY, p.getNamespaces());
                            // Do we have an "expr" attribute?
                            if (argExpr != null) {
                                // Yes, evaluate and store as parameter value
                                try {
                                    argValue = (String) istance.eval(p, p.getExpr());
                                } catch (FlowExpressionException see) {
                                    errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                                            see.getMessage(), invoke, see);
                                }
                            } else {
                                // No. Does value of "name" attribute refer to a valid
                                // location in the data model?
                                try {
                                    argValue = eval.evalLocation(ctx, p.getName());
                                    if (argValue == null) {
                                        // Generate error, 4.3.1 in WD-scxml-20080516
                                        FlowTriggerEvent te = new FlowTriggerEvent(state.getId()
                                                + ERR_ILLEGAL_ALLOC,
                                                FlowTriggerEvent.ERROR_EVENT);
                                        internalEvents.add(te);
                                    }
                                } catch (FlowExpressionException see) {
                                    errRep.onError(ErrorConstants.EXPRESSION_ERROR,
                                            see.getMessage(), invoke, see);
                                }
                            }
                            ctx.setLocal(NAMESPACES_KEY, null);
                            args.put(p.getName(), argValue);
                        }

                        inv.invoke(source, args);
                    });

                    istance.setInvoker(state, invoke, inv);
                } catch (InvokerException ie) {
                    istance.getExecutor().getErrorReporter().onError(INVOKE_ERROR,
                            StateFlowHelper.getErrorMessage(ie), invoke, ie);
                    FlowTriggerEvent te = new FlowTriggerEvent(
                            state.getId() + ".invoke.failed", FlowTriggerEvent.ERROR_EVENT);
                    internalEvents.add(te);
                }
            }
        }
    }

    /**
     * Implements prefix match, that is, if, for example,
     * &quot;mouse.click&quot; is a member of eventOccurrences and a transition
     * is triggered by &quot;mouse&quot;, the method returns true.
     *
     * @param transEvent a trigger event of a transition
     * @param eventOccurrences current events
     * @return true/false
     */
    protected boolean eventMatch(final String transEvent,
            final Set eventOccurrences) {
        if (StateFlowHelper.isStringEmpty(transEvent)) { // Eventless transition
            return true;
        } else {
            String trimTransEvent = transEvent.trim();
            Iterator i = eventOccurrences.iterator();
            while (i.hasNext()) {
                FlowTriggerEvent te = (FlowTriggerEvent) i.next();
                String event = te.getName();
                if (event == null) {
                    continue; // Unnamed events
                }
                String trimEvent = event.trim();
                if (trimEvent.equals(trimTransEvent)) {
                    return true; // Match
                } else if (te.getType() != FlowTriggerEvent.CHANGE_EVENT && trimTransEvent.equals("*")) {
                    return true; // Wildcard, skip gen'ed ones like .done etc.
                } else if (trimTransEvent.endsWith(".*") && trimEvent.startsWith(trimTransEvent.substring(0, trimTransEvent.length() - 1))) {
                    return true; // Prefixed wildcard
                }
            }
            return false;
        }
    }

    /**
     * Implements event prefix match to ascertain &lt;finalize&gt; execution.
     *
     * @param parentStateId the ID of the parent state of the &lt;invoke&gt;
     * holding the &lt;finalize&gt;
     * @param eventOccurrences current events
     * @return true/false
     */
    @SuppressWarnings("UnnecessaryContinue")
    protected boolean finalizeMatch(final String parentStateId, final Set eventOccurrences) {
        
        String prefix = parentStateId + ".invoke."; // invoke prefix
        Iterator i = eventOccurrences.iterator();
        while (i.hasNext()) {
            String evt = ((FlowTriggerEvent) i.next()).getName();
            if (evt == null) {
                continue; // Unnamed events
            } else if (evt.trim().startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TransitionTargetComparator factory method.
     *
     * @return Comparator The TransitionTarget comparator
     */
    protected Comparator getTTComparator() {
        return targetComparator;
    }

}
