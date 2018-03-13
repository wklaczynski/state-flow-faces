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
public class History extends TransitionTarget {

    /**
     * Whether this is a shallow or deep history, the default is shallow.
     */
    private boolean isDeep;

    /**
     * A conditionless transition representing the default history state
     * and indicates the state to transition to if the parent state has
     * never been entered before.
     */
    private Transition transition;

    /**
     * Default no-args constructor for XML Digester.
     */
    public History() {
        super();
    }

    /**
     * Get the transition.
     *
     * @return Returns the transition.
     */
    public final Transition getTransition() {
        return transition;
    }

    /**
     * Set the transition.
     *
     * @param transition The transition to set.
     */
    public final void setTransition(final Transition transition) {
        this.transition = transition;
        this.transition.setParent(this);
    }

    /**
     * Is this history &quot;deep&quot; (as against &quot;shallow&quot;).
     *
     * @return Returns whether this is a &quot;deep&quot; history
     */
    public final boolean isDeep() {
        return isDeep;
    }

    /**
     * This method is invoked by XML digester when parsing SCXML markup.
     *
     * @param type The history type, which can be &quot;shallow&quot; or
     * &quot;deep&quot;
     */
    public final void setType(final String type) {
        if (type.equals("deep")) {
            isDeep = true;
        }
        //shallow is by default
    }

}
