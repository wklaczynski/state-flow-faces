/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.semantics;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowStep;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface StateChartSemantics {

    static final String CURRENT_SEMANTICS_STACK_KEY = "javax.faces.state.CURRENT_SEMANTICS_STACK";

    /**
     * Optional post processing immediately following Digester. May be used for
     * removing pseudo-states etc.
     *
     * @param input SCXML state machine
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     * @param errRep ErrorReporter callback
     */
    StateChart normalizeStateMachine(final StateChart input, final FlowErrorReporter errRep);

    /**
     * Determining the initial state(s) for this state machine.
     *
     * @param input SCXML state machine
     * @param states a set of States to populate
     * @param entryList a list of States and Parallels to enter
     * @param errRep ErrorReporter callback
     * @param sfInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal object model problem.
     */
    void determineInitialStates(final StateChart input, final Set states,
            final List entryList, final FlowErrorReporter errRep,
            final FlowInstance sfInstance)
            throws ModelException;

    /**
     * Executes all OnExit/Transition/OnEntry transitional actions.
     *
     * @param step provides EntryList, TransitList, ExitList gets updated its
     * AfterStatus/Events
     * @param stateMachine state machine - SCXML instance
     * @param evtDispatcher the event dispatcher - EventDispatcher instance
     * @param errRep error reporter
     * @param sfInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal object model problem.
     */
    void executeActions(final FlowStep step, final StateChart stateMachine,
            final FlowEventDispatcher evtDispatcher, final FlowErrorReporter errRep,
            final FlowInstance sfInstance)
            throws ModelException;

    /**
     * Enumerate all the reachable transitions.
     *
     * @param stateMachine a state machine to traverse
     * @param step with current status and list of transitions to populate
     * @param errRep ErrorReporter callback
     */
    void enumerateReachableTransitions(final StateChart stateMachine,
            final FlowStep step, final FlowErrorReporter errRep);

    /**
     * Filter the transitions set, eliminate those whose guard conditions are
     * not satisfied.
     *
     * @param step with current status
     * @param evtDispatcher the event dispatcher - EventDispatcher instance
     * @param errRep ErrorReporter callback
     * @param sfInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal object model problem.
     */
    void filterTransitionsSet(final FlowStep step,
            final FlowEventDispatcher evtDispatcher, final FlowErrorReporter errRep,
            final FlowInstance sfInstance)
            throws ModelException;

    /**
     * Follow the candidate transitions for this execution Step, and update the
     * lists of entered and exited states accordingly.
     *
     * @param step The current Step
     * @param errorReporter The ErrorReporter for the current environment
     * @param sfInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal object model problem.
     */
    void followTransitions(final FlowStep step, final FlowErrorReporter errorReporter,
            final FlowInstance sfInstance)
            throws ModelException;

    /**
     * Go over the exit list and update history information for relevant states.
     *
     * @param step The current Step
     * @param errRep ErrorReporter callback
     * @param sfInstance The state chart instance
     */
    void updateHistoryStates(final FlowStep step, final FlowErrorReporter errRep,
            final FlowInstance sfInstance);

    /**
     * Forward events to invoked activities, execute finalize handlers.
     *
     * @param events The events to be forwarded
     * @param errRep ErrorReporter callback
     * @param scInstance The state chart instance
     *
     * @throws ModelException in case there is a fatal object model problem.
     */
    void processInvokes(final FlowTriggerEvent[] events,
            final FlowErrorReporter errRep, final FlowInstance scInstance)
            throws ModelException;

    /**
     * Initiate any new invoked activities.
     *
     * @param step The current Step
     * @param errRep ErrorReporter callback
     * @param sfInstance The state chart instance
     *
     */
    void initiateInvokes(final FlowStep step, final FlowErrorReporter errRep,
            final FlowInstance sfInstance);

    public static <T> T getCurrent(Class<T> type) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> semanticsELStack = getSemanticsELStack(type, contextAttributes);
        return semanticsELStack.peek();
    }

    public static <T> void pushToEL(Class<T> type, T component) {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> semanticsELStack = getSemanticsELStack(type, contextAttributes);
        semanticsELStack.push(component);
    }

    public static <T> void popFromEL(Class<T> type, T component) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<T> semanticsELStack = getSemanticsELStack(type, contextAttributes);

        for (T topComponent = semanticsELStack.peek(); topComponent != component; topComponent = semanticsELStack.peek()) {
            popFromEL(type, topComponent);
        }

        semanticsELStack.pop();
    }

    public static <T> ArrayDeque<T> getSemanticsELStack(Class<T> type, Map<Object, Object> contextAttributes) {
        String keyName = CURRENT_SEMANTICS_STACK_KEY + ":" + type.getName();
        ArrayDeque<T> elStack = (ArrayDeque<T>) contextAttributes.get(keyName);

        if (elStack == null) {
            elStack = new ArrayDeque<>();
            contextAttributes.put(keyName, elStack);
        }
        return elStack;
    }

}
