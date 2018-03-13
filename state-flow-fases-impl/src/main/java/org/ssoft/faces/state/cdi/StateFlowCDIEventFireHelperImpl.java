/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.cdi;

import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.faces.state.StateFlowExecutor;
import javax.inject.Inject;
import javax.faces.state.annotation.StateScoped;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.annotation.ParallelScoped;
import javax.faces.state.annotation.DialogScoped;

public class StateFlowCDIEventFireHelperImpl implements StateFlowCDIEventFireHelper {

    @Inject
    @Initialized(DialogScoped.class)
    Event<StateFlowExecutor> stateChartScopeInitializedEvent;
    @Inject
    @Destroyed(DialogScoped.class)
    Event<StateFlowExecutor> stateChartScopeDestroyedEvent;

    @Inject
    @Initialized(StateScoped.class)
    Event<State> stateScopeInitializedEvent;
    
    @Inject
    @Destroyed(StateScoped.class)
    Event<State> stateScopeDestroyedEvent;

    @Inject
    @Initialized(ParallelScoped.class)
    Event<Parallel> parallelScopeInitializedEvent;
    
    @Inject
    @Destroyed(ParallelScoped.class)
    Event<Parallel> parallelScopeDestroyedEvent;

    @Override
    public void fireExecutorInitializedEvent(StateFlowExecutor executor) {
        stateChartScopeInitializedEvent.fire(executor);
    }

    @Override
    public void fireExecutorDestroyedEvent(StateFlowExecutor executor) {
        stateChartScopeDestroyedEvent.fire(executor);
    }

    @Override
    public void fireStateInitializedEvent(State state) {
        stateScopeInitializedEvent.fire(state);
    }

    @Override
    public void fireStateDestroyedEvent(State state) {
        stateScopeDestroyedEvent.fire(state);
    }

    @Override
    public void fireParallelInitializedEvent(Parallel parallel) {
        parallelScopeInitializedEvent.fire(parallel);
    }

    @Override
    public void fireParallelDestroyedEvent(Parallel parallel) {
        parallelScopeDestroyedEvent.fire(parallel);
    }

}
