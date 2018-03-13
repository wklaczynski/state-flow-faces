/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.state.FlowEventDispatcher;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowEventDispatcherImpl implements FlowEventDispatcher {

    /**
     * Implementation independent log category.
     */
    public static final Logger log = FlowLogger.FLOW.getLogger();

    /**
     * Constructor.
     */
    public FlowEventDispatcherImpl() {
        super();
    }

    /**
     * @see EventDispatcher#cancel(String)
     */
    @Override
    public void cancel(final String sendId) {
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, "cancel( sendId: {0})", sendId);
        }
    }

    /**
     * @see
     * EventDispatcher#send(String,String,String,String,Map,Object,long,List)
     */
    @Override
    public void send(final String sendId, final String target,
            final String targetType, final String event, final Map params,
            final Object hints, final long delay, final List externalNodes) {
        if (log.isLoggable(Level.INFO)) {
            StringBuilder buf = new StringBuilder();
            buf.append("send ( sendId: ").append(sendId);
            buf.append(", target: ").append(target);
            buf.append(", targetType: ").append(targetType);
            buf.append(", event: ").append(event);
            buf.append(", params: ").append(String.valueOf(params));
            buf.append(", hints: ").append(String.valueOf(hints));
            buf.append(", delay: ").append(delay);
            buf.append(')');
            log.info(buf.toString());
        }

    }

}
