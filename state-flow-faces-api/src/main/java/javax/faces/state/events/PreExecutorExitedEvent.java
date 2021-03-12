/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.SystemEventListener;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PreExecutorExitedEvent extends ExecuteSystemEvent {

    public PreExecutorExitedEvent(SCXMLExecutor executor) {
        super(executor);
    }

    public PreExecutorExitedEvent(FacesContext facesContext, SCXMLExecutor executor) {
        super(facesContext, executor);
    }

    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof SystemEventListener);
    }

}
