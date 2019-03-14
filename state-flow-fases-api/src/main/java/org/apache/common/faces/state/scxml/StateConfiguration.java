/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.faces.state.scxml;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import static org.apache.common.faces.state.scxml.SCXMLConstants.STATE_MACHINE_HINT;
import org.apache.common.faces.state.scxml.model.EnterableState;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 * The current active states of a state machine
 */
public class StateConfiguration implements Serializable {
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The states that are currently active.
     */
    private final Set<EnterableState> activeStates = new HashSet<>();
    private final Set<EnterableState> activeStatesSet = Collections.unmodifiableSet(activeStates);

    /**
     * The atomic states that are currently active.
     */
    private final Set<EnterableState> atomicStates = new HashSet<>();
    private final Set<EnterableState> atomicStatesSet = Collections.unmodifiableSet(atomicStates);

    /**
     * Get the active states
     *
     * @return active states including simple states and their
     *         complex ancestors up to the root.
     */
    public Set<EnterableState> getActiveStates() {
        return  activeStatesSet;
    }

    /**
     * Get the current atomic states (leaf only).
     *
     * @return Returns the atomic states - simple (leaf) states only.
     */
    public Set<EnterableState> getStates() {
        return  atomicStatesSet;
    }

    /**
     * Enter an active state
     * If the state is atomic also record it add it to the current states
     * @param state state to enter
     */
    public void enterState(final EnterableState state) {
        if (!activeStates.add(state)) {
            throw new IllegalStateException("State "+state.getId()+" already added.");
        }
        if (state.isAtomicState()) {
            if (!atomicStates.add(state)) {
                throw new IllegalStateException("Atomic state "+state.getId()+" already added.");
            }
        }
    }

    /**
     * Exit an active state
     * If the state is atomic also remove it from current states
     * @param state state to exit
     */
    public void exitState(final EnterableState state) {
        if (!activeStates.remove(state)) {
            throw new IllegalStateException("State "+state.getId()+" not active.");
        }
        atomicStates.remove(state);
    }

    /**
     * Clear the state configuration
     */
    public void clear() {
        activeStates.clear();
        atomicStates.clear();
    }
    
    /**
     *
     * @param context
     * @return
     */
    public Object saveState(Context context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[2];

        values[0] = saveActiveStates(context);
        values[1] = saveAtomicStates(context);

        return values;
    }

    /**
     *
     * @param context
     * @param state
     */
    public void restoreState(Context context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        SCXML chart = (SCXML) context.get(STATE_MACHINE_HINT);
        
        
        restoreActiveStates(context, chart, values[0]);
        restoreAtomicStates(context, chart, values[1]);

    }
    
    private Object saveActiveStates(Context context) {
        Object state = null;
        if (null != activeStates && activeStates.size() > 0) {
            Object[] attached = new Object[activeStates.size()];
            int i = 0;
            for (EnterableState fstate : activeStates) {
                attached[i++] = fstate.getClientId();
            }
            state = attached;
        }
        return state;
    }

    private void restoreActiveStates(Context context, SCXML chart, Object state) {
        activeStates.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                String ttid = (String) value;
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                EnterableState tt = (EnterableState) found;
                activeStates.add(tt);
            }
        }
    }
    
    private Object saveAtomicStates(Context context) {
        Object state = null;
        if (null != atomicStates && atomicStates.size() > 0) {
            Object[] attached = new Object[atomicStates.size()];
            int i = 0;
            for (EnterableState fstate : atomicStates) {
                attached[i++] = fstate.getClientId();
            }
            state = attached;
        }
        return state;
    }

    private void restoreAtomicStates(Context context, SCXML chart, Object state) {
        atomicStates.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                String ttid = (String) value;
                Object found = chart.findElement(ttid);
                if (found == null) {
                    throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
                }

                EnterableState tt = (EnterableState) found;
                atomicStates.add(tt);
            }
        }
    }
    
    
    
    
}
