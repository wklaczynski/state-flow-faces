/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state;


/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowConstants {
    
    public final static String MAP_SCXML_SUFIX = "javax.faces.MAP_SCXML_SUFIX";

    public final static String ORYGINAL_SCXML_SUFIX = "javax.faces.ORYGINAL_SCXML_SUFIX";
    
    public final static String ORYGINAL_SCXML_DEFAULT_SUFIX = ".scxml";
    
    public final static String STATE_FLOW_PREFIX = "org.apache.faces.state.";

    public static final String ANNOTATED_CLASSES = STATE_FLOW_PREFIX + "AnnotatedClasses";
    
    public static final String CDI_BEAN_MANAGER = STATE_FLOW_PREFIX + "cdi.BeanManager";
    
    public static final String CDI_AVAILABLE = STATE_FLOW_PREFIX + "cdi.AvailableFlag";
    
    public static final String SCXML_DEFINITION_ID_SUFFIX = ".scxml";
    
    public static final String CDI_1_1_OR_LATER = STATE_FLOW_PREFIX + "cdi.OneOneOrLater";

    public static final String STATE_FLOW_MAP = STATE_FLOW_PREFIX + "flow.MAP";
    
    public static final String STATEFLOW_REFRESH_PERIOD_PARAM_NAME =  "javax.faces.state.STATEFLOW_REFRESH_PERIOD";
    
    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";
    
}
