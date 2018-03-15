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
import java.util.List;
import java.util.Set;
import javax.faces.context.FacesContext;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import static javax.faces.state.model.StateChart.STATE_MACHINE_HINT;
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
    private final Collection<FlowTriggerEvent> events;

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

    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[2];
        
        values[0] = saveEventsState(context);
        values[1] = saveStatesState(context);

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
        
        if(values[0] != null) {
            events.clear();
            events.addAll(restoreEventsState(context, values[0]));
        }
        if(values[1] != null) {
            states.clear();
            states.addAll(restoreStatesState(context, values[1]));
        }
    }

    private Object saveEventsState(FacesContext context) {
        Object state = null;
        if (null != events && events.size() > 0) {
            Object[] attached = new Object[events.size()];
            int i = 0;
            for (FlowTriggerEvent event : events) {
                attached[i++] = event.saveState(context);
            }
            state = attached;
        }
        return state;
    }

    private Collection<FlowTriggerEvent> restoreEventsState(FacesContext context, Object state) {
        if (null != state) {
            Object[] values = (Object[]) state;
            List<FlowTriggerEvent> result = new ArrayList<>(values.length);
            for (Object value : values) {
                FlowTriggerEvent event = new FlowTriggerEvent("", 0);
                event.restoreState(context, value);
                result.add(event);
            }
            return result;
        }
        return null;
    }

    private Object saveStatesState(FacesContext context) {
        Object state = null;
        if (null != states && states.size() > 0) {
            Object[] attached = new Object[states.size()];
            int i = 0;
            for (State fstate : states) {
                attached[i++] = fstate.getClientId();
            }
            state = attached;
        }
        return state;
    }

    private Collection<State> restoreStatesState(FacesContext context, Object state) {
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);
        
        if (null != state) {
            Object[] values = (Object[]) state;
            List<State> result = new ArrayList<>(values.length);
            for (Object value : values) {
                String stid = (String) value;
                Object found = chart.findElement(stid);
                if(found != null){
                    State fstate = (State) found;
                    result.add(fstate);
                } else {
                    throw new IllegalStateException(String.format("Restored element %s not found.", stid));
                }
            }
            return result;
        }
        return null;
    }
    
}
