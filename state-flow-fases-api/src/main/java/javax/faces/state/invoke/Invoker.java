/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.invoke;

import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface Invoker {

    /**
     * Set the invoker type of the owning state for the &lt;invoke&gt;.
     *
     * @param type The type of the parent state.
     */
    void setType(String type);
    
    /**
     * Set the state ID of the owning state for the &lt;invoke&gt;.
     * Implementations must use this ID for constructing the event name for
     * the special "done" event (and optionally, for other event names
     * as well).
     *
     * @param parentStateId The ID of the parent state.
     */
    void setParentStateId(String parentStateId);

    /**
     * Set the "context" of the parent state machine, which provides the
     * channel.
     *
     * @param instance
     */
    void setInstance(FlowInstance instance);

    /**
     * Begin this invocation.
     *
     * @param source The source URI of the activity being invoked.
     * @param params The &lt;param&gt; values
     * @throws InvokerException In case there is a fatal problem with
     *                          invoking the source.
     */
    void invoke(String source, Map params) throws InvokerException;

    /**
     * Forwards the events triggered on the parent state machine
     * on to the invoked activity.
     *
     * @param evts
     *            an array of external events which triggered during the last
     *            time quantum
     *
     * @throws InvokerException In case there is a fatal problem with
     *                          processing the events forwarded by the
     *                          parent state machine.
     */
    void parentEvents(FlowTriggerEvent[] evts) throws InvokerException;

    /**
     * Cancel this invocation.
     *
     * @throws InvokerException In case there is a fatal problem with
     *                          canceling this invoke.
     */
    void cancel() throws InvokerException;

    /**
     * Save state this Context.
     * @param context The FacesContext
     * @return The saved state
     */
    Object saveState(FacesContext context);
    
    
    /**
     * Save state this Context.
     * @param context The FacesContext
     * @param state State object
     */
    void restoreState(FacesContext context, Object state);
    
}

