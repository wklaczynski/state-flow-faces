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
package javax.scxml.semantics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.scxml.TriggerEvent;
import javax.scxml.model.EnterableState;
import javax.scxml.model.History;
import javax.scxml.model.SimpleTransition;
import javax.scxml.model.TransitionalState;

/**
 * A logical unit of progression in the execution of a SCXML model.
 *
 */
public class Step {

    /**
     * The event in this step.
     */
    private final TriggerEvent event;

    /**
     * The set of states that were exited during this step.
     */
    private final Set<EnterableState> exitSet;

    /**
     * The set of states that were entered during this step.
     */
    private final Set<EnterableState> entrySet;

    /**
     * The set of states that were entered during this step by default
     */
    private final Set<EnterableState> defaultEntrySet;

    /**
     * The map of default History transitions to be executed as result of entering states in this step.
     */
    private final Map<TransitionalState, SimpleTransition> defaultHistoryTransitions;

    /**
     * The map of new History configurations created as result of exiting states in this step
     */
    private final Map<History, Set<EnterableState>> newHistoryConfigurations;

    /**
     * The list of Transitions taken during this step.
     */
    private final List<SimpleTransition> transitList;

    /**
     * @param event The event received in this unit of progression
     */
    public Step(TriggerEvent event) {
        this.event = event;
        this.exitSet = new HashSet<>();
        this.entrySet = new HashSet<>();
        this.defaultEntrySet = new HashSet<>();
        this.defaultHistoryTransitions = new HashMap<>();
        this.newHistoryConfigurations = new HashMap<>();
        this.transitList = new ArrayList<>();
    }

    /**
     * Ensure the intermediate state of this step is cleared before start processing the event and/or transitions
     */
    public void clearIntermediateState() {
        exitSet.clear();
        entrySet.clear();
        defaultEntrySet.clear();
        defaultHistoryTransitions.clear();
        newHistoryConfigurations.clear();
    }

    /**
     * @return Returns the entrySet.
     */
    public Set<EnterableState> getEntrySet() {
        return entrySet;
    }

    /**
     * @return Returns the defaultEntrySet.
     */
    public Set<EnterableState> getDefaultEntrySet() {
        return defaultEntrySet;
    }

    /**
     * @return Returns the map of default History transitions to be executed as result of entering states in this step
     */
    public Map<TransitionalState, SimpleTransition> getDefaultHistoryTransitions() {
        return defaultHistoryTransitions;
    }

    /**
     * @return Returns the map of new History configurations created as result of exiting states in this step
     */
    public Map<History, Set<EnterableState>> getNewHistoryConfigurations() {
        return newHistoryConfigurations;
    }

    /**
     * @return Returns the exitSet.
     */
    public Set<EnterableState> getExitSet() {
        return exitSet;
    }

    /**
     * @return Returns the current event.
     */
    public TriggerEvent getEvent() {
        return event;
    }

    /**
     * @return Returns the transitList.
     */
    public List<SimpleTransition> getTransitList() {
        return transitList;
    }
}

