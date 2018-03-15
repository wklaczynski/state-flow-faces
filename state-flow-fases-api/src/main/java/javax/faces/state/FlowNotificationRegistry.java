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
package javax.faces.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowNotificationRegistry implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Map of all listeners keyed by Observable.
     */
    private final Map regs = new HashMap();

    /**
     * Constructor.
     */
    public FlowNotificationRegistry() {
        super();
    }

    /**
     * Register this SCXMLListener for this Observable.
     *
     * @param source The observable this listener wants to listen to
     * @param lst The listener
     */
    synchronized void addListener(final Object source, final StateFlowListener lst) {
        Set entries = (Set) regs.get(source);
        if (entries == null) {
            entries = new LinkedHashSet();
            regs.put(source, entries);
        }
        entries.add(lst);
    }

    /**
     * Deregister this SCXMLListener for this Observable.
     *
     * @param source The observable this listener wants to stop listening to
     * @param lst The listener
     */
    synchronized void removeListener(final Object source, final StateFlowListener lst) {
        Set entries = (Set) regs.get(source);
        if (entries != null) {
            entries.remove(lst);
            if (entries.isEmpty()) {
                regs.remove(source);
            }
        }
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * entered.
     *
     * @param observable The Observable
     * @param state The TransitionTarget that was entered
     */
    public void fireOnEntry(final TransitionTarget observable, final TransitionTarget state) {
        Object source = observable;
        fireOnEntry(source, state);
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * entered.
     *
     * @param observable The Observable
     * @param state The TransitionTarget that was entered
     */
    public void fireOnEntry(final StateChart observable,
            final TransitionTarget state) {
        Object source = observable;
        fireOnEntry(source, state);
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * entered.
     *
     * @param source The Observable
     * @param state The TransitionTarget that was entered
     */
    private synchronized void fireOnEntry(final Object source,
            final TransitionTarget state) {
        Set entries = (Set) regs.get(source);
        if (entries != null) {
            for (Iterator iter = entries.iterator(); iter.hasNext();) {
                StateFlowListener lst = (StateFlowListener) iter.next();
                lst.onEntry(state);
            }
        }
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * exited.
     *
     * @param observable The Observable
     * @param state The TransitionTarget that was exited
     */
    public void fireOnExit(final TransitionTarget observable,
            final TransitionTarget state) {
        Object source = observable;
        fireOnExit(source, state);
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * exited.
     *
     * @param observable The Observable
     * @param state The TransitionTarget that was exited
     */
    public void fireOnExit(final StateChart observable,
            final TransitionTarget state) {
        Object source = observable;
        fireOnExit(source, state);
    }

    /**
     * Inform all relevant listeners that a TransitionTarget has been
     * exited.
     *
     * @param source The Observable
     * @param state The TransitionTarget that was exited
     */
    private synchronized void fireOnExit(final Object source,
            final TransitionTarget state) {
        Set entries = (Set) regs.get(source);
        if (entries != null) {
            for (Iterator iter = entries.iterator(); iter.hasNext();) {
                StateFlowListener lst = (StateFlowListener) iter.next();
                lst.onExit(state);
            }
        }
    }

    /**
     * Inform all relevant listeners of a transition that has occured.
     *
     * @param observable The Observable
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that was taken
     */
    public void fireOnTransition(final Transition observable,
            final TransitionTarget from, final TransitionTarget to,
            final Transition transition) {
        Object source = observable;
        fireOnTransition(source, from, to, transition);
    }

    /**
     * Inform all relevant listeners of a transition that has occured.
     *
     * @param observable The Observable
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that was taken
     */
    public void fireOnTransition(final StateChart observable,
            final TransitionTarget from, final TransitionTarget to,
            final Transition transition) {
        Object source = observable;
        fireOnTransition(source, from, to, transition);
    }

    /**
     * Inform all relevant listeners of a transition that has occured.
     *
     * @param source The Observable
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that was taken
     */
    private synchronized void fireOnTransition(final Object source,
            final TransitionTarget from, final TransitionTarget to,
            final Transition transition) {
        Set entries = (Set) regs.get(source);
        if (entries != null) {
            for (Iterator iter = entries.iterator(); iter.hasNext();) {
                StateFlowListener lst = (StateFlowListener) iter.next();
                lst.onTransition(from, to, transition);
            }
        }
    }

}

