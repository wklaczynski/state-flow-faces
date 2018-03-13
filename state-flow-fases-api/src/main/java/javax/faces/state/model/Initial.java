/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Initial extends TransitionTarget {

    /**
     * A conditionless transition that is always enabled and will be taken
     * as soon as the state is entered. The target of the transition must
     * be a descendant of the parent state of initial.
     */
    private Transition transition;

    /**
     * Constructor.
     */
    public Initial() {
        super();
    }

    /**
     * Get the initial transition.
     *
     * @return Returns the transition.
     */
    public final Transition getTransition() {
        return transition;
    }

    /**
     * Set the initial transition.
     *
     * @param transition The transition to set.
     */
    public final void setTransition(final Transition transition) {
        this.transition = transition;
        this.transition.setParent(this);
    }

}

