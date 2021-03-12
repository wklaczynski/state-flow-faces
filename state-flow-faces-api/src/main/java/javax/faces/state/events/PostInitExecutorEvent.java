/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.SystemEventListener;

/**
 *
 * <p>
 * When an instance of this event is passed to
 * {@link SystemEventListener#processEvent} or {@link
 * ComponentSystemEventListener#processEvent}, the listener implementation may
 * assume that the <code>source</code> of this event instance is in a tree that
 * has just had its executor initialized.</p>
 *
 * @since 1.2
 */
public class PostInitExecutorEvent extends ComponentSystemEvent {

    /**
     * <p>Instantiate a new
     * <code>PostInitExecutorEvent</code> that indicates the argument
     * <code>component</code> just had its state restored.</p>
     *
     * @param component the <code>UIComponent</code> whose state was just
     * restored.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostInitExecutorEvent(UIComponent component) {
        super(component);
    }

    /**
     * <p>Instantiate a new
     * <code>PostInitExecutorEvent</code> that indicates the argument
     * <code>component</code> just had its state restored.</p>
     *
     * @param facesContext the Faces context.
     * @param component the <code>UIComponent</code> whose state was just
     * restored.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     */
    public PostInitExecutorEvent(FacesContext facesContext, UIComponent component) {
        super(facesContext, component);
    }

    public void setComponent(UIComponent newComponent) {
        this.source = newComponent;
    }

}
