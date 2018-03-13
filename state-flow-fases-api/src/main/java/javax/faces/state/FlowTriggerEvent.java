/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.io.Serializable;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowTriggerEvent implements Serializable {

    /**
     * Constructor.
     *
     * @param name The event name
     * @param type The event type
     * @param payload The event payload, must be {@link Serializable}
     */
    public FlowTriggerEvent(final String name, final int type,
            final Object payload) {
        super();
        this.name = name;
        this.type = type;
        this.payload = payload;
    }

    /**
     * Constructor.
     *
     * @param name The event name
     * @param type The event type
     */
    public FlowTriggerEvent(final String name, final int type) {
        this(name, type, null);
    }

    /**
     * <code>CALL_EVENT</code>.
     */
    public static final int CALL_EVENT = 1;

    /**
     * <code>CHANGE_EVENT</code>.
     *
     */
    public static final int CHANGE_EVENT = 2;

    /**
     * <code>SIGNAL_EVENT</code>.
     *
     */
    public static final int SIGNAL_EVENT = 3;

    /**
     * <code>TIME_EVENT</code>.
     *
     */
    public static final int TIME_EVENT = 4;

    /**
     * <code>ERROR_EVENT</code>.
     *
     */
    public static final int ERROR_EVENT = 5;

    /**
     * The event name.
     *
     */
    private String name;

    /**
     * The event type.
     *
     */
    private int type;

    /**
     * The event payload.
     *
     */
    private Object payload;

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return Returns the payload.
     */
    public Object getPayload() {
        return payload;
    }

    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }

    /**
     * Define an equals operator for FlowTriggerEvent.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FlowTriggerEvent) {
            FlowTriggerEvent te2 = (FlowTriggerEvent) obj;
            if (type == te2.type && name.equals(te2.name)
                && ((payload == null && te2.payload == null)
                     || (payload != null && payload.equals(te2.payload)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of this FlowTriggerEvent object.
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("TriggerEvent{name=");
        buf.append(name).append(",type=").append(type);
        if (payload != null) {
            buf.append(",payload=").append(payload.toString());
        }
        buf.append("}");
        return String.valueOf(buf);
    }

    /**
     * Returns the hash code for this FlowTriggerEvent object.
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return String.valueOf(this).hashCode();
    }

}

