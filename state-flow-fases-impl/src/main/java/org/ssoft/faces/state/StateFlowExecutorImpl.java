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
package org.ssoft.faces.state;

import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowStatus;
import javax.faces.state.StateFlowExecutor;
import org.ssoft.faces.state.el.FlowEvaluatorImpl;
import org.ssoft.faces.state.semantics.StateChartSemanticsImpl;
import javax.faces.state.semantics.StateChartSemantics;
import org.ssoft.faces.state.log.FlowErrorReporterImpl;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowExecutorImpl extends StateFlowExecutor {

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp The event dispatcher
     * @param errRep The error reporter
     */
    public StateFlowExecutorImpl(final FlowEvaluator expEvaluator,
            final FlowEventDispatcher evtDisp, final FlowErrorReporter errRep) {
        this(expEvaluator, evtDisp, errRep, null);
    }

    /**
     * Convenience constructor.
     */
    public StateFlowExecutorImpl() {
        this(null, null, null, null);
    }

    /**
     * Constructor.
     *
     * @param expEvaluator The expression evaluator
     * @param evtDisp The event dispatcher
     * @param errRep The error reporter
     * @param semantics The SCXML semantics
     */
    public StateFlowExecutorImpl(final FlowEvaluator expEvaluator,
            final FlowEventDispatcher evtDisp, final FlowErrorReporter errRep,
            final StateChartSemantics semantics) {
        this.eventdispatcher = evtDisp;
        this.errorReporter = errRep;
        this.currentStatus = new FlowStatus();
        this.stateMachine = null;
        this.flowInstance = new FlowInstanceImpl(this);
        this.semantics = semantics;
        init(expEvaluator);
    }

    private void init(FlowEvaluator expEvaluator) {
        if (semantics == null) {
            this.semantics = new StateChartSemanticsImpl();
        }
        if (eventdispatcher == null) {
            this.eventdispatcher = new FlowEventDispatcherImpl();
        }
        if (errorReporter == null) {
            this.errorReporter = new FlowErrorReporterImpl();
        }

        if (expEvaluator == null) {
            expEvaluator = new FlowEvaluatorImpl(this);
        }

        this.flowInstance.setEvaluator(expEvaluator);
    }
    
}
