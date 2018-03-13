/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.state.model.State;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowOnStopEvent {

    State state;

    public FlowOnStopEvent(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }

}
