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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.context.FacesContext;
import org.apache.common.scxml.env.SimpleContext;

/**
 * A Context or &quot;scope&quot; for storing variables; usually tied to a SCXML
 * root or State object.
 */
public interface Context {

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method searches the chain of parent Contexts for variable existence.
     *
     * @param name The variable name
     * @param value The variable value
     */
    void set(String name, Object value);

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method allows to shaddow a variable of the same name up the Context
     * chain.
     *
     * @param name The variable name
     * @param value The variable value
     */
    void setLocal(String name, Object value);

    
    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method allows to shaddow a variable of the same name up the Context
     * chain.
     *
     * @param name The variable name
     */
    void removeLocal(String name);
    
    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The name of the variable
     * @return The value (or null)
     */
    Object get(String name);

    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The name of the variable
     */
    void remove(String name);

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The name of the variable
     * @return Whether a variable with the name exists in this Context
     */
    boolean has(String name);

    /**
     * Check if this variable exists, only checking this Context
     *
     * @param name The name of the variable
     * @return Whether a variable with the name exists in this Context
     */
    boolean hasLocal(String name);

    /**
     * Get the Map of all variables in this Context.
     *
     * @return Local variable entries Map To get variables in parent Context,
     * call getParent().getVars().
     * @see #getParent()
     */
    Map<String, Object> getVars();

    /**
     * Clear this Context.
     */
    void reset();

    /**
     * Get the parent Context, may be null.
     *
     * @return The parent Context in a chained Context environment
     */
    Context getParent();

    /**
     * Get the SCXMLSystemContext for this Context, should not be null unless
     * this is the root Context
     *
     * @return The SCXMLSystemContext in a chained Context environment
     */
    SCXMLSystemContext getSystemContext();

    /**
     *
     */
    static ConcurrentHashMap threadInitContext = new ConcurrentHashMap(2);

    /**
     * <p>
     * The <code>ThreadLocal</code> variable used to record the {@link Context}
     * instance for each processing thread.</p>
     */
    static ThreadLocal<Context> instance = new ThreadLocal<Context>() {
        @Override
        protected Context initialValue() {
            return (null);
        }
    };

    /**
     * Return the Context instance
     *
     * @return current Context
     */
    public static Context getCurrentInstance() {
        Context context = instance.get();

        if (null == context) {
            context = (Context) threadInitContext.get(Thread.currentThread());
        }
        if (null == context) {
            context = new SimpleContext();
        }
        return context;
    }

    /**
     * <p>
     * Set the {@link FacesContext} instance for the request that is being
     * processed by the current thread.</p>
     *
     * @param context The {@link Context} instance for the current thread,
     * or <code>null</code> if this thread no longer has a
     * <code>Context</code> instance.
     *
     */
    public static void setCurrentInstance(Context context) {

        if (context == null) {
            instance.remove();
        } else {
            instance.set(context);
        }

    }

}
