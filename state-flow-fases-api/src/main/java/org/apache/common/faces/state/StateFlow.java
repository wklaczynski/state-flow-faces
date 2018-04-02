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
package org.apache.common.faces.state;

import javax.faces.event.PhaseId;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlow {
    
    public static final String STATECHART_FACET_NAME = "javax_stateflow_metadata";

    public static final String STATE_MACHINE_HINT = "javax.faces.flow.STATE_MACHINE_HINT";

    public static final String CUSTOM_ACTIONS_HINT = "javax.faces.flow.CUSTOM_ACTIONS_HINT";

    public static final String CUSTOM_INVOKERS_HINT = "javax.faces.flow.CUSTOM_INVOKERS_HINT";

    public static final String CURRENT_EXECUTOR_HINT = "javax.faces.flow.CURRENT_EXECUTOR_HINT";

    public static final String CURRENT_COMPONENT_HINT = "javax.faces.flow.CURRENT_COMPONENT_HINT";
    
    public static final String STATEFLOW_COMPONENT_NAME = "javax_faces_stateflow";

    public static final String DEFAULT_STATECHART_NAME = "main";

    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";

    public static final String BUILD_STATE_MACHINE_HINT = "javax.faces.flow.BUILD_STATE_MACHINE_HINT";

    public static final String OUTCOME_EVENT_PREFIX = "faces.view.action.";
    
    public static final String PHASE_EVENT_PREFIX = "faces.phase.";

    public static final String DECODE_DISPATCHER_EVENTS = "faces.dipatrcher.events.decode";

    public static final String ENCODE_DISPATCHER_EVENTS = "faces.dipatrcher.events.encode";
    
    public static final String BEFORE_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "before.";

    public static final String AFTER_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "after.";

    public static final String BEFORE_RESTORE_VIEW = BEFORE_PHASE_EVENT_PREFIX + 
            PhaseId.RESTORE_VIEW.getName().toLowerCase();

    public static final String AFTER_RESTORE_VIEW = AFTER_PHASE_EVENT_PREFIX + 
            PhaseId.RESTORE_VIEW.getName().toLowerCase();
    
    public static final String BEFORE_RENDER_VIEW = BEFORE_PHASE_EVENT_PREFIX + 
            PhaseId.RENDER_RESPONSE.getName().toLowerCase();
    
    public static final String AFTER_RENDER_VIEW = AFTER_PHASE_EVENT_PREFIX + 
            PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    public static final String BEFORE_INVOKE_APPLICATION = BEFORE_PHASE_EVENT_PREFIX + 
            PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    public static final String AFTER_INVOKE_APPLICATION = AFTER_PHASE_EVENT_PREFIX + 
            PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    public static final String BEFORE_APPLY_REQUEST_VALUES = BEFORE_PHASE_EVENT_PREFIX + 
            PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    public static final String AFTER_APPLY_REQUEST_VALUES = AFTER_PHASE_EVENT_PREFIX + 
            PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    public static final String BEFORE_PROCESS_VALIDATIONS = BEFORE_PHASE_EVENT_PREFIX + 
            PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();

    public static final String AFTER_PROCESS_VALIDATIONS = AFTER_PHASE_EVENT_PREFIX + 
            PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();
    
}
