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
package javax.faces.state;

import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FlowContext {
    
    /**
     * Get target opwner reference this Context.
     * @return TransitionTarget
     */
    TransitionTarget getTarget();    
    
    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method searches the chain of parent Contexts for variable
     * existence.
     *
     * @param name The variable name
     * @param value The variable value
     */
    void set(String name, Object value);

    /**
     * Assigns a new value to an existing variable or creates a new one.
     * The method allows to shaddow a variable of the same name up the
     * Context chain.
     *
     * @param name The variable name
     * @param value The variable value
     */
    void setLocal(String name, Object value);

    /**
     * Get the value of this variable; delegating to parent.
     *
     * @param name The name of the variable
     * @return The value (or null)
     */
    Object get(String name);

    /**
     * Check if this variable exists, delegating to parent.
     *
     * @param name The name of the variable
     * @return Whether a variable with the name exists in this Context
     */
    boolean has(String name);

    /**
     * Get the Map of all variables in this Context.
     *
     * @return Local variable entries Map
     * To get variables in parent Context, call getParent().getVars().
     * @see #getParent()
     */
    Map getVars();

    /**
     * Clear this Context.
     */
    void reset();

    /**
     * Get the parent Context, may be null.
     *
     * @return The parent Context in a chained Context environment
     */
    FlowContext getParent();

    /**
     * Save state this Context.
     * @param context The FacesContext
     * @return The saved state
     */
    public Object saveState(FacesContext context);
    
    
    /**
     * Save state this Context.
     * @param context The FacesContext
     * @param state State object
     */
    public void restoreState(FacesContext context, Object state);
    
    
}
