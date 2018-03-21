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

import java.util.Collection;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Cancel extends Action {

    /**
     * Constructor.
     */
    public Cancel() {
        super();
    }

    /**
     * The ID of the send message that should be cancelled.
     */
    private String sendid;

    /**
     * Get the ID of the send message that should be cancelled.
     *
     * @return Returns the sendid.
     */
    public String getSendid() {
        return sendid;
    }

    /**
     * Set the ID of the send message that should be cancelled.
     *
     * @param sendid The sendid to set.
     */
    public void setSendid(final String sendid) {
        this.sendid = sendid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {
        evtDispatcher.cancel(sendid);
    }

    @Override
    public String toString() {
        return "Cancel{" + "sendid=" + sendid + '}';
    }

}
