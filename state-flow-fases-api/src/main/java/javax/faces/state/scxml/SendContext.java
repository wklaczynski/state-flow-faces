/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.scxml;

import java.util.Map;

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
    
    private final Map<String, SCXMLIOProcessor> ioProcessors;

    public SendContext(String id, String type, String target, String event, Object data, Object hints, long delay, Map<String, SCXMLIOProcessor> ioProcessors) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.event = event;
        this.data = data;
        this.hints = hints;
        this.delay = delay;
        this.ioProcessors = ioProcessors;
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

    public Map<String, SCXMLIOProcessor> getIoProcessors() {
        return ioProcessors;
    }
    
}
