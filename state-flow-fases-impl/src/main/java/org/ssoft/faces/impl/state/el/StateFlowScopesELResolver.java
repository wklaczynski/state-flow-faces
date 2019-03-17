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
package org.ssoft.faces.impl.state.el;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.el.*;
import javax.faces.context.FacesContext;
import javax.faces.event.PostConstructCustomScopeEvent;
import javax.faces.event.ScopeContext;
import javax.faces.state.StateChartExecuteContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author waldek
 */
public class StateFlowScopesELResolver extends ELResolver {
    
    /**
     *
     */
    public static final String DIALOG_SCOPE = "dialogScope";

    /**
     *
     */
    public static final String DIALOG_VARIABLE_NAME = "dialog";

    /**
     *
     */
    public static final String CHART_SCOPE = "chartScope";

    /**
     *
     */
    public static final String CHART_VARIABLE_NAME = "chart";

    /**
     *
     */
    public static final String SCXML_SCOPE = "scxmlScope";

    /**
     *
     */
    public static final String SCXML_VARIABLE_NAME = "scxml";
    
    /**
     *
     */
    public static final String STATE_VARIABLE_NAME = "state";

    /**
     *
     */
    public static final String CHART_PARAM_MAP = "org.scxml.attr";

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
                    case DIALOG_SCOPE:
                        context.setPropertyResolved(true);
                        result = getDialogScope(context);
                        break;
                    case CHART_VARIABLE_NAME:
                    case SCXML_VARIABLE_NAME:
                        context.setPropertyResolved(true);
                        result = getChartParams(context);
                        break;
                    case STATE_VARIABLE_NAME:
                        context.setPropertyResolved(true);
                        result = getStateParams(context);
                        break;
                    case CHART_SCOPE:
                    case SCXML_SCOPE:
                        context.setPropertyResolved(true);
                        result = getChartScope(context);
                        break;
                    default:
                        break;
                }
            }
        } else if (base instanceof DialogParams) {
            DialogParams scope = (DialogParams) base;
            result = scope.get(property.toString());
            context.setPropertyResolved(true);
        } else if (base instanceof ChartParams) {
            ChartParams scope = (ChartParams) base;
            result = scope.get(property.toString());
            context.setPropertyResolved(true);
        } else if (base instanceof StateParams) {
            StateParams scope = (StateParams) base;
            result = scope.get(property.toString());
            context.setPropertyResolved(true);
        } else if (base instanceof DialogScope) {
            context.setPropertyResolved(true);
            DialogScope scope = (DialogScope) base;
            result = scope.get(property.toString());
        } else if (base instanceof ChartScope) {
            context.setPropertyResolved(true);
            ChartScope scope = (ChartScope) base;
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
                    case DIALOG_SCOPE:
                        result = DialogScope.class;
                        break;
                    case DIALOG_VARIABLE_NAME:
                        result = DialogParams.class;
                        break;
                    case CHART_SCOPE:
                    case SCXML_SCOPE:
                        result = ChartScope.class;
                        break;
                    default:
                        break;
                    case CHART_VARIABLE_NAME:
                    case SCXML_VARIABLE_NAME:
                        result = ChartParams.class;
                        break;
                    case STATE_VARIABLE_NAME:
                        result = StateParams.class;
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
        } else if (base instanceof ChartParams) {
            ChartParams scope = (ChartParams) base;
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
        } else if (base instanceof ChartScope) {
            ChartScope scope = (ChartScope) base;
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
        } else if (base instanceof ChartScope) {
            context.setPropertyResolved(true);
            ChartScope scope = (ChartScope) base;
            scope.put(property.toString(), value);
        } else if (base instanceof DialogParams) {
            context.setPropertyResolved(true);
            DialogParams scope = (DialogParams) base;
            scope.set(property.toString(), value);
        } else if (base instanceof ChartParams) {
            context.setPropertyResolved(true);
            ChartParams scope = (ChartParams) base;
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
            switch (property.toString()) {
                case DIALOG_SCOPE:
                    context.setPropertyResolved(true);
                    result = true;
                    break;
                case DIALOG_VARIABLE_NAME:
                    context.setPropertyResolved(true);
                    result = true;
                    break;
                case CHART_SCOPE:
                case SCXML_SCOPE:
                    context.setPropertyResolved(true);
                    result = true;
                    break;
                case CHART_VARIABLE_NAME:
                case SCXML_VARIABLE_NAME:
                    context.setPropertyResolved(true);
                    result = true;
                    break;
                default:
                    break;
            }
        } else if (base instanceof DialogScope) {
            result = false;
        } else if (base instanceof ChartScope) {
            result = false;
        } else if (base instanceof DialogParams) {
            result = false;
        } else if (base instanceof ChartParams) {
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
        } else if (base instanceof ChartScope) {
            context.setPropertyResolved(true);
            ChartScope scope = (ChartScope) base;
            Map<String, Object> params = scope;
            for (Map.Entry<String, Object> param : params.entrySet()) {
                FeatureDescriptor desc = new FeatureDescriptor();
                desc.setName(param.getKey());
                desc.setDisplayName("Chart Scope Object");
                desc.setValue(ELResolver.TYPE, param.getValue().getClass());
                desc.setValue(ELResolver.RESOLVABLE_AT_DESIGN_TIME, true);
                result.add(desc);
            }
        } else if (base instanceof ChartParams) {
            context.setPropertyResolved(true);
            ChartParams scope = (ChartParams) base;
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
        } else if (base instanceof ChartScope) {
            return String.class;
        }
        return null;
    }

    private DialogScope getDialogScope(ELContext context) {
        DialogScope attrScope = null;
        
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateChartExecuteContext ec = handler.getCurrentExecuteContext(fc);
        
        if (ec != null) {
            Context ctx = ec.getExecutor().getRootContext();
            attrScope = (DialogScope) ctx.get(CHART_PARAM_MAP);
            if (attrScope == null) {
                attrScope = new DialogScope();
                ctx.set(CHART_PARAM_MAP, attrScope);
                attrScope.onCreate();
            }
        }
        return attrScope;
    }

    private ChartScope getChartScope(ELContext context) {
        ChartScope attrScope = null;
        
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateChartExecuteContext ec = handler.getCurrentExecuteContext(fc);
        
        if (ec != null) {
            Context ctx = ec.getExecutor().getGlobalContext();
            attrScope = (ChartScope) ctx.get(CHART_PARAM_MAP);
            if (attrScope == null) {
                attrScope = new ChartScope();
                ctx.set(CHART_PARAM_MAP, attrScope);
                attrScope.onCreate();
            }
        }
        return attrScope;
    }
    
    private DialogParams getDialogParams(ELContext context) {
        DialogParams attrScope = null;
        
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateChartExecuteContext ec = handler.getCurrentExecuteContext(fc);
        
        if (ec != null) {
            SCXMLExecutor executor = ec.getExecutor();
            attrScope = new DialogParams(executor);
        }
        return attrScope;
    }

    private ChartParams getChartParams(ELContext context) {
        ChartParams attrScope = null;
        
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateChartExecuteContext ec = handler.getCurrentExecuteContext(fc);
        
        if (ec != null) {
            SCXMLExecutor executor = ec.getExecutor();
            attrScope = new ChartParams(executor);
        }
        return attrScope;
    }

    private StateParams getStateParams(ELContext context) {
        StateParams attrScope = null;

        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateChartExecuteContext executeContext = handler.getCurrentExecuteContext(fc);
        
        if (executeContext != null) {
            Context ctx = executeContext.getContext();
            if (ctx != null) {
                attrScope = new StateParams(ctx);
            }
        }
        return attrScope;
    }

    /**
     *
     */
    public class DialogScope extends ConcurrentHashMap<String, Object> implements Serializable {

        /**
         *
         */
        public DialogScope() {
            super();
        }

        /**
         *
         */
        public void onCreate() {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ScopeContext context = new ScopeContext(CHART_SCOPE, this);
            ctx.getApplication().publishEvent(ctx, PostConstructCustomScopeEvent.class, context);
        }
    }

    /**
     *
     */
    public class ChartScope extends ConcurrentHashMap<String, Object> implements Serializable {

        /**
         *
         */
        public ChartScope() {
            super();
        }

        /**
         *
         */
        public void onCreate() {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ScopeContext context = new ScopeContext(CHART_SCOPE, this);
            ctx.getApplication().publishEvent(ctx, PostConstructCustomScopeEvent.class, context);
        }
    }
    
    private static class DialogParams extends AbstractMap<String, Object> implements Serializable {

        private final Context ctx;
        private final SCXMLExecutor executor;

        public DialogParams(SCXMLExecutor executor) {
            this.executor = executor;
            this.ctx = executor.getRootContext();
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
    
    private static class ChartParams extends AbstractMap<String, Object> implements Serializable {

        private final Context ctx;
        private final SCXMLExecutor executor;

        public ChartParams(SCXMLExecutor executor) {
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
