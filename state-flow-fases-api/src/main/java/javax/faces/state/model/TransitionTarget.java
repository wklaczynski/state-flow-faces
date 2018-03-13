/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class TransitionTarget implements Serializable {

    /**
     * Identifier for this transition target. Other parts of the SCXML document
     * may refer to this &lt;state&gt; using this ID.
     */
    private String id;

    /**
     * Optional property holding executable content to be run upon entering this
     * transition target.
     */
    private OnEntry onEntry;

    /**
     * Optional property holding executable content to be run upon exiting this
     * transition target.
     */
    private OnExit onExit;

    /**
     * Optional property holding the data model for this transition target.
     */
    private Datamodel datamodel;

    /**
     * The parent of this transition target (may be null, if the parent is the
     * document root).
     */
    private TransitionTarget parent;

    /**
     * A list of outgoing Transitions from this target, by document order.
     */
    private final List<Transition> transitions;

    /**
     * List of history states owned by a given state (applies to non-leaf
     * states).
     */
    private final List<History> history;

    /**
     * Constructor.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public TransitionTarget() {
        super();
//        onEntry = new OnEntry(); //empty defaults
//        onEntry.setParent(this);
//        onExit = new OnExit();   //empty defaults
//        onExit.setParent(this);
        parent = null;
        transitions = new ArrayList<>();
        history = new ArrayList<>();
    }

    /**
     * Get the identifier for this transition target (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this transition target.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the onentry property.
     *
     * @return Returns the onEntry.
     */
    public final OnEntry getOnEntry() {
        return onEntry;
    }

    /**
     * Set the onentry property.
     *
     * @param onEntry The onEntry to set.
     */
    public final void setOnEntry(final OnEntry onEntry) {
        this.onEntry = onEntry;
        this.onEntry.setParent(this);
    }

    /**
     * Get the onexit property.
     *
     * @return Returns the onExit.
     */
    public final OnExit getOnExit() {
        return onExit;
    }

    /**
     * Set the onexit property.
     *
     * @param onExit The onExit to set.
     */
    public final void setOnExit(final OnExit onExit) {
        this.onExit = onExit;
        this.onExit.setParent(this);
    }

    /**
     * Get the data model for this transition target.
     *
     * @return Returns the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model for this transition target.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Get the parent TransitionTarget.
     *
     * @return Returns the parent state (null if parent is &lt;scxml&gt;
     * element)
     */
    public final TransitionTarget getParent() {
        return parent;
    }

    /**
     * Set the parent TransitionTarget.
     *
     * @param parent The parent state to set
     */
    public final void setParent(final TransitionTarget parent) {
        this.parent = parent;
    }

    /**
     * Get the list of all outgoing transitions from this target, that will be
     * candidates for being fired on the given event.
     *
     * @param event The event
     * @return List Returns the candidate transitions for given event
     */
    public final List<Transition> getTransitionsList(final String event) {
        List matchingTransitions = null; // TODO v1.0 we returned null <= v0.6
        for (int i = 0; i < transitions.size(); i++) {
            Transition t = (Transition) transitions.get(i);
            if ((event == null && t.getEvent() == null)
                    || (event != null && event.equals(t.getEvent()))) {
                if (matchingTransitions == null) {
                    matchingTransitions = new ArrayList();
                }
                matchingTransitions.add(t);
            }
        }
        return matchingTransitions;
    }

    /**
     * Add a transition to the map of all outgoing transitions for this
     * transition target.
     *
     * @param transition The transitions to set.
     */
    public final void addTransition(final Transition transition) {
        transitions.add(transition);
        transition.setParent(this);
    }

    /**
     * Get the outgoing transitions for this target as a java.util.List.
     *
     * @return List Returns the transitions list.
     */
    public final List<Transition> getTransitionsList() {
        return transitions;
    }

    /**
     * This method is used by XML digester.
     *
     * @param h History pseudo state
     */
    public final void addHistory(final History h) {
        history.add(h);
        h.setParent(this);
    }

    /**
     * Does this state have a history pseudo state.
     *
     * @return boolean true if a given state contains at least one history
     * pseudo state
     */
    public final boolean hasHistory() {
        return (!history.isEmpty());
    }

    /**
     * Get the list of history pseudo states for this state.
     *
     * @return a list of all history pseudo states contained by a given state
     * (can be empty)
     * @see #hasHistory()
     */
    public final List<History> getHistory() {
        return history;
    }

}
