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
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class StateFlowHandler {
    
    public static final String STATEFLOW_COMPONENT_NAME = "javax_faces_stateflow";

    public static final String DEFAULT_STATECHART_NAME = "main";
    
    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";

    public static final String BUILD_STATE_MACHINE_HINT = "javax.faces.flow.BUILD_STATE_MACHINE_HINT";
    
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
    
    public StateChart createStateMachine(FacesContext context, String path) throws ModelException {
        return createStateMachine(context, path, DEFAULT_STATECHART_NAME); 
    }
    
    public abstract StateChart createStateMachine(FacesContext context, String path, String id) throws ModelException;
    
    public abstract void writeState(FacesContext context);
    
}
