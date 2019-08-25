/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.event;

import javax.faces.context.FacesContext;
import javax.faces.event.SystemEvent;
import javax.faces.state.scxml.SendContext;
import javax.faces.state.scxml.model.Invoke;

/**
 *
 * <p>SendSystemEvent is
 * the base class for {@link SystemEvent}s that are specific to a {@link
 * Invoke} instance.</p>
 *
 * @since 1.2
 */
public final class SendSystemEvent extends SystemEvent {


    /**
     * <p>Pass the argument
     * <code>component</code> to the superclass constructor.</p>

     * @param context the <code>String</code> reference to be
     * passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     * 
     */
    public SendSystemEvent(SendContext context) {
        super(context);
    }

    /**
     * <p>Pass the argument
     * <code>component</code> to the superclass constructor.</p>
     * 
     * @param facesContext the Faces context.
     * @param context the <code>String</code> reference to be
     * passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     * 
     */
    public SendSystemEvent(FacesContext facesContext, String context) {
        super(facesContext, context);
    }
    
    /**
     * 
     * @return the component for this event
     */
    public SendContext getSendContext() {
        return ((SendContext) getSource());

    }    

}
