/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state;

import org.apache.faces.state.log.FlowErrorReporterImpl;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowStatus;
import javax.faces.state.StateFlowExecutor;
import org.apache.faces.state.el.FlowEvaluatorImpl;
import org.apache.faces.state.semantics.StateChartSemanticsImpl;
import javax.faces.state.semantics.StateChartSemantics;

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
        this.flowInstance = new FlowInstance(this);
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
