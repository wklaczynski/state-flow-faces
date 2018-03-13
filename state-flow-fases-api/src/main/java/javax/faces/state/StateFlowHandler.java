/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class StateFlowHandler {
    
    public static final String STATEFLOW_COMPONENT_NAME = "javax_faces_stateflow";
    
    public static final String KEY = "javax.faces.state.StateFlowHandler";
    
    public final static StateFlowHandler getInstance() {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = (StateFlowHandler) fc.getExternalContext().getApplicationMap().get(KEY);
        return handler;
    }
    
    public StateFlowExecutor getExecutor(FacesContext context) {
        return getExecutor(context, null);
    }
    
    public abstract StateFlowExecutor getExecutor(FacesContext context, StateFlowExecutor parent);
    
    public abstract StateFlowExecutor getRootExecutor(FacesContext context);
    
    public abstract boolean isActive(FacesContext context);
    
    public abstract StateFlowExecutor startExecutor(FacesContext context, StateChart stateMachine, Map params, boolean root);
    
    public void stopExecutor(FacesContext context) {
        stopExecutor(context, null);
    }

    public abstract void stopExecutor(FacesContext context, StateFlowExecutor to);
    
    
    public abstract StateChart createStateMachine(FacesContext context, String path) throws ModelException;
    
}
