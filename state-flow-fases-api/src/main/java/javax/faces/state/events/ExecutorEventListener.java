/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesListener;
import javax.faces.event.SystemEvent;

/**
 *  <p class="changed_added_2_0">Implementors of this class do not need
 *  an <code>isListenerForSource()</code> method because they are only
 *  installed on specific component instances, therefore the
 *  <code>isListenerForSource()</code> method is implicit.  Also, the 
 * {@link #processEvent} method on this interface takes a 
 * {@link ExecuteSystemEvent} because the event will always be associated with
 * a {@link javax.faces.component.UIComponent} instance.</p>
 *
 * @since 2.0
 */
public interface ExecutorEventListener extends FacesListener {

    /**
     * <p>When called, the listener can assume that any guarantees given
     * in the javadoc for the specific {@link SystemEvent}
     * subclass are true.</p>
     *
     * @param event the <code>ExecuteSystemEvent</code> instance that
     * is being processed.
     *
     * @throws AbortProcessingException if lifecycle processing should
     * cease for this request.
     */
    public void processEvent(ExecuteSystemEvent event) throws AbortProcessingException;
    
}
