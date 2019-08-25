/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package org.ssoft.faces.prime;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.event.CancelSystemEvent;
import javax.faces.state.event.SendSystemEvent;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.ParentSCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.SendContext;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ActionExecutionError;
import javax.faces.state.task.DelayedEventTask;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PrimeFlowSendEventListener implements SystemEventListener {

    private final Map<String, DelayedEventTask> tasks = Collections.synchronizedMap(new LinkedHashMap<>());

    @Override
    public void processEvent(SystemEvent se) throws AbortProcessingException {
        FacesContext fc = se.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (se instanceof SendSystemEvent) {

            SendContext sc = ((SendSystemEvent) se).getSendContext();

            String type = sc.getType();

            if (type == null) {
                return;
            }

            String id = sc.getId();
            Map<String, SCXMLIOProcessor> ioProcessors = sc.getIoProcessors();
            String target = sc.getTarget();
            String event = sc.getEvent();

            if ("x-dialog-url".equals(type)) {
                String originType = SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR;
                boolean internal = false;
                SCXMLIOProcessor ioProcessor;
                String origin = target;
                if (target == null) {
                    ioProcessor = ioProcessors.get(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR);
                    origin = SCXMLIOProcessor.SCXML_EVENT_PROCESSOR;
                } else if (ioProcessors.containsKey(target)) {
                    ioProcessor = ioProcessors.get(target);
                    internal = SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target);
                } else if (SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR.equals(target)) {
                    ioProcessor = ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR);
                    internal = true;
                } else {
                    if (target.startsWith(SCXMLIOProcessor.EVENT_PROCESSOR_ALIAS_PREFIX)) {
                        ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                                new EventBuilder(TriggerEvent.ERROR_COMMUNICATION, TriggerEvent.ERROR_EVENT)
                                        .sendId(id).build());
                        throw new AbortProcessingException("<send>: Unavailable target - " + target);
                    } else {
                        ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR).addEvent(
                                new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT)
                                        .sendId(id).build());
                        throw new AbortProcessingException("<send>: Invalid or unsupported target - " + target);
                    }
                }

                if (event == null) {
                    ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR)
                            .addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).sendId(id).build());
                    throw new ActionExecutionError(true, "<send>: Cannot send without event name");
                } else {
                    EventBuilder eventBuilder = new EventBuilder(event, TriggerEvent.SIGNAL_EVENT)
                            .sendId(id)
                            .data(sc.getData());
                    if (!internal) {
                        eventBuilder.origin(origin).originType(originType);
                        if (SCXMLIOProcessor.PARENT_EVENT_PROCESSOR.equals(target)) {
                            eventBuilder.invokeId(((ParentSCXMLIOProcessor) ioProcessor).getInvokeId());
                        }
                        long delay = sc.getDelay();

                        if (delay > 0L) {
                            // Need to execute this one
                            DelayedEventTask eventTask = new DelayedEventTask(id,
                                    eventBuilder.build(),
                                    System.currentTimeMillis() + delay,
                                    ioProcessor);

                            if (!handler.getTimerEventProducer().execute(eventTask)) {
                                tasks.put(id, eventTask);
                            }

                            return;
                        }
                    }
                    ioProcessor.addEvent(eventBuilder.build());
                }

            }
        } else if (se instanceof CancelSystemEvent) {
            String sendId = ((CancelSystemEvent) se).getSendId();
            if (!tasks.containsKey(sendId)) {
                return;
            }
            DelayedEventTask task = tasks.get(sendId);
            if (task != null) {
                if (handler.getTimerEventProducer().cancel(task)) {
                    tasks.remove(sendId);
                }
            }

        }

    }

    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof SendContext || source instanceof String;
    }

}
