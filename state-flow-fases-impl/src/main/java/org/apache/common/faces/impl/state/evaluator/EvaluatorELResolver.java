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
package org.apache.common.faces.impl.state.evaluator;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLSystemContext;
import org.apache.common.scxml.system.EventVariable;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class EvaluatorELResolver extends ELResolver implements Serializable {

    public static final String DONE_DATA_NAME = "done";
    public static final String EVENT_VAR_NAME = "event";

    private static final Set<String> writeprotect = new HashSet<>(Arrays.asList(
            SCXMLSystemContext.EVENT_KEY));

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (null == property) {
                return null;
            }
            switch (property.toString()) {
                case EVENT_VAR_NAME: {
                    result = getEventVariableParams(context);
                    if (result != null) {
                        context.setPropertyResolved(true);
                    }
                    break;
                }
                case DONE_DATA_NAME: {
                    result = getDoneData(context);
                    if (result != null) {
                        context.setPropertyResolved(true);
                    }
                    break;
                }
                default: {
                    Context ctx = (Context) context.getContext(Context.class);
                    if (ctx != null && ctx.has(property.toString())) {
                        Object value = ctx.get(property.toString());
                        if (value != null) {
                            context.setPropertyResolved(true);
                            result = value;
                        }
                    }
                    break;
                }
            }
        } else if (base instanceof EventVariableParams) {
            context.setPropertyResolved(true);
            EventVariableParams scope = (EventVariableParams) base;
            result = scope.get(property.toString());
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Class result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (null == property) {
                return null;
            }
            switch (property.toString()) {
                case EVENT_VAR_NAME: {
                    if (hasEventVariableParams(context)) {
                        context.setPropertyResolved(true);
                        result = EventVariableParams.class;
                    }
                    break;
                }
                case DONE_DATA_NAME: {
                    Object doneData = getDoneData(context);
                    if (doneData != null) {
                        context.setPropertyResolved(true);
                        result = doneData.getClass();
                    }
                    break;
                }
                default: {
                    Context ctx = (Context) context.getContext(Context.class);
                    if (ctx != null && ctx.has(property.toString())) {
                        context.setPropertyResolved(true);
                        Object value = ctx.get(property.toString());
                        if (value != null) {
                            result = value.getClass();
                        }
                    }
                    break;
                }
            }
        } else if (base instanceof EventVariableParams) {
            context.setPropertyResolved(true);
            EventVariableParams scope = (EventVariableParams) base;
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (null != property) {
                switch (property.toString()) {
                    case DONE_DATA_NAME: {
                        context.setPropertyResolved(true);
                        String message = "Read Only Property";
                        message = message + " base " + base + " property " + property;
                        throw new PropertyNotWritableException(message);
                    }
                    default: {
                        Context ctx = (Context) context.getContext(Context.class);
                        if (ctx != null && ctx.has(property.toString())) {
                            if (writeprotect.contains(property.toString())) {
                                String message = "Read Only Property";
                                message = message + " base " + base + " property " + property;
                                throw new PropertyNotWritableException(message);
                            }
                            Object old = ctx.get(property.toString());
                            if (old != null) {
                                context.setPropertyResolved(true);
                                ctx.set(property.toString(), value);
                            }
                        }
                    }
                }
            }
        } else if (base instanceof EventVariableParams) {
            context.setPropertyResolved(true);
            String message = "Read Only Property";
            message = message + " base " + base + " property " + property;
            throw new PropertyNotWritableException(message);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        if (null == base) {
            if (null == property) {
                return false;
            }
            switch (property.toString()) {
                case EVENT_VAR_NAME:
                case DONE_DATA_NAME: {
                    context.setPropertyResolved(true);
                    return true;
                }
                default: {
                    Context ctx = (Context) context.getContext(Context.class);
                    if (ctx != null && ctx.has(property.toString())) {
                        context.setPropertyResolved(true);
                        return writeprotect.contains(property.toString());
                    }
                }
            }
        } else if (base instanceof EventVariableParams) {
            context.setPropertyResolved(true);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    private void notf(String scope, Object property) {
        throw new PropertyNotFoundException(String.format("(%s property '%s' not found)", scope, property));
    }

    private Object getDoneData(ELContext context) {
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                EventVariable event = (EventVariable) ctx.get(SCXMLSystemContext.EVENT_KEY);
                if (event != null) {
                    Object donedata = event.getData();
                    return donedata;
                }
            }
        }
        return null;
    }

    private EventVariableParams getEventVariableParams(ELContext context) {
        EventVariableParams result = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                EventVariable event = (EventVariable) ctx.get(SCXMLSystemContext.EVENT_KEY);
                result = new EventVariableParams(ctx, event);
            }
        }
        return result;
    }

    private boolean hasEventVariableParams(ELContext context) {
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                return ctx.has(SCXMLSystemContext.EVENT_KEY);
            }
        }
        return false;
    }

    private static class EventVariableParams implements Serializable {

        private static final Set<String> names = new HashSet<>(Arrays.asList(
                "name", "type", "sendid", "origin", "origintype", "invokeid", "data"));

        private final Context ctx;
        private final EventVariable event;

        public EventVariableParams(Context ctx, EventVariable event) {
            this.ctx = ctx;
            this.event = event;
        }

        public Context getCtx() {
            return ctx;
        }

        public EventVariable getEvent() {
            return event;
        }

        public Object get(String name) {
            switch (name) {
                case "name": {
                    return event.getName();
                }
                case "type": {
                    return event.getType();
                }
                case "sendid": {
                    return event.getSendid();
                }
                case "origin": {
                    return event.getOrigin();
                }
                case "origintype": {
                    return event.getOrigintype();
                }
                case "invokeid": {
                    return event.getInvokeid();
                }
                case "data": {
                    return event.getData();
                }
            }

            return null;
        }

        public void set(String name, Object value) {

        }

        public boolean has(String name) {
            return names.contains(name);
        }
    }

}
