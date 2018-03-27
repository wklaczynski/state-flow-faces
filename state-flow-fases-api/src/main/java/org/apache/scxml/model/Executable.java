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
package org.apache.scxml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.scxml.UniqueIdGenerator;

/**
 * An abstract base class for containers of executable elements in SCXML, such
 * as &lt;onentry&gt; and &lt;onexit&gt;.
 *
 */
public abstract class Executable implements UniqueIdGenerator, Serializable {

    /**
     * The set of executable elements (those that inheriting from Action) that
     * are contained in this Executable.
     */
    private final List<Action> actions;

    /**
     * The parent container, for traceability.
     */
    private EnterableState parent;

    /**
     * Constructor.
     */
    public Executable() {
        this.actions = new ArrayList<>();
    }

    /**
     * Get the executable actions contained in this Executable.
     *
     * @return Returns the actions.
     */
    public final List<Action> getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in this
     * Executable.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * Get the EnterableState parent.
     *
     * @return Returns the parent.
     */
    public EnterableState getParent() {
        return parent;
    }

    /**
     * Set the EnterableState parent.
     *
     * @param parent The parent to set.
     */
    protected void setParent(final EnterableState parent) {
        this.parent = parent;
    }

    /**
     * <p>
     * The assigned client identifier for this state.</p>
     */
    private String clientId = null;

    /**
     * Create the identifier for this transition target.
     *
     * @param element to generate
     * @return Returns the new unique client id.
     */
    @Override
    public String createUniqueId(Object element) {
        String result = null;
        if (element instanceof Action) {
            Action action = (Action) element;
            if (!getActions().contains(action)) {
                throw new IllegalArgumentException("This executable "
                        + "element no constain "
                        + "child element: " + action.getClass().getName());

            }
            result = "action_" + getActions().indexOf(action);
        }

        return result;
    }

    /**
     * Get the identifier for this ecutable.
     *
     * @return Returns the unique client id.
     */
    public String getClientId() {
        if (this.clientId == null) {
            String parentId = null;

            if (this.parent != null) {
                parentId = this.parent.getClientId();
            }

            String id = parent.createUniqueId(this);
            this.clientId = id;
            if (parentId != null) {
                StringBuilder idBuilder
                        = new StringBuilder(parentId.length()
                                + 1 + this.clientId.length());

                this.clientId = idBuilder
                        .append(parentId)
                        .append(":")
                        .append(id).toString();
            }
        }
        return clientId;
    }

}
