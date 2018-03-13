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
public abstract class Executable implements Serializable {

    /**
     * The set of executable elements (those that inheriting from Action) that
     * are contained in this Executable.
     */
    private final List actions;

    /**
     * The parent container, for traceability.
     */
    private TransitionTarget parent;

    /**
     * Constructor.
     */
    public Executable() {
        super();
        this.actions = new ArrayList();
    }

    /**
     * Get the executable actions contained in this Executable.
     *
     * @return Returns the actions.
     */
    public final List getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in this
     * Executable.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * Get the TransitionTarget parent.
     *
     * @return Returns the parent.
     */
    public final TransitionTarget getParent() {
        return parent;
    }

    /**
     * Set the TransitionTarget parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final TransitionTarget parent) {
        this.parent = parent;
    }
    
}
