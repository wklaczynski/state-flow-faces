/*
 * Copyright 2018 Waldemar Kłaczyński.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.state;

import java.io.Serializable;
import javax.faces.context.FacesContext;

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
    public FlowTriggerEvent(final String name, final int type, final Object payload) {
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

    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[3];
        
        values[0] = name;
        values[1] = type;
        values[2] = payload;

        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        name = (String) values[0];
        type = (int) values[1];
        payload = values[2];
    }
    
    
}

