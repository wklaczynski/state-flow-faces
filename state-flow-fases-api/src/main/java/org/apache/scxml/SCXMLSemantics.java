/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.scxml;

import java.util.Map;
import java.util.Set;
import org.apache.scxml.model.EnterableState;
import org.apache.scxml.model.ModelException;
import org.apache.scxml.model.SCXML;

/**
 * <p>The purpose of this interface is to separate the the
 * <a href="https://www.w3.org/TR/2015/REC-scxml-20150901/#AlgorithmforSCXMLInterpretation">
 *     W3C SCXML Algorithm for SCXML Interpretation</a>
 * from the <code>SCXMLExecutor</code> and therefore make it pluggable.</p>
 * <p>
 * From an SCXML execution POV, there are only four entry points needed into the Algorithm, namely:
 * </p>
 * <ul>
 *  <li>1: {@link #initialize(SCXMLExecutionContext, Map)} - Initialization of the state machine, optionally with external
 *  (initial) data for the root (global) data elements in the root &lt;datamodel&gt;
 *  </li>
 *  <li>2: {@link #firstStep(SCXMLExecutionContext)} - Performing and completing a first macro step, The state machine
 *  thereafter should be ready for processing external events (or be terminated already)</li>
 *  <li>3: {@link #nextStep(SCXMLExecutionContext, TriggerEvent)} - Processing a single external event and completing the
 *  macro step for it, after which the state machine should be ready for processing another external event (if any),
 *  or be terminated already.</li>
 *  <li>4: {@link #finalStep(SCXMLExecutionContext)} - If the state machine terminated
 *  ({@link SCXMLExecutionContext#isRunning()} == false), after either of the above steps, finalize the state machine
 *  by performing the final step.</li>
 * </ul>
 * <p>After a state machine has been terminated you can re-initialize the execution context, and start again.</p>
 * <p>
 * The first two methods represent the <b>interpret</b> entry point specified in the Algorithm for SCXML Interpretation.
 * The third and fourth method represent the <b>mainEventLoop</b> and <b>exitInterpreter</b> entry points.
 * These have been more practically and logically broken into four different methods so that the blocking wait for
 * external events can be handled externally.</p>
 * <p>
 *  It is up to the specific SCXMLSemantics implementation to provide the concrete handling for these methods according
 *  to the <a href="https://www.w3.org/TR/2015/REC-scxml-20150901/#AlgorithmforSCXMLInterpretation">Algorithm for SCXML
 *  Interpretation</a> in the SCXML specification (or possibly something else/non-conforming implementation).</p>
 * <p>
 * The default {@link org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl} provides an implementation of the
 * specification, and can easily be overridden/customized as a whole or only on specific parts of the Algorithm
 * implementation.
 * </p>
 * <p>
 * Note that both the {@link #firstStep(SCXMLExecutionContext)} and {@link #nextStep(SCXMLExecutionContext, TriggerEvent)}
 * first run to completion for any internal events raised before returning, as expected and required by the SCXML
 * specification, so it is currently not possible to 'manage' internal event processing externally.
 * </p>
 *
 * <p>Specific semantics can be created by subclassing
 * <code>org.apache.commons.scxml2.semantics.SCXMLSemanticsImpl</code>.</p>
 */
public interface SCXMLSemantics {

    /**
     * Optional post processing after loading an {@link SCXML} document, invoked by {@link SCXMLExecutor}
     * when setting the {@link SCXMLExecutor#setStateMachine(SCXML)}. May be used for removing pseudo-states etc.
     *
     * @param input  SCXML state machine
     * @param errRep ErrorReporter callback
     * @return normalized SCXML state machine, pseudo states are removed, etc.
     */
    SCXML normalizeStateMachine(final SCXML input, final ErrorReporter errRep);

