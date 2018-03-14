/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Parallel extends TransitionTarget {

    /**
     * The set of parallel state machines contained in this &lt;parallel&gt;.
     */
    private final Set<TransitionTarget> children;

    /**
     * Constructor.
     */
    public Parallel() {
        this.children = new LinkedHashSet();
    }

    /**
     * Get the set of child transition targets (may be empty).
     *
     * @return Set Returns the children.
     */
    public final Set<TransitionTarget> getChildren() {
        return children;
    }

    /**
     * Add a child.
     *
     * @param tt A child transition target.
     */
    public final void addChild(final TransitionTarget tt) {
        // TODO: State is a sufficient enough type for the parameter
        this.children.add(tt);
        tt.setParent(this);
    }

}

