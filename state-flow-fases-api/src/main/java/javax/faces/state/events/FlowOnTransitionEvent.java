/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowOnTransitionEvent {

    TransitionTarget from;
    TransitionTarget to;
    Transition transition;

    public FlowOnTransitionEvent(TransitionTarget from, TransitionTarget to, Transition transition) {
        this.from = from;
        this.to = to;
        this.transition = transition;
    }

    public TransitionTarget getFrom() {
        return from;
    }

    public TransitionTarget getTo() {
        return to;
    }

    public Transition getTransition() {
        return transition;
    }

}
