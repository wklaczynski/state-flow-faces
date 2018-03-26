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
package org.apache.faces.state;

import javax.faces.event.PhaseId;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlow {
    
    public static final String STATECHART_FACET_NAME = "javax_stateflow_metadata";

    public static final String STATE_MACHINE_HINT = "javax.faces.flow.STATE_MACHINE_HINT";

    public static final String CUSTOM_ACTIONS_HINT = "javax.faces.flow.CUSTOM_ACTIONS_HINT";

    public static final String CURRENT_EXECUTOR_HINT = "javax.faces.flow.CURRENT_EXECUTOR_HINT";
    
    public static final String STATEFLOW_COMPONENT_NAME = "javax_faces_stateflow";

    public static final String DEFAULT_STATECHART_NAME = "main";

    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";

    public static final String BUILD_STATE_MACHINE_HINT = "javax.faces.flow.BUILD_STATE_MACHINE_HINT";

    public static final String OUTCOME_EVENT_PREFIX = "faces.view.action.";
    
    public static final String FACES_PHASE_EVENT_PREFIX = "faces.phase.";

    public static final String FACES_RESTORE_VIEW = FACES_PHASE_EVENT_PREFIX + "restore";

    public static final String FACES_RENDER_VIEW = FACES_PHASE_EVENT_PREFIX + 
            PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    public static final String FACES_INVOKE_APPLICATION = FACES_PHASE_EVENT_PREFIX + 
            PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    public static final String FACES_APPLY_REQUEST_VALUES = FACES_PHASE_EVENT_PREFIX + 
            PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    public static final String FACES_PROCESS_VALIDATIONS = FACES_PHASE_EVENT_PREFIX + 
            PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();
    
}
