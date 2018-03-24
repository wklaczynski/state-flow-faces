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
package org.apache.faces.impl.state.facelets;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.*;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructCustomScopeEvent;
import javax.faces.event.ScopeContext;
import org.apache.scxml.Context;
import org.apache.scxml.SCXMLExecutor;

/**
 *
 * @author waldek
 */
public class StateFlowScopesELResolver extends ELResolver {

    public static final String DIALOG_SCOPE = "scxmlScope";
    public static final String DIALOG_VARIABLE_NAME = "scxml";
    public static final String STATE_VARIABLE_NAME = "state";
    public static final String DIALOG_PARAM_MAP = "org.scxml.attr";

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (null != property.toString()) {
                switch (property.toString()) {
                    case DIALOG_VARIABLE_NAME:
                        context.setPropertyResolved(true);
                        result = getDialogParams(context);
                        break;
                    case STATE_VARIABLE_NAME:
                        context.setPropertyResolved(true);
                        result = getStateParams(context);
                        break;
                    case DIALOG_SCOPE:
                        context.setPropertyResolved(true);
                        result = getDialogScope(context);
                        break;
                    default:
                        break;
                }
            }
        } else if (base instanceof DialogParams) {
            DialogParams scope = (DialogParams) base;
            result = scope.get(property.toString());
            if (result == null) {
                //notf("chart", property);
            }
            context.setPropertyResolved(true);
        } else if (base instanceof StateParams) {
            StateParams scope = (StateParams) base;
            result = scope.get(property.toString());
            if (result == null) {
                //notf("state", property);
            }
            context.setPropertyResolved(true);
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            result = scope.get(property.toString());
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        Class result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (null != property.toString()) {
                switch (property.toString()) {
                    case DIALOG_VARIABLE_NAME:
                        result = DialogParams.class;
                        break;
                    case STATE_VARIABLE_NAME:
                        result = StateParams.class;
                        break;
                    case DIALOG_SCOPE:
                        result = DialogScope.class;
                        break;
                    default:
                        break;
                }
            }
        } else if (base instanceof DialogParams) {
            DialogParams scope = (DialogParams) base;
            Object value = scope.get(property.toString());
            context.setPropertyResolved(true);
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof StateParams) {
            StateParams scope = (StateParams) base;
            context.setPropertyResolved(true);
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        } else if (base instanceof DialogScope) {
            DialogScope scope = (DialogScope) base;
            context.setPropertyResolved(true);
            Object value = scope.get(property.toString());
            if (value != null) {
                result = value.getClass();
            }
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException {
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            scope.put(property.toString(), value);
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
            scope.set(property.toString(), value);
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
            scope.set(property.toString(), value);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws NullPointerException, PropertyNotFoundException, ELException {
        boolean result = false;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            if (property.toString().equals(DIALOG_SCOPE)) {
                context.setPropertyResolved(true);
                result = true;
            } else if (STATE_VARIABLE_NAME.equals(property.toString())) {
                result = true;
            }
        } else if (base instanceof DialogScope) {
            result = false;
        } else if (base instanceof DialogParams) {
            result = false;
        } else if (base instanceof StateParams) {
            result = false;
        }
        return result;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        List<FeatureDescriptor> result = Collections.<FeatureDescriptor>emptyList();
        if (null == base) {
            return Collections.<FeatureDescriptor>emptyList().iterator();
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            Map<String, Object> params = scope;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                FeatureDescriptor desc = new FeatureDescriptor();
                desc.setName(param.getKey());
                desc.setDisplayName("Dialog Scope Object");
                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
                result.add(desc);
            }
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
        } else if (base instanceof StateParams) {
            context.setPropertyResolved(true);
            StateParams scope = (StateParams) base;
        }
        return result.iterator();
    }

    private void notf(String scope, Object property) {
        throw new PropertyNotFoundException(String.format("(%s property '%s' not found)", scope, property));
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (null == base) {
            return null;
        } else if (base instanceof DialogScope) {
            return String.class;
        }
        return null;
    }

    private DialogScope getDialogScope(ELContext context) {
        DialogScope attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = executor.getGlobalContext();
            attrScope = (DialogScope) ctx.get(DIALOG_PARAM_MAP);
            if (attrScope == null) {
                attrScope = new DialogScope();
                ctx.set(DIALOG_PARAM_MAP, attrScope);
                attrScope.onCreate();
            }
        }
        return attrScope;
    }

    private DialogParams getDialogParams(ELContext context) {
        DialogParams attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            attrScope = new DialogParams(executor);
        }
        return attrScope;
    }

    private StateParams getStateParams(ELContext context) {
        StateParams attrScope = null;
        SCXMLExecutor executor = (SCXMLExecutor) context.getContext(SCXMLExecutor.class);
        if (executor != null) {
            Context ctx = (Context) context.getContext(Context.class);
            if (ctx != null) {
                attrScope = new StateParams(ctx);
            }
        }
        return attrScope;
    }

    public class DialogScope extends ConcurrentHashMap<String, Object> implements Serializable {

        public DialogScope() {
            super();
        }

        public void onCreate() {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ScopeContext context = new ScopeContext(DIALOG_SCOPE, this);
            ctx.getApplication().publishEvent(ctx, PostConstructCustomScopeEvent.class, context);
        }
    }

    private static class DialogParams extends AbstractMap<String, Object> implements Serializable {

        private final Context ctx;
        private final SCXMLExecutor executor;

        public DialogParams(SCXMLExecutor executor) {
            this.executor = executor;
            this.ctx = executor.getGlobalContext();
        }

        public Context getCtx() {
            return ctx;
        }

        public SCXMLExecutor getExecutor() {
            return executor;
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

        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            return ctx.getVars().entrySet();
        }

    }

    private static class StateParams implements Serializable {

        private final Context ctx;

        public StateParams(Context ctx) {
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
