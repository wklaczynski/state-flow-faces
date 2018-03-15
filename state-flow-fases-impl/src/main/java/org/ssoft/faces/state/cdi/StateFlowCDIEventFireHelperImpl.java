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
import javax.faces.state.annotation.StateChartScoped;

public class StateFlowCDIEventFireHelperImpl implements StateFlowCDIEventFireHelper {

    @Inject
    @Initialized(StateChartScoped.class)
    Event<StateFlowExecutor> stateChartScopeInitializedEvent;
    @Inject
    @Destroyed(StateChartScoped.class)
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
