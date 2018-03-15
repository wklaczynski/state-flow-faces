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
package org.ssoft.faces.state.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowExecutor;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class AsyncTrigger implements Runnable {
    
    public static final Logger log = FlowLogger.FLOW.getLogger();

    private final StateFlowExecutor executor;

    private final List<FlowTriggerEvent> events;

    public AsyncTrigger(final StateFlowExecutor executor, final FlowTriggerEvent event) {
        this.executor = executor;
        this.events = new ArrayList();
        this.events.add(event);
    }

    public AsyncTrigger(final StateFlowExecutor executor) {
        this.executor = executor;
        this.events = new ArrayList();
    }
    
    public boolean isEmpty() {
        return events.isEmpty();
    }

    public boolean contains(FlowTriggerEvent e) {
        return events.contains(e);
    }

    public boolean add(FlowTriggerEvent e) {
        return events.add(e);
    }

    public FlowTriggerEvent get(int index) {
        return events.get(index);
    }

    public FlowTriggerEvent set(int index, FlowTriggerEvent element) {
        return events.set(index, element);
    }

    public void add(int index, FlowTriggerEvent element) {
        events.add(index, element);
    }

    public FlowTriggerEvent remove(int index) {
        return events.remove(index);
    }

    public void start() {
        if (!events.isEmpty()) {
            run();
        }
    }

    @Override
    public void run() {
        try {
            synchronized (executor) {
                FlowTriggerEvent[] evts = events.toArray(new FlowTriggerEvent[events.size()]);
                executor.triggerEvents(evts);
            }
        } catch (ModelException me) {
            log.log(Level.SEVERE, me.getMessage(), me);
        }
    }
}
