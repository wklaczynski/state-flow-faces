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
package javax.faces.state.scxml.model;

import java.util.logging.Level;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.SCXMLExpressionException;


/**
 * The class in this SCXML object model that corresponds to the
 * &lt;cancel&gt; SCXML element.
 *
 */
public class Cancel extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

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
     * The expression that evaluates to the ID of the send message that should be cancelled.
     */
    private String sendidexpr;

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
     * Get the expression that evaluates to the ID of the send message that should be cancelled.
     * 
     * @return the expression that evaluates to the ID of the send message that should be cancelled.
     */
    public String getSendidexpr() {
        return sendidexpr;
    }

    /**
     * Set the expression that evaluates to the ID of the send message that should be cancelled.
     * 
     * @param sendidexpr the expression that evaluates to the ID of the send message that should be cancelled.
     */
    public void setSendidexpr(String sendidexpr) {
        this.sendidexpr = sendidexpr;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        Evaluator eval = exctx.getEvaluator();

        String sendidValue = sendid;
        if (sendidValue == null && sendidexpr != null) {
            sendidValue = (String) eval.eval(ctx, sendidexpr);
            if ((sendidValue == null || sendidValue.trim().length() == 0)
                    && exctx.getAppLog().isLoggable(Level.WARNING)) {
                exctx.getAppLog().log(Level.WARNING, "<send>: sendid expression \"{0}\" evaluated to null or empty String", sendidexpr);
            }
        }

        exctx.getEventDispatcher().cancel(sendidValue);
    }
}

