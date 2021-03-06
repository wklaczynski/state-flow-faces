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
package org.ssoft.faces.impl.state;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowImplConstants {

    /**
     *
     */
    public final static String STATE_FLOW_PREFIX = "javax.faces.flow.";

    /**
     *
     */
    public final static String MAP_SCXML_SUFIX = STATE_FLOW_PREFIX + "MAP_SCXML_SUFIX";

    /**
     *
     */
    public final static String ORGINAL_SCXML_SUFIX = STATE_FLOW_PREFIX + "ORGINAL_SCXML_SUFIX";

    /**
     *
     */
    public final static String STATE_CHART_REQUEST_PARAM_NAME = STATE_FLOW_PREFIX + "STATE_CHART_REQUEST_PARAM_NAME";

    /**
     *
     */
    public final static String STATE_CHART_LOGSTEP_PARAM_NAME = STATE_FLOW_PREFIX + "STATE_CHART_LOGSTEP";

    /**
     *
     */
    public final static String STATE_CHART_SERIALIZED_PARAM_NAME = STATE_FLOW_PREFIX + "STATE_CHART_ALWEYS_SERIALIZED";

    /**
     *
     */
    public final static String STATE_CHART_VIEW_REDIRECT_PARAM_NAME = STATE_FLOW_PREFIX + "DEFAULT_VIEW_REDIRECT";

    /**
     *
     */
    public final static String STATE_CHART_USE_WINDOW_PARAM_NAME = STATE_FLOW_PREFIX + "USE_WINDOW_MODE";

    /**
     *
     */
    public final static String STATE_CHART_AJAX_REDIRECT_PARAM_NAME = STATE_FLOW_PREFIX + "DEFAULT_AJAX_REDIRECT";

    /**
     *
     */
    public final static String STATE_USE_FLASH_REDIRECT_PARAM_NAME = STATE_FLOW_PREFIX + "DEFAULT_USE_FLASH_IN_REDIRECT";

    /**
     *
     */
    public final static String LOCAL_XPATH_RESOLVER = STATE_FLOW_PREFIX + "LOCAL_XPATH_RESOLVER";

    /**
     *
     */
    public final static String ORYGINAL_SCXML_DEFAULT_SUFIX = ".scxml";

    /**
     *
     */
    public final static String STATE_CHART_DEFAULT_PARAM_NAME = "scxml";

    /**
     *
     */
    public static final String ANNOTATED_CLASSES = STATE_FLOW_PREFIX + "AnnotatedClasses";

    /**
     *
     */
    public static final String CDI_BEAN_MANAGER = STATE_FLOW_PREFIX + "cdi.BeanManager";

    /**
     *
     */
    public static final String CDI_AVAILABLE = STATE_FLOW_PREFIX + "cdi.AvailableFlag";

    /**
     *
     */
    public static final String SCXML_DEFINITION_ID_SUFFIX = ".scxml";

    /**
     *
     */
    public static final String CDI_1_1_OR_LATER = STATE_FLOW_PREFIX + "cdi.OneOneOrLater";

    /**
     *
     */
    public static final String STATE_FLOW_MAP = STATE_FLOW_PREFIX + "flow.MAP";

    /**
     *
     */
    public static final String STATEFLOW_REFRESH_PERIOD_PARAM_NAME = "javax.faces.state.STATEFLOW_REFRESH_PERIOD";

    /**
     *
     */
    public static final String STATE_FLOW_STACK = STATE_FLOW_PREFIX + "FlowStack";
    
    /**
     *
     */
    public static final String SCXML_DATA_MODEL = "http://xmlns.ssoft.org/faces/scxml";
    
    /**
     *
     */
    public static final String FXSCXML_DATA_MODEL = "http://xmlns.ssoft.org/faces/fxscxml";

    /**
     *
     */
    public final static String STATE_FLOW_DISPATCH_TASK = "org.ssoft.faces.state.STATE_FLOW_DISPATCH_TASK";
    
    
}
