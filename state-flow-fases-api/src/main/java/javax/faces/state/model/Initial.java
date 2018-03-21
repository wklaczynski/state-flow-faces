/*
 * Copyright 2018 Waldemar Kłaczyński.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    @Override
    protected String createUniqueId(Object element) {
        if(element instanceof Transition) {
            return "start";
        } else if(element instanceof State) {
            return "initial";
        } else {
            throw new IllegalArgumentException("Initial element no support child "
                    + "element type: " + element.getClass().getName());
        }
    }

}

