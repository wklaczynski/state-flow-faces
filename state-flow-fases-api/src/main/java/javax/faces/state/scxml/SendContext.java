/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.scxml;

import java.util.Map;
import java.util.logging.Logger;
import javax.faces.state.scxml.model.Invoke;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SendContext {
    
    private final String id;

    private final String type;

    private final String target;

    private final String event;
    
    private final Object data;
    
    private final Object hints;
    
    private final long delay;
    
    private final ActionExecutionContext exctx;
    private final Context ctx;

    public SendContext(String id, String type, String target, String event, Object data, Object hints, long delay, ActionExecutionContext exctx, Context ctx) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.event = event;
        this.data = data;
        this.hints = hints;
        this.delay = delay;
        this.exctx = exctx;
        this.ctx = ctx;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getTarget() {
        return target;
    }

    public String getEvent() {
        return event;
    }

    public Object getData() {
        return data;
    }

    public Object getHints() {
        return hints;
    }

    public long getDelay() {
        return delay;
    }

    /**
     * @return Returns the state machine
     */
    public SCXML getStateMachine() {
        return exctx.getStateMachine();
    }

    /**
     * @return Returns the global context
     */
    public Context getGlobalContext() {
        return exctx.getGlobalContext();
    }

    /**
     * @return Returns the context
     */
    public Context getCurrentContext() {
        return ctx;
    }

    /**
     * @return Returns The evaluator.
     */
    public Evaluator getEvaluator() {
        return exctx.getEvaluator();
    }

    /**
     * @return Returns the error reporter
     */
    public ErrorReporter getErrorReporter() {
        return exctx.getErrorReporter();
    }

    /**
     * @return Returns the event dispatcher
     */
    public EventDispatcher getEventDispatcher() {
        return exctx.getEventDispatcher();
    }

    /**
     * @return Returns the I/O Processor for the internal event queue
     */
    public SCXMLIOProcessor getInternalIOProcessor() {
        return exctx.getInternalIOProcessor();
    }

    /**
     * @return Returns the map of current active Invokes and their invokeId
     */
    public Map<Invoke, String> getInvokeIds() {
        return exctx.getInvokeIds();
    }
    
    /**
     * @return Returns the SCXML Execution Logger for the application
     */
    public Logger getAppLog() {
        return exctx.getAppLog();
    }
    
}
