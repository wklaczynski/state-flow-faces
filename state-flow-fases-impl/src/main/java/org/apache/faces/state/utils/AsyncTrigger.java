/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.utils;

import java.util.ArrayList;
import java.util.List;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class AsyncTrigger implements Runnable {


    private final StateFlowExecutor executor;

    private final List<FlowTriggerEvent> events;

    private final Log log = LogFactory.getLog(AsyncTrigger.class);

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
            log.error(me.getMessage(), me);
        }
    }
}
