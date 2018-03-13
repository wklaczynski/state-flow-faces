package org.apache.faces.state.jsf;

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

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewHandler extends ViewHandlerWrapper {

    private final ViewHandler wrapped;
    private String sufix;

    public StateFlowViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public String deriveLogicalViewId(FacesContext context, String input) {
        if (input.endsWith(getSufix())) {
            String path = input.substring(0, input.lastIndexOf(sufix));
            path += ".scxml";
            input = path;
        }
        return super.deriveLogicalViewId(context, input);
    }

    private String getSufix() {
        if (sufix == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            sufix = context.getExternalContext().getInitParameter("javax.faces.DIALOG_ACTION_SCXML_SUFIX");
            if (sufix == null) {
                sufix = ".scxml";
            }
        }
        return sufix;
    }

}
