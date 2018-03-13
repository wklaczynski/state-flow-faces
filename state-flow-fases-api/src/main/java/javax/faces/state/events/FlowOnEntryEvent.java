/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowOnEntryEvent {

    TransitionTarget target;

    public FlowOnEntryEvent(TransitionTarget target) {
        this.target = target;
    }

    public TransitionTarget getTarget() {
        return target;
    }
}
