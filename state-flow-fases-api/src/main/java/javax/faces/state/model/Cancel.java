/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.util.Collection;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Cancel extends Action {

    /**
     * Constructor.
     */
    public Cancel() {
        super();
    }

    /**
     * The ID of the send message that should be cancelled.
     */
    private String sendid;

    /**
     * Get the ID of the send message that should be cancelled.
     *
     * @return Returns the sendid.
     */
    public String getSendid() {
        return sendid;
    }

    /**
     * Set the ID of the send message that should be cancelled.
     *
     * @param sendid The sendid to set.
     */
    public void setSendid(final String sendid) {
        this.sendid = sendid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {
        evtDispatcher.cancel(sendid);
    }

}
