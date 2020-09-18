/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.context.FacesContext;
import javax.faces.event.FacesListener;
import javax.faces.event.SystemEventListener;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PostExecuteEvent extends ExecuteSystemEvent {

    public PostExecuteEvent(SCXMLExecutor executor) {
        super(executor);
    }

    public PostExecuteEvent(FacesContext facesContext, SCXMLExecutor executor) {
        super(facesContext, executor);
    }

    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        return (listener instanceof SystemEventListener);
    }

}
