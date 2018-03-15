/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.invoke;

import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class AbstractInvoker implements Invoker {

    protected String type;
    protected String parentStateId;
    protected FlowInstance instance;

    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public String getParentStateId() {
        return parentStateId;
    }

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
    }

    public FlowInstance getInstance() {
        return instance;
    }

    @Override
    public void setInstance(FlowInstance instance) {
        this.instance = instance;
    }

    @Override
    public void parentEvents(FlowTriggerEvent[] evts) throws InvokerException {

    }

    @Override
    public void cancel() throws InvokerException {

    }

    @Override
    public Object saveState(FacesContext context) {
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {

    }

}
