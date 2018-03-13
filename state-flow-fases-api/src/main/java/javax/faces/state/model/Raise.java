/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.util.Collection;
import java.util.logging.Level;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Raise extends Action {

    /**
     * The event of the derived event to be generated.
     */
    private String event;

    /**
     * Constructor.
     */
    public Raise() {
        super();
    }

    /**
     * Get the event event.
     *
     * @return Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * Set the event event.
     *
     * @param event The event event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
    throws ModelException, FlowExpressionException {

        if (log.isLoggable(Level.FINER)) {
            log.log(Level.FINER, "<event>: Adding event named ''{0}'' to list of derived events.", event);
        }
        FlowTriggerEvent ev = new FlowTriggerEvent(event, FlowTriggerEvent.SIGNAL_EVENT);
        derivedEvents.add(ev);

    }

}

