/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.events;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.FacesListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * <p>
 * <strong>
 * ExecuteSystemEvent</strong> is the base class for {@link SystemEvent}s that
 * are specific to a {@link
 * UIComponent} instance.</p>
 *
 * @since 1.2
 */
public class ExecuteSystemEvent extends SystemEvent {

    /**
     * <p>
     * Pass the argument <code>component</code> to the superclass
     * constructor.</p>
     *
     * @param executor the <code>SCXMLExecutor</code> reference to be passed to
     * the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     *
     * @since 1.2
     */
    public ExecuteSystemEvent(SCXMLExecutor executor) {
        super(executor);
    }

    /**
     * <p>
     * Pass the argument <code>component</code> to the superclass
     * constructor.</p>
     *
     * @param facesContext the Faces context.
     * @param executor the <code>SCXMLExecutor</code> reference to be passed to
     * the superclass constructor.
     *
     * @throws IllegalArgumentException if the argument is <code>null</code>.
     *
     * @since 1.2
     */
    public ExecuteSystemEvent(FacesContext facesContext, SCXMLExecutor executor) {
        super(facesContext, executor);
    }

    /**
     * <p>
     * Return <code>true</code> if the argument {@link FacesListener} is an
     * instance of the appropriate listener class that this event supports. The
     * default implementation returns true if the listener is a
     * {@link ComponentSystemEventListener} or if
     * <code>super.isAppropriateListener()</code> returns true.</p>
     *
     * @param listener {@link FacesListener} to evaluate
     */
    @Override
    public boolean isAppropriateListener(FacesListener listener) {
        boolean result = (listener instanceof ExecutorEventListener);
        if (!result) {
            result = super.isAppropriateListener(listener);
        }
        return result;
    }

    public SCXMLExecutor getExecutor() {
        return (SCXMLExecutor) getSource();
    }

   /**
     * <p class="changed_added_2_2">Broadcast this event instance to 
     * the specified {@link FacesListener} by calling the superclass's
     * <code>processListener()</code> implementation.</p>
     *
     * @param listener {@link FacesListener} to evaluate
     * @since 2.2
     */
    @Override
    public void processListener(FacesListener listener) {
        SCXMLExecutor c = getExecutor();
        UIComponent cFromStack;
        boolean didPush = false;
        FacesContext context = FacesContext.getCurrentInstance();
        cFromStack = UIComponent.getCurrentComponent(context);
        if (null == cFromStack) {
            didPush = true;
            //c.pushComponentToEL(context, null);
        }
        try {
            if (listener instanceof SystemEventListener) {
                super.processListener(listener);
            } else if (listener instanceof ExecutorEventListener) {
                ((ExecutorEventListener)listener).processEvent(this);
            }
        } finally {
            if (didPush) {
                //c.popComponentFromEL(context);
            }
        }
    }    
    
}
