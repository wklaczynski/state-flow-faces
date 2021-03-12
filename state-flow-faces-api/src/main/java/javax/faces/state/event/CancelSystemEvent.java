/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.event;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.SystemEvent;
import javax.faces.state.scxml.model.Invoke;

/**
 *
 * <p>SendSystemEvent is
 * the base class for {@link SystemEvent}s that are specific to a {@link
 * Invoke} instance.</p>
 *
 * @since 1.2
 */
public final class CancelSystemEvent extends SystemEvent {


    /**
     * <p>Pass the argument
     * <code>component</code> to the superclass constructor.</p>

     * @param sendid the <code>String</code> reference to be
     * passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     * 
     */
    public CancelSystemEvent(String sendid) {
        super(sendid);
    }

    /**
     * <p>Pass the argument
     * <code>component</code> to the superclass constructor.</p>
     * 
     * @param facesContext the Faces context.
     * @param sendid the <code>String</code> reference to be
     * passed to the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     * 
     */
    public CancelSystemEvent(FacesContext facesContext, String sendid) {
        super(facesContext, sendid);
    }
    
    /**
     * 
     * @return the component for this event
     */
    public String getSendId() {
        return ((String) getSource());

    }    

}
