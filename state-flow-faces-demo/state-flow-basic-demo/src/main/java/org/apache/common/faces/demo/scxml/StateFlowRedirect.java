/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.demo.scxml;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
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
@StateChartAction(value = "redirect", namespaceURI = "http://xmlns.apache.org/faces/basic/demo")
public class StateFlowRedirect extends Action {

    static final Logger log = Logger.getLogger(StateFlowRedirect.class.getName());

    private String url;
    private String action;

    public StateFlowRedirect() {
        super();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException, ActionExecutionError {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            if (action != null) {
                String actionURL = fc.getApplication().
                        getViewHandler().getActionURL(fc, action);

                String redirectPath = ec.encodeRedirectURL(actionURL, new HashMap<>());
                ec.redirect(redirectPath);
            } else {
                String actionURL = url;

                String redirectPath = ec.encodeRedirectURL(actionURL, new HashMap<>());
                ec.redirect(redirectPath);
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

}
