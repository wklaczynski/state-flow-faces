/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import javax.faces.state.model.StateChart;
import static javax.faces.state.model.StateChart.STATE_MACHINE_HINT;
import javax.faces.state.model.TransitionTarget;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowContextImpl implements FlowContext, Serializable {

    public static final Logger log = FlowLogger.FLOW.getLogger();
    private TransitionTarget target;
    /**
     * The parent Context to this Context.
     */
    private FlowContext parent;
    /**
     * The Map of variables and their values in this Context.
     */
    private Map<String, Object> vars;

    /**
     * Constructor.
     *
     * @param target
     */
    public FlowContextImpl(TransitionTarget target) {
        this(target, null, null);
    }

    /**
     * Constructor.
     *
     * @param target
     * @param parent A parent Context, can be null
     */
    public FlowContextImpl(TransitionTarget target, final FlowContext parent) {
        this(target, parent, null);
    }

    /**
     * Constructor.
     *
     * @param target
     * @param initialVars A pre-populated initial variables map
     */
    public FlowContextImpl(TransitionTarget target, final Map initialVars) {
        this(target, null, initialVars);
    }

    /**
     * Constructor.
     *
     * @param target
     * @param parent A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    public FlowContextImpl(TransitionTarget target, final FlowContext parent, final Map initialVars) {
        this.target = target;
        this.parent = parent;
        if (initialVars == null) {
            this.vars = new HashMap();
        } else {
            this.vars = initialVars;
        }
    }

    /**
     * Get target opwner reference this Context.
     *
     * @return TransitionTarget
     */
    @Override
    public TransitionTarget getTarget() {
        return target;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method searches the chain of parent Contexts for variable existence.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml.Context#set(String, Object)
     */
    @Override
    public void set(final String name, final Object value) {
        if (vars.containsKey(name)) { //first try to override local
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
     * @see org.apache.commons.scxml.Context#get(java.lang.String)
     */
    @Override
    public Object get(final String name) {
        if (vars.containsKey(name)) {
            return vars.get(name);
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
     * @see org.apache.commons.scxml.Context#has(java.lang.String)
     */
    @Override
    public boolean has(final String name) {
        if (vars.containsKey(name)) {
            return true;
        } else if (parent != null && parent.has(name)) {
            return true;
        }
        return false;
    }

    /**
     * Clear this Context.
     *
     * @see org.apache.commons.scxml.Context#reset()
     */
    @Override
    public void reset() {
        vars.clear();
    }

    /**
     * Get the parent Context, may be null.
     *
     * @return Context The parent Context
     * @see org.apache.commons.scxml.Context#getParent()
     */
    @Override
    public FlowContext getParent() {
        return parent;
    }

    /**
     * Assigns a new value to an existing variable or creates a new one. The
     * method allows to shaddow a variable of the same name up the Context
     * chain.
     *
     * @param name The variable name
     * @param value The variable value
     * @see org.apache.commons.scxml.Context#setLocal(String, Object)
     */
    @Override
    public void setLocal(final String name, final Object value) {
        vars.put(name, value);
        if (log.isLoggable(Level.FINE) && !name.equals("_ALL_STATES")) {
            log.log(Level.FINE, "{0} = {1}", new Object[]{name, String.valueOf(value)});
        }
    }

    /**
     * Set the variables map.
     *
     * @param vars The new Map of variables.
     */
    protected void setVars(final Map vars) {
        this.vars = vars;
    }

    /**
     * Get the Map of all local variables in this Context.
     *
     * @return Returns the vars.
     */
    @Override
    public Map getVars() {
        return vars;
    }

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[2];
        if (target != null) {
            values[0] = target.getClientId();
        }
        values[1] = saveVarsState(context);

        return values;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }
        StateChart chart = (StateChart) context.getAttributes().get(STATE_MACHINE_HINT);

        Object[] values = (Object[]) state;

        if (values[0] != null) {
            String ttid = (String) values[0];
            Object found = chart.findElement(ttid);
            if (found == null) {
                throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
            }
            
            target = (TransitionTarget) found;
        }

        restoreVarsState(context, values[1]);
    }

    private Object saveVarsState(FacesContext context) {
        Object state = null;
        if (null != vars && vars.size() > 0) {
            Object[] attached = new Object[vars.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                attached[i++] = new Object[]{entry.getKey(), entry.getValue()};
            }
            state = attached;
        }
        return state;
    }

    private void restoreVarsState(FacesContext context, Object state) {
        vars.clear();
        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;
                vars.put((String) entry[0], entry[1]);
            }
        }
    }

}
