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
package org.apache.common.scxml.model;

import java.util.logging.Level;
import org.apache.common.scxml.ActionExecutionContext;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExpressionException;
import org.apache.common.scxml.TriggerEvent;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;raise&gt; SCXML element.
 *
 * @since 2.0
 */
public class Raise extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The event to be generated.
     */
    private String event;

    /**
     * Constructor.
     */
    public Raise() {
        super();
    }

    /**
     * Get the event.
     *
     * @return Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * Set the event.
     *
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {

        if (exctx.getAppLog().isLoggable(Level.FINE)) {
            exctx.getAppLog().log(Level.FINE, "<raise>: Adding event ''{0}'' to list of derived events.", event);
        }
        TriggerEvent ev = new EventBuilder(event, TriggerEvent.SIGNAL_EVENT).build();
        exctx.getInternalIOProcessor().addEvent(ev);

    }

}