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
package org.ssoft.faces.impl.state.cdi;

import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.model.EnterableState;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIHelper {

    /**
     *
     */
    public static void flowEntered() {
        FlowCDIContext.flowEntered();
    }

    /**
     *
     */
    public static void flowExited() {
        FlowCDIContext.flowExited();
    }

    /**
     *
     * @param executor
     */
    public static void executorEntered(SCXMLExecutor executor) {
        DialogCDIContext.executorEntered(executor);
        ChartCDIContext.executorEntered(executor);
        StateCDIContext.executorEntered(executor);
    }

    /**
     *
     * @param executor
     */
    public static void executorExited(SCXMLExecutor executor) {
        DialogCDIContext.executorExited(executor);
        ChartCDIContext.executorExited(executor);
        StateCDIContext.executorExited(executor);
    }

    /**
     *
     * @param executor
     * @param state
     */
    public static void stateEntered(SCXMLExecutor executor, EnterableState state) {
        //StateTargetCDIContext.stateEntered(executor, state);
    }

    /**
     *
     * @param executor
     * @param state
     */
    public static void stateExited(SCXMLExecutor executor, EnterableState state) {
        //StateTargetCDIContext.stateExited(executor, state);
    }
    
}
