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

import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.model.EnterableState;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIHelper {

    public static void executorEntered(SCXMLExecutor executor) {
        if (executor.getParentSCXMLIOProcessor() == null) {
            StateDialogCDIContext.executorEntered(executor);
        }
        StateChartCDIContext.executorEntered(executor);
        StateTargetCDIContext.executorEntered(executor);
    }

    public static void executorExited(SCXMLExecutor executor) {
        if (executor.getParentSCXMLIOProcessor() == null) {
            StateDialogCDIContext.executorExited(executor);
        }
        StateChartCDIContext.executorExited(executor);
        StateTargetCDIContext.executorExited(executor);
    }

    public static void stateEntered(SCXMLExecutor executor, EnterableState state) {
        StateTargetCDIContext.stateEntered(executor, state);
    }

    public static void stateExited(SCXMLExecutor executor, EnterableState state) {
        StateTargetCDIContext.stateExited(executor, state);
    }
    
    
}
