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
package javax.faces.state;

import javax.faces.event.PhaseId;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlow {

    /**
     *
     */
    public static interface Name {

        /**
         *
         * @param path
         * @return
         */
        String get(String path);
    }

    private static class NameResolver implements Name {

        private final String prefix;
        private final String sufix;

        public NameResolver(String prefix, String sufix) {
            this.prefix = prefix;
            this.sufix = sufix;
        }

        @Override
        public String get(String path) {
            return (prefix != null ? prefix : "") + path + (sufix != null ? sufix : "");
        }

    }

    /**
     *
     */
    public static final String VIEWROOT_CONTROLLER_TYPE = "VIEWROOT";

    /**
     *
     */
    public static final String EXECUTOR_CONTROLLER_TYPE = "EXECUTOR";

    /**
     *
     */
    public static final String STATE_CHART_FACET_NAME = "javax_stateflow_metadata";

    /**
     *
     */
    public static final String STATE_CHART_MACHINE_HINT = "javax.faces.flow.STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String CUSTOM_ACTIONS_HINT = "javax.faces.flow.CUSTOM_ACTIONS_HINT";

    /**
     *
     */
    public static final String CUSTOM_INVOKERS_HINT = "javax.faces.flow.CUSTOM_INVOKERS_HINT";

    /**
     *
     */
    public static final String CURRENT_EXECUTOR_HINT = "javax.faces.flow.CURRENT_EXECUTOR_HINT";

    /**
     *
     */
    public static final String CURRENT_COMPONENT_HINT = "javax.faces.flow.CURRENT_COMPONENT_HINT";

    /**
     *
     */
    public static final String CONTROLLER_SET_HINT = "javax.faces.flow.CONTROLLER_SET_HINT";

    /**
     *
     */
    public static final String DEFINITION_SET_HINT = "javax.faces.flow.DEFINITION_SET_HINT";

    /**
     *
     */
    public static final String EXECUTOR_CONTROLLER_KEY = "javax.faces.component.EXECUTOR_CONTROLLER_KEY";

    /**
     *
     */
    public static final String EXECUTOR_CONTROLLER_LOCATION_KEY = "javax.faces.component.EXECUTOR_CONTROLLER_LOCATION_KEY";

    /**
     *
     */
    public static final String DISABLE_EXPRESSION_MAP = "javax.faces.component.DISABLE_EXPRESSION_MAP";

    
    /**
     *
     */
    public static final Name RENDER_EXECUTOR_FACET = new NameResolver(
            "state.flow.faces:", ":RENDER_EXECUTOR_FACET");

    /**
     *
     */
    public static final Name EXECUTOR_CONTEXT_PATH = new NameResolver(
            "state.flow.faces:", ":RENDER_CONTEXT_PATH");
    

    /**
     *
     */
    public static final Name EXECUTE_CONTEXT_STATE = new NameResolver(
            "state.flow.faces:", ":ExecuteContextState");

    /**
     *
     */
    public static final String EXECUTOR_CONTEXT_VIEW_PATH = EXECUTOR_CONTEXT_PATH.get("ViewRoot");
    
    /**
     *
     */
    public static final String DEFAULT_STATE_MACHINE_NAME = "main";

    /**
     *
     */
    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String VIEW_RESTORED_HINT = "javax.faces.flow.VIEW_RESTORED_HINT";
    
    /**
     *
     */
    public static final String BUILD_STATE_MACHINE_HINT = "javax.faces.flow.BUILD_STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String BUILD_STATE_CONTINER_HINT = "javax.faces.flow.BUILD_STATE_CONTINER_HINT";

    /**
     *
     */
    public static final String EXECUTE_STATE_MACHINE_HINT = "javax.faces.flow.EXECUTE_STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String FACES_CHART_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";

    /**
     *
     */
    public static final String FACES_CHART_CONTROLLER_TYPE = "com.sun.faces.FACES_CHART_CONTROLLER";

    /**
     *
     */
    public static final String FACES_CHART_CONTINER_NAME = "com.sun.faces.FACES_CHART_CONTINER";

    /**
     *
     */
    public static final String FACES_CHART_CONTINER_SOURCE = "com.sun.faces.FACES_CHART_CONTINER_SOURCE";

    /**
     *
     */
    public static final String FACES_CHART_EXECUTOR_VIEW_ID = "com.sun.faces.FACES_CHART_EXECUTOR_VIEW_ID";

    /**
     *
     */
    public static final String FACES_VIEW_ROOT_EXECUTOR_ID = "com.sun.faces.FACES_VIEW_ROOT_EXECUTOR_ID";

    /**
     *
     */
    public static final String VIEW_EVENT_PREFIX = "faces.view.";

    /**
     *
     */
    public static final String OUTCOME_EVENT_PREFIX = "faces.view.action.";

    /**
     *
     */
    public static final String SYSTEM_EVENT_PREFIX = "faces.system.event.";

    /**
     *
     */
    public static final String PORTLET_EVENT_PREFIX = "faces.view.portlet.";

    /**
     *
     */
    public static final String PHASE_EVENT_PREFIX = "faces.phase.";

    /**
     *
     */
    public static final String DECODE_DISPATCHER_EVENTS = "faces.dipatrcher.events.decode";

    /**
     *
     */
    public static final String ENCODE_DISPATCHER_EVENTS = "faces.dipatcher.events.encode";

    /**
     *
     */
    public static final String BEFORE_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "before.";

    /**
     *
     */
    public static final String FACES_VIEW_STATE = "com.sun.faces.FACES_VIEW_STATE";
    
    /**
     *
     */
    public static final String AFTER_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "after.";

    /**
     *
     */
    public static final String BEFORE_BUILD_VIEW = BEFORE_PHASE_EVENT_PREFIX
            + "BUILD_VIEW";

    /**
     *
     */
    public static final String BEFORE_HANDLE_RESOURCE = BEFORE_PHASE_EVENT_PREFIX
            + "HANDLE_RESOURCE";
    
    /**
     *
     */
    public static final String AFTER_BUILD_VIEW = AFTER_PHASE_EVENT_PREFIX
            + "BUILD_VIEW";
    
    /**
     *
     */
    public static final String BEFORE_RESTORE_VIEW = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.RESTORE_VIEW.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_RESTORE_VIEW = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.RESTORE_VIEW.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_RENDER_VIEW = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_RENDER_VIEW = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_INVOKE_APPLICATION = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_INVOKE_APPLICATION = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_APPLY_REQUEST_VALUES = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_APPLY_REQUEST_VALUES = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_PROCESS_VALIDATIONS = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_PROCESS_VALIDATIONS = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();


    /**
     *
     */
    public static final String BEFORE_CHANGE_VIEW_EXECUTOR = BEFORE_PHASE_EVENT_PREFIX
            + "CHANGE_VIEW_EXECUTOR";
    
    
    /**
     *
     */
    public static final String AFTER_CHANGE_VIEW_EXECUTOR = AFTER_PHASE_EVENT_PREFIX
            + "CHANGE_VIEW_EXECUTOR";
    
    
}
