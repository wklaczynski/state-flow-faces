/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.cdi;

import javax.faces.state.StateFlowExecutor;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface StateFlowCDIEventFireHelper {

    void fireExecutorInitializedEvent(StateFlowExecutor executor);

    void fireExecutorDestroyedEvent(StateFlowExecutor executor);
    
    void fireStateInitializedEvent(State state);

    void fireStateDestroyedEvent(State state);
    
    void fireParallelInitializedEvent(Parallel parallel);

    void fireParallelDestroyedEvent(Parallel parallel);

}
