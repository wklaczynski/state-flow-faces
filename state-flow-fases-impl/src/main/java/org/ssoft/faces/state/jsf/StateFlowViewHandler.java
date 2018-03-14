package org.ssoft.faces.state.jsf;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.context.FacesContext;
import static org.ssoft.faces.state.FlowConstants.MAP_SCXML_SUFIX;
import static org.ssoft.faces.state.FlowConstants.ORYGINAL_SCXML_DEFAULT_SUFIX;
import static org.ssoft.faces.state.FlowConstants.ORYGINAL_SCXML_SUFIX;
import org.ssoft.faces.state.config.StateWebConfiguration;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewHandler extends ViewHandlerWrapper {

    private final ViewHandler wrapped;
    private String sufix;
    private String defsufix;
    private final StateWebConfiguration webcfg;

    public StateFlowViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
        webcfg = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public String deriveLogicalViewId(FacesContext context, String input) {
//        if (!input.endsWith(getDefSufix()) && input.endsWith(getSufix())) {
//            String path = input.substring(0, input.lastIndexOf(sufix));
//            path += getDefSufix();
//            input = path;
//        }
        return super.deriveLogicalViewId(context, input);
    }

    private String getSufix() {
        if (sufix == null) {
            sufix = webcfg.getOptionValue(MAP_SCXML_SUFIX, ORYGINAL_SCXML_DEFAULT_SUFIX);
        }
        return sufix;
    }

    private String getDefSufix() {
        if (defsufix == null) {
            String[] values = webcfg.getOptionValues(ORYGINAL_SCXML_SUFIX, " ");
            if (values.length == 0) {
                defsufix = ORYGINAL_SCXML_DEFAULT_SUFIX;
            } else {
                defsufix = values[0];
            }
        }
        return defsufix;
    }

}
