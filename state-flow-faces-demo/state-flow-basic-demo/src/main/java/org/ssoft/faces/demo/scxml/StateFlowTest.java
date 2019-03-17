/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.demo.scxml;

import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.model.Action;
import javax.faces.state.annotation.StateChartAction;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.model.ActionExecutionError;
import javax.faces.state.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateChartAction(value = "test", namespaceURI = "http://xmlns.apache.org/faces/basic/demo")
public class StateFlowTest extends Action {

    static final Logger log = Logger.getLogger(StateFlowTest.class.getName());

    private String message;

    public StateFlowTest() {
        super();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException, ActionExecutionError {
        FacesContext fc = FacesContext.getCurrentInstance();

        FacesMessage facesMessage = new FacesMessage(message);
        facesMessage.setSeverity(SEVERITY_INFO);
        fc.addMessage(null, facesMessage);
    }

}
