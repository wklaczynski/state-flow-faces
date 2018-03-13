/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.List;
import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FlowEventDispatcher {

    /**
     * Cancel the specified send message.
     *
     * @param sendId The ID of the send message to cancel
     */
    void cancel(String sendId);

    /**
     * Send this message to the target.
     *
     * @param sendId The ID of the send message
     * @param target An expression returning the target location of the event
     * @param targetType The type of the Event I/O Processor that the event
     *  should be dispatched to
     * @param event The type of event being generated.
     * @param params A list of zero or more whitespace separated variable
     *  names to be included with the event.
     * @param hints The data containing information which may be
     *  used by the implementing platform to configure the event processor
     * @param delay The event is dispatched after the delay interval elapses
     * @param externalNodes The list of external nodes associated with
     *  the &lt;send&gt; element.
     */
    void send(String sendId, String target, String targetType,
            String event, Map params, Object hints, long delay,
            List externalNodes);

}

