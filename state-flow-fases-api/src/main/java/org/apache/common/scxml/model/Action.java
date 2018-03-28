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

import java.io.Serializable;
import org.apache.common.scxml.ActionExecutionContext;
import org.apache.common.scxml.SCXMLExpressionException;

/**
 * An abstract base class for executable elements in SCXML, such as
 * &lt;assign&gt;, &lt;log&gt; etc.
 *
 */
public abstract class Action implements UniqueClientId, Serializable {

    /**
     * Link to its parent or container.
     */
    private Executable parent;

    /**
     * Constructor.
     */
    public Action() {
        super();
        this.parent = null;
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * <p>
     * The assigned client identifier for this state.</p>
     */
    private String clientId = null;

    /**
     * Return the {@link EnterableState} whose
     * {@link org.apache.commons.scxml2.Context} this action executes in.
     *
     * @return The parent {@link EnterableState}
     * @throws ModelException For an unknown EnterableState subclass
     *
     * @since 0.9
     */
    public EnterableState getParentEnterableState()
            throws ModelException {
        if (parent == null && this instanceof Script && ((Script) this).isGlobalScript()) {
            // global script doesn't have a EnterableState
            return null;
        } else if (parent == null) {
            throw new ModelException("Action "
                    + this.getClass().getName() + " instance missing required parent TransitionTarget");
        }
        return parent.getParent();
    }

    /**
     * Execute this action instance.
     *
     * @param exctx The ActionExecutionContext for this execution instance
     *
     * @throws ModelException If the execution causes the model to enter a
     * non-deterministic state.
     * @throws SCXMLExpressionException If the execution involves trying to
     * evaluate an expression which is malformed.
     * @throws ActionExecutionError to be thrown if the execution caused an
     * error (event) to be raised, which then shall stop execution of (possible)
     * following actions within the same executable content block
     */
    public abstract void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException, ActionExecutionError;

    /**
     * Get the identifier for this ecutable.
     *
     * @return Returns the unique client id.
     */
    @Override
    public String getClientId() {
        return clientId;
    }

    /**
     * Set the identifier for this transition target.
     *
     * @param clientId The clientId to set.
     */
    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + clientId + '}';
    }
    
}
