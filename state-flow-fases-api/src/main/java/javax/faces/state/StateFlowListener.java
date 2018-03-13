/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface StateFlowListener {

    /**
     * Handle the entry into a TransitionTarget.
     *
     * @param state The TransitionTarget entered
     */
    void onEntry(TransitionTarget state);

    /**
     * Handle the exit out of a TransitionTarget.
     *
     * @param state The TransitionTarget exited
     */
    void onExit(TransitionTarget state);

    /**
     * Handle the transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition taken
     */
    void onTransition(TransitionTarget from, TransitionTarget to, Transition transition);

}

