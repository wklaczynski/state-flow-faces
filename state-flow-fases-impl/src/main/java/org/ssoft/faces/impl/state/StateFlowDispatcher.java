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
package org.ssoft.faces.impl.state;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.state.event.CancelSystemEvent;
import javax.faces.state.event.SendSystemEvent;
import javax.faces.state.task.DelayedEventTask;
import javax.faces.state.task.FacesProcessHolder;
import javax.faces.state.task.TimerEventProducer;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.ParentSCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.scxml.SendContext;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.io.StateHolder;
import javax.faces.state.scxml.model.ActionExecutionError;

/**
 * <p>
 * EventDispatcher implementation that can execute <code>delay</code>ed
 * &lt;send&gt; events for the &quot;scxml&quot; <code>type</code> attribute
 * value (which is also the default). This implementation uses J2SE
 * <code>Timer</code>s.</p>
 *
 * <p>
 * No other <code>type</code>s are processed. Subclasses may support additional
 * <code>type</code>s by overriding the <code>send(...)</code> and
 * <code>cancel(...)</code> methods and delegating to their <code>super</code>
 * counterparts for the &quot;scxml&quot; <code>type</code>.</p>
 *
 */
public class StateFlowDispatcher implements EventDispatcher, FacesProcessHolder, StateHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Implementation independent log category.
     */
    protected static final Logger log = Logger.getLogger("javax.faces.state");

    /**
     * Timer Event producer.
     */
    private final TimerEventProducer timerEventProducer;

    /**
     * The <code>Map</code> of active <code>Timer</code>s, keyed by &lt;send&gt;
     * element <code>id</code>s.
     */
    private final Map<String, DelayedEventTask> tasks = Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     *
     * @param timerEventProducer
     */
    public StateFlowDispatcher(TimerEventProducer timerEventProducer) {
        this.timerEventProducer = timerEventProducer;
    }

    /**
     * Get the log instance.
     *
     * @return The current log instance
     */
    protected Logger getLog() {
        return log;
    }

    /**
     * Get the current tasks.
     *
     * @return The currently scheduled tasks
     */
    protected Map<String, DelayedEventTask> getTasks() {
        return tasks;
    }

    @Override
    public StateFlowDispatcher newInstance() {
        return new StateFlowDispatcher(timerEventProducer);
    }

    /**
     * @see EventDispatcher#cancel(String)
     */
    @Override
    public void cancel(final String sendId) {
        if (!tasks.containsKey(sendId)) {
            try {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getApplication().publishEvent(fc, CancelSystemEvent.class, String.class, sendId);
            } catch (AbortProcessingException ape) {
                throw new ActionExecutionError(true, ape.getMessage());
            }
            return; // done, we don't track this one or its already expired
        }
        DelayedEventTask task = tasks.get(sendId);
        if (task != null) {
            if (timerEventProducer.cancel(task)) {
                tasks.remove(sendId);
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "cancel( sendId: {0})", sendId);
                }
            }
        }
    }

    /**
     * @see EventDispatcher#send(javax.faces.state.scxml.SendContext)
     */
    @Override
    public void send(final SendContext sctx) {

        Context ctx = sctx.getCurrentContext();
        final String id = sctx.getId();
        final String target = sctx.getTarget();
        final String type = sctx.getType();
        final String event = sctx.getEvent();
        final Object data = sctx.getData();
        final Object hints = sctx.getHints();
        final long delay = sctx.getDelay();

        // We only handle the "scxml" type (which is the default too) and optionally the #_internal target
        if (type == null || type.equalsIgnoreCase(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR)
                || type.equals(SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR)) {
            String originType = SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR;
            SCXMLIOProcessor ioProcessor = null;
            String invokeId = null;

            boolean internal = false;

            final Map<String, SCXMLIOProcessor> ioProcessors = (Map<String, SCXMLIOProcessor>) ctx.get(SCXMLSystemContext.IOPROCESSORS_KEY);

            String origin = target;
            if (target == null) {
                ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
                origin = SCXMLIOProcessor.SCXML_EVENT_PROCESSOR;
            } else if (ioProcessors.containsKey(target)) {
                ioProcessor = ioProcessors.get(target);
                internal = SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target);
                if (ioProcessor == null
                        && target.startsWith(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX)
                        && !target.startsWith(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX)) {
                    
                    invokeId = target.substring(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX.length());
                    ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
                    origin = SCXMLIOProcessor.INVOKE_EVENT_PROCESSOR;
                }
            } else if (SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target)) {
                ioProcessor = ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR);
                internal = true;
            } else if (target.startsWith(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX)
                    && !target.startsWith(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX)) {
                invokeId = target.substring(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX.length());
                ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
                origin = SCXMLIOProcessor.INVOKE_EVENT_PROCESSOR;
            } else {
                if (target.startsWith(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX)) {
                    ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                            new EventBuilder(TriggerEvent.ERROR_COMMUNICATION, TriggerEvent.ERROR_EVENT)
                                    .sendId(id).build());
                    throw new ActionExecutionError(true, "<send>: Unavailable target - " + target);
                } else {
                    ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                            new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT)
                                    .sendId(id).build());
                    throw new ActionExecutionError(true, "<send>: Invalid or unsupported target - " + target);
                }
            }

            if (event == null) {
                ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR)
                        .addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).sendId(id).build());
                throw new ActionExecutionError(true, "<send>: Cannot send without event name");
            } else {
                EventBuilder eventBuilder = new EventBuilder(event, TriggerEvent.SIGNAL_EVENT)
                        .sendId(id)
                        .data(data)
                        .invokeId(invokeId);
                if (!internal) {
                    eventBuilder.origin(origin).originType(originType);
                    if (SCXMLIOProcessor.PARENT_EVENT_PROCESSOR.equals(target)) {
                        eventBuilder.invokeId(((ParentSCXMLIOProcessor) ioProcessor).getInvokeId());
                    }
                    if (delay > 0L) {
                        // Need to execute this one
                        DelayedEventTask eventTask = new DelayedEventTask(id,
                                eventBuilder.build(),
                                System.currentTimeMillis() + delay,
                                ioProcessor);

                        if (!timerEventProducer.execute(eventTask)) {
                            tasks.put(id, eventTask);
                        }

                        if (log.isLoggable(Level.FINE)) {
                            log.log(Level.FINE, "Scheduled event ''{0}'' with delay {1}ms, as specified by <send> with id ''{2}''", new Object[]{event, delay, id});
                        }
                        return;
                    }
                }
                ioProcessor.addEvent(eventBuilder.build());
            }
        } else {
            try {
                FacesContext fc = FacesContext.getCurrentInstance();
                fc.getApplication().publishEvent(fc, SendSystemEvent.class, SendContext.class, sctx);
            } catch (AbortProcessingException ape) {
                throw new ActionExecutionError(true, ape.getMessage());
            }
        }
    }

    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[3];

        values[0] = saveTasksState(context);

        return values;
    }

    @Override
    public void restoreState(Context context, Object state) {
        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        restoreInvokersState(context, values[0]);

    }

    private Object saveTasksState(Context context) {
        Object state = null;
        if (null != tasks && tasks.size() > 0) {
            Object[] attached = new Object[tasks.size()];
            int i = 0;
            for (Map.Entry<String, DelayedEventTask> entry : tasks.entrySet()) {
                Object values[] = new Object[3];

                DelayedEventTask task = entry.getValue();
                values[0] = task.getId();
                values[1] = task.getTime();
                values[2] = task.getEvent();
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreInvokersState(Context context, Object state) {
        tasks.clear();

        Map<String, SCXMLIOProcessor> ioProcessors
                                      = (Map<String, SCXMLIOProcessor>) context.get(SCXMLSystemContext.IOPROCESSORS_KEY);

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String id = (String) entry[0];
                long time = (long) entry[1];
                TriggerEvent triggerEvent = (TriggerEvent) entry[2];

                String target = triggerEvent.getOrigin();
                SCXMLIOProcessor ioProcessor = ioProcessors.get(target);

                DelayedEventTask eventTask = new DelayedEventTask(id,
                        triggerEvent,
                        time,
                        ioProcessor);

                tasks.put(id, eventTask);
            }
        }
    }

    @Override
    public void processDecodes(FacesContext context) {
        Set<String> keys = new LinkedHashSet<>(tasks.keySet());
        for (String key : keys) {
            DelayedEventTask task = tasks.get(key);
            if (timerEventProducer.execute(task)) {
                tasks.remove(key);
            }
        }
    }

    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        List<DelayedEventTask> taskList = new ArrayList<>(tasks.values());
        timerEventProducer.encodeBegin(taskList);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        timerEventProducer.encodeEnd();
    }

}
