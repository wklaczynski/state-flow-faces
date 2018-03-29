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
package org.apache.common.scxml;

import java.util.logging.Logger;
import org.apache.common.scxml.model.EnterableState;
import org.apache.common.scxml.model.Invoke;
import org.apache.common.scxml.model.ModelException;
import org.apache.common.scxml.model.SCXML;


/**
 * ActionExecutionContext providing restricted access to the SCXML model, instance and services needed
 * for the execution of {@link org.apache.commons.scxml2.model.Action} instances
 */
public class InvokeContext {

    /**
     * The SCXML execution context this action exection context belongs to
     */
    private final SCXMLExecutionContext exctx;
    private final Invoke invoke;

    /**
     * Constructor
     * @param exctx The SCXML execution context this action execution context belongs to
     * @param invoke The SCXML invoke for this invoker
     */
    public InvokeContext(SCXMLExecutionContext exctx, Invoke invoke) {
        this.exctx = exctx;
        this.invoke = invoke;
    }

    /**
     * @return Returns the state machine
     */
    public SCXML getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * Return the {@link EnterableState} whose
     * {@link org.apache.commons.scxml2.Context} this action executes in.
     *
     * @return The parent {@link EnterableState}
     * @throws ModelException For an unknown EnterableState subclass
     */
    public EnterableState getEnterableState() throws ModelException {
        return invoke.getParentEnterableState();
    }
    
    /**
     * @return Returns the global context
     */
    public Context getGlobalContext() {
        return exctx.getScInstance().getGlobalContext();
    }

    /**
     * @return Returns the global context
     * @throws ModelException For an unknown EnterableState subclass
     */
    public Context getContext() throws ModelException {
        return getContext(getEnterableState());
    }
    
    /**
     * @param state an EnterableState
     * @return Returns the context for an EnterableState
     */
    public Context getContext(EnterableState state) {
        return exctx.getScInstance().getContext(state);
    }

    /**
     * @return Returns The evaluator.
     */
    public Evaluator getEvaluator() {
        return exctx.getEvaluator();
    }

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return exctx.getEventDispatcher();
    }

    /**
     * @return Returns the I/O Processor for the internal event queue
     */
    public SCXMLIOProcessor getInternalIOProcessor() {
        return exctx;
    }

    /**
     * @return Returns the SCXML Execution Logger for the application
     */
    public Logger getAppLog() {
        return exctx.getAppLog();
    }
}