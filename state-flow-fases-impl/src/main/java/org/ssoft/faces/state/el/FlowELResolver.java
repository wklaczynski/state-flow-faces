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
package org.ssoft.faces.state.el;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotWritableException;
import javax.scxml.Context;
import javax.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowELResolver extends ELResolver implements Serializable {

    public static final String STATE_RESULT_NAME = "result";

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (STATE_RESULT_NAME.equals(property.toString())) {
                context.setPropertyResolved(true);
                result = getResultParams(context);
            } else {
                Context ctx = (Context) context.getContext(Context.class);
                if (ctx != null && ctx.has(property.toString())) {
                    Object value = ctx.get(property.toString());
                    if (value != null) {
                        context.setPropertyResolved(true);
                        result = value;
                    }
                }
            }
        } else if (base instanceof ResultParams) {
            context.setPropertyResolved(true);
            ResultParams scope = (ResultParams) base;
            result = scope.get(property.toString());
            if (result == null) {
                String message = " base " + base + " property " + property;
                throw new PropertyNotFoundException(message);
            }
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
            if (STATE_RESULT_NAME.equals(property.toString())) {
                result = ResultParams.class;
            } else {
                Context ctx = (Context) context.getContext(Context.class);
                if (ctx != null && ctx.has(property.toString())) {
                    Object value = ctx.get(property.toString());
                    if (value != null) {
                        context.setPropertyResolved(true);
                        result = value.getClass();
                    }
                }
            }
        } else if (base instanceof ResultParams) {
            context.setPropertyResolved(true);
            ResultParams scope = (ResultParams) base;
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
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null && ctx.has(property.toString())) {
                Object old = ctx.get(property.toString());
                if (old != null) {
                    context.setPropertyResolved(true);
                    ctx.set(property.toString(), value);
                }
            }
        } else if (base instanceof ResultParams) {
            context.setPropertyResolved(true);
            String message = "Read Only Property";
            message = message + " base " + base + " property " + property;
            throw new PropertyNotWritableException(message);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        boolean result = false;
        if (null == base) {
            if (property.toString().equals(STATE_RESULT_NAME)) {
                context.setPropertyResolved(true);
                result = true;
            } else {
                Context ctx = (Context) context.getContext(Context.class);
                if (ctx != null && ctx.has(property.toString())) {
                    context.setPropertyResolved(true);
                    result = false;
                }
            }
        } else if (base instanceof ResultParams) {
            context.setPropertyResolved(true);
            result = true;
        }
        return result;
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

    private ResultParams getResultParams(ELContext context) {
        ResultParams attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                Context result = (Context) ctx.get("__@result@__");
                if (result != null) {
                    attrScope = new ResultParams(result);
                }
            }
        }
        return attrScope;
    }

    private static class ResultParams implements Serializable {

        private final Context ctx;

        public ResultParams(Context ctx) {
            this.ctx = ctx;
        }

        public Context getCtx() {
            return ctx;
        }

        public Object get(String name) {
            return ctx.get(name);
        }

        public void set(String name, Object value) {
            ctx.setLocal(name, value);
        }

        public boolean has(String name) {
            return ctx.has(name);
        }
    }

}
