/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.faces.state.model.State;
import javax.faces.state.utils.StateFlowHelper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowStatus implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The states that are currently active.
     */
    private final Set<State> states;

    /**
     * The events that are currently queued.
     */
    private final Collection events;

    /**
     * Have we reached a final configuration for this state machine.
     *
     * True - if all the states are final and there are not events pending from
     * the last step. False - otherwise.
     *
     * @return Whether a final configuration has been reached.
     */
    public boolean isFinal() {
        boolean rslt = true;
        for (State state : states) {
            if (!state.isFinal()) {
                rslt = false;
                break;
            }
            //the status is final only iff these are top-level states
            if (state.getParent() != null) {
                rslt = false;
                break;
            }
        }
        if (!events.isEmpty()) {
            rslt = false;
        }
        return rslt;
    }

    /**
     * Constructor.
     */
    public FlowStatus() {
        states = new HashSet();
        events = new ArrayList();
    }

    /**
     * Get the states configuration (leaf only).
     *
     * @return Returns the states configuration - simple (leaf) states only.
     */
    public Set<State> getStates() {
        return states;
    }

    /**
     * Get the events that are currently queued.
     *
     * @return The events that are currently queued.
     */
    public Collection<FlowTriggerEvent> getEvents() {
        return events;
    }

    /**
     * Get the complete states configuration.
     *
     * @return complete states configuration including simple states and their
     * complex ancestors up to the root.
     */
    public Set<State> getAllStates() {
        return StateFlowHelper.getAncestorClosure(states, null);
    }

}
