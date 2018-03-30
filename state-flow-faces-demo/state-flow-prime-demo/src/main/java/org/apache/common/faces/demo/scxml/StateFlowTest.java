/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.demo.scxml;

import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import javax.faces.context.FacesContext;
import org.apache.common.scxml.model.Action;
import org.apache.common.faces.state.annotation.StateChartAction;
import org.apache.common.scxml.ActionExecutionContext;
import org.apache.common.scxml.SCXMLExpressionException;
import org.apache.common.scxml.model.ActionExecutionError;
import org.apache.common.scxml.model.ModelException;

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
