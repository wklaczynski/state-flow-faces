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
package org.apache.common.faces.state.task;

import java.io.Serializable;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;

/**
 *
 * @author Waldemar Kłaczyński
 *
 * DelayedEventTask.
 */
public class DelayedEventTask implements Serializable {

    /**
     * The ID of the &lt;send&gt; element.
     */
    private final String id;

    /**
     * The event
     */
    private final TriggerEvent event;
    
    /**
     * The time
     */
    private final long time;

    /**
     * The target io processor
     */
    private final SCXMLIOProcessor target;

    /**
     * Constructor for events with payload.
     *
     * @param id The ID of the send element.
     * @param event The event to be triggered.
     * @param time
     * @param target The target io processor
     */
    public DelayedEventTask(final String id, final TriggerEvent event, long time, SCXMLIOProcessor target) {
        super();
        this.id = id;
        this.event = event;
        this.time = time;
        this.target = target;
    }

    /**
     *
     * @return
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @return
     */
    public TriggerEvent getEvent() {
        return event;
    }

    /**
     *
     * @return
     */
    public long getTime() {
        return time;
    }

    /**
     *
     * @return
     */
    public SCXMLIOProcessor getTarget() {
        return target;
    }

}
