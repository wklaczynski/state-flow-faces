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
package org.apache.common.scxml.env;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLLogger;
import org.apache.common.scxml.SCXMLSystemContext;
import org.apache.common.scxml.io.StateHolder;
import static org.apache.common.scxml.io.StateHolderSaver.restoreAttachedState;
import static org.apache.common.scxml.io.StateHolderSaver.saveAttachedState;

/**
 * Simple Context wrapping a map of variables.
 *
 */
public class AbstractContext implements Context, StateHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Implementation independent log category.
     */
    protected static final Logger log = SCXMLLogger.SCXML.getLogger();
    /**
     * The parent Context to this Context.
     */
    private final Context parent;
    /**
     * The Map of variables and their values in this Context.
     */
    private Map<String, Object> vars;

    /**
     *
     */
    protected SCXMLSystemContext systemContext;

    /**
     * Constructor.
     *
     */
    public AbstractContext() {
        this(null, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public AbstractContext(final Context parent) {
        this(parent, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public AbstractContext(final Context parent, final Map<String, Object> initialVars) {
        this.parent = parent;
        this.systemContext = parent instanceof SCXMLSystemContext
                ? (SCXMLSystemContext) parent : parent != null ? parent.getSystemContext() : null;
        if (initialVars == null) {
            setVars(new HashMap<>());
        } else {
            setVars(this.vars = initialVars);
        }
    }

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method searches the chain of parent Contexts for variable existence.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml2.Context#set(String, Object)
     */
    @Override
    public void set(final String name, final Object value) {
        if (getVars().containsKey(name)) { //first try to override local
            setLocal(name, value);
        } else if (parent != null && parent.has(name)) { //then check for global
            parent.set(name, value);
        } else { //otherwise create a new local variable
            setLocal(name, value);
        }
    }

    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The variable name
     * @return Object The variable value
     * @see org.apache.commons.scxml2.Context#get(java.lang.String)
     */
    @Override
    public Object get(final String name) {
        Object localValue = getVars().get(name);
        if (localValue != null) {
            return localValue;
        } else if (parent != null) {
            return parent.get(name);
        } else {
            return null;
        }
    }

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     * @see org.apache.commons.scxml2.Context#has(java.lang.String)
     */
    @Override
    public boolean has(final String name) {
        return (hasLocal(name) || (parent != null && parent.has(name)));
    }

    /**
     * Check if this variable exists, only checking this Context
     *
     * @param name The variable name
     * @return boolean true if this variable exists
     * @see org.apache.commons.scxml2.Context#hasLocal(java.lang.String)
     */
    @Override
    public boolean hasLocal(final String name) {
        return (getVars().containsKey(name));
    }

    /**
     * Clear this Context.
     *
     * @see org.apache.commons.scxml2.Context#reset()
     */
    @Override
    public void reset() {
        getVars().clear();
    }

    /**
     * Get the parent Context, may be null.
     *
     * @return Context The parent Context
     * @see org.apache.commons.scxml2.Context#getParent()
     */
    @Override
    public Context getParent() {
        return parent;
    }

    /**
     * Get the SCXMLSystemContext for this Context, should not be null unless
     * this is the root Context
     *
     * @return The SCXMLSystemContext in a chained Context environment
     */
    @Override
    public SCXMLSystemContext getSystemContext() {
        return systemContext;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method allows to shaddow a variable of the same name up the Context
     * chain.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml2.Context#setLocal(String, Object)
     */
    @Override
    public void setLocal(final String name, final Object value) {
        getVars().put(name, value);
    }

    /**
     * Set the variables map.
     *
     * @param vars The new Map of variables.
     */
    protected void setVars(final Map<String, Object> vars) {
        this.vars = vars;
    }

    /**
     * Get the Map of all local variables in this Context.
     *
     * @return Returns the vars.
     */
    @Override
    public Map<String, Object> getVars() {
        return vars;
    }

    /**
     *
     * @param context
     * @return
     */
    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[1];
        values[0] = saveVarsState(context);

        return values;
    }

    /**
     *
     * @param context
     * @param state
     */
    @Override
    public void restoreState(Context context, Object state) {
        if (state == null) {
            return;
        }
        Object[] values = (Object[]) state;

        restoreVarsState(context, values[0]);
    }

    /**
     *
     * @param context
     * @return
     */
    protected Object saveVarsState(Context context) {
        Object state = null;
        if (null != vars && vars.size() > 0) {
            Object[] attached = new Object[vars.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                Object vstate = saveAttachedState(context, entry.getValue());
                attached[i++] = new Object[]{entry.getKey(), vstate};
            }
            state = attached;
        }
        return state;
    }

    /**
     *
     * @param context
     * @param state
     */
    protected void restoreVarsState(Context context, Object state) {
        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;
                String key = (String) entry[0];

                Object vobj = restoreAttachedState(context, entry[1]);
                vars.put(key, vobj);
            }
        }
    }

}
