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


/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowConstants {
    
    public final static String MAP_SCXML_SUFIX = "javax.faces.flow.MAP_SCXML_SUFIX";

    public final static String ORYGINAL_SCXML_SUFIX = "javax.faces.flow.ORYGINAL_SCXML_SUFIX";

    public final static String STATE_CHART_REQUEST_PARAM_NAME = "javax.faces.flow.STATE_CHART_REQUEST_PARAM_NAME";

    public final static String LOCAL_XPATH_RESOLVER = "javax.faces.flow.LOCAL_XPATH_RESOLVER";
    
    public final static String ORYGINAL_SCXML_DEFAULT_SUFIX = ".scxml";

    public final static String STATE_CHART_DEFAULT_PARAM_NAME = "flow";
    
    public final static String STATE_FLOW_PREFIX = "org.apache.faces.state.";

    public static final String ANNOTATED_CLASSES = STATE_FLOW_PREFIX + "AnnotatedClasses";
    
    public static final String CDI_BEAN_MANAGER = STATE_FLOW_PREFIX + "cdi.BeanManager";
    
    public static final String CDI_AVAILABLE = STATE_FLOW_PREFIX + "cdi.AvailableFlag";
    
    public static final String SCXML_DEFINITION_ID_SUFFIX = ".scxml";
    
    public static final String CDI_1_1_OR_LATER = STATE_FLOW_PREFIX + "cdi.OneOneOrLater";

    public static final String STATE_FLOW_MAP = STATE_FLOW_PREFIX + "flow.MAP";
    
    public static final String STATEFLOW_REFRESH_PERIOD_PARAM_NAME =  "javax.faces.state.STATEFLOW_REFRESH_PERIOD";
    
    public static final String STATE_FLOW_STACK = STATE_FLOW_PREFIX + "FlowStack";
    
}