    /**
     * Initialize the SCXML state machine, optionally initializing (overriding) root &lt;datamodel&gt;&lt;data&gt; elements
     * with external values provided through a data map.
     * @param data A data map to initialize/override &lt;data&gt; elements in the root (global) &lt;datamodel&gt; with
     *             ids matching the keys in the map (other data map entries will be ignored)
     * @param exctx The execution context to initialize
     * @throws ModelException
     */
    void initialize(final SCXMLExecutionContext exctx, final Map<String, Object> data) throws ModelException;

    /**
     * First step in the execution of an SCXML state machine.
     * <p>
     * In the default implementation, this will first (re)initialize the state machine instance, destroying any existing
     * state!
     * </p>
     * <p>
     * The first step is corresponding to the Algorithm for SCXML processing from the interpret() procedure to the
     * mainLoop() procedure up to the blocking wait for an external event.
     * </p>
     * <p>
     * This step should complete the SCXML initial execution and a subsequent macroStep to stabilize the state machine
     * again before returning.
     * </p>
     * <p>
     * If the state machine no longer is running after all this, first the {@link #finalStep(SCXMLExecutionContext)}
     * should be called for cleanup before returning.
     * </p>
     * @param exctx The execution context for this step
     * @throws ModelException if the state machine instance failed to initialize or a SCXML model error occurred during
     * the execution.
     */
    void firstStep(final SCXMLExecutionContext exctx) throws ModelException;

    /**
     * Next step in the execution of an SCXML state machine.
     * <p>
     * The next step is corresponding to the Algorithm for SCXML processing mainEventLoop() procedure after receiving an
     * external event, up to the blocking wait for another external event.
     * </p>
     * <p>
     * If the state machine isn't {@link SCXMLExecutionContext#isRunning()} (any more), this method should do nothing.
     * </p>
     * <p>
     * If the provided event is a {@link TriggerEvent#CANCEL_EVENT}, the state machine should stop running.
     * </p>
     * <p>
     * Otherwise, the event must be set in the {@link SCXMLSystemContext} and processing of the event then should start,
     * and if the event leads to any transitions a microStep for this event should be performed, followed up by a
     * macroStep to stabilize the state machine again before returning.
     * </p>
     * <p>
     * If the state machine no longer is running after all this, first the {@link #finalStep(SCXMLExecutionContext)}
     * should be called for cleanup before returning.
     * </p>
     * @param exctx The execution context for this step
     * @param event The event to process
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    void nextStep(final SCXMLExecutionContext exctx, final TriggerEvent event) throws ModelException;

    /**
     * The final step in the execution of an SCXML state machine.
     * <p>
     * This final step is corresponding to the Algorithm for SCXML processing exitInterpreter() procedure, after the
     * state machine stopped running.
     * </p>
     * <p>
     * If the state machine still is {@link SCXMLExecutionContext#isRunning()} invoking this method should simply
     * do nothing.
     * </p>
     * <p>
     * This final step should first exit all remaining active states and cancel any active invokers, before handling
     * the possible donedata element for the last final state.
     * </p>
     * <p>
     *  <em>NOTE: the current implementation does not yet provide final donedata handling.</em>
     * </p>
     * @param exctx The execution context for this step
     * @throws ModelException if a SCXML model error occurred during the execution.
     */
    void finalStep(final SCXMLExecutionContext exctx) throws ModelException;

    /**
     * Checks whether a given set of states is a legal Harel State Table
     * configuration (with the respect to the definition of the OR and AND
     * states).
     * <p>
     * When {@link SCXMLExecutionContext#isCheckLegalConfiguration()} is true (default) the SCXMLSemantics implementation
     * <em>should</em> invoke this method before executing a step, and throw a ModelException if a non-legal
     * configuration is encountered.
     * </p>
     * <p>
     * This method is also first invoked when manually initializing the status of a state machine through
     * {@link SCXMLExecutor#setConfiguration(java.util.Set)}.
     * </p>
     * @param states a set of states
     * @param errRep ErrorReporter to report detailed error info if needed
     * @return true if a given state configuration is legal, false otherwise
     */
    boolean isLegalConfiguration(final Set<EnterableState> states, final ErrorReporter errRep);
}
