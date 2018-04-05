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
package org.apache.common.faces.impl.state.cdi;

import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import org.apache.common.faces.state.annotation.StateChartScoped;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.faces.state.annotation.StateDialogScoped;
import org.apache.common.faces.state.annotation.StateTargetScoped;
import org.apache.common.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIEventFireHelperImpl implements StateFlowCDIEventFireHelper {

    @Inject
    @Initialized(StateChartScoped.class)
    Event<SCXMLExecutor> stateChartScopeInitializedEvent;
    @Inject
    @Destroyed(StateChartScoped.class)
    Event<SCXMLExecutor> stateChartScopeDestroyedEvent;

    @Inject
    @Initialized(StateDialogScoped.class)
    Event<SCXMLExecutor> stateDialogScopeInitializedEvent;
    @Inject
    @Destroyed(StateDialogScoped.class)
    Event<SCXMLExecutor> stateDialogScopeDestroyedEvent;

    @Inject
    @Initialized(StateTargetScoped.class)
    Event<TransitionTarget> stateTargetScopeInitializedEvent;
    @Inject
    @Destroyed(StateTargetScoped.class)
    Event<TransitionTarget> stateTargetScopeDestroyedEvent;

    @Inject
    @Initialized(StateTargetScoped.class)
    Event<SCXMLExecutor> stateTargetScopeExecutorInitializedEvent;
    @Inject
    @Destroyed(StateTargetScoped.class)
    Event<SCXMLExecutor> stateTargetScopeExecutorDestroyedEvent;
    
    /**
     *
     * @param executor
     */
    @Override
    public void fireExecutorInitializedEvent(SCXMLExecutor executor) {
        stateChartScopeInitializedEvent.fire(executor);
    }

    /**
     *
     * @param executor
     */
    @Override
    public void fireExecutorDestroyedEvent(SCXMLExecutor executor) {
        stateChartScopeDestroyedEvent.fire(executor);
    }
    
    /**
     *
     * @param executor
     */
    @Override
    public void fireRootExecutorInitializedEvent(SCXMLExecutor executor) {
        stateDialogScopeInitializedEvent.fire(executor);
    }

    /**
     *
     * @param executor
     */
    @Override
    public void fireRootExecutorDestroyedEvent(SCXMLExecutor executor) {
        stateDialogScopeDestroyedEvent.fire(executor);
    }

    /**
     *
     * @param target
     */
    @Override
    public void fireTargetInitializedEvent(TransitionTarget target) {
        stateTargetScopeInitializedEvent.fire(target);
    }

    /**
     *
     * @param target
     */
    @Override
    public void fireTargetDestroyedEvent(TransitionTarget target) {
        stateTargetScopeDestroyedEvent.fire(target);
    }

    /**
     *
     * @param executor
     */
    @Override
    public void fireTargetExecutorInitializedEvent(SCXMLExecutor executor) {
        stateTargetScopeExecutorInitializedEvent.fire(executor);
    }

    /**
     *
     * @param executor
     */
    @Override
    public void fireTargetExecutorDestroyedEvent(SCXMLExecutor executor) {
        stateTargetScopeExecutorDestroyedEvent.fire(executor);
    }

}
