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
package org.apache.faces.impl.state.evaluator;

import com.sun.faces.facelets.el.ELText;
import org.apache.faces.impl.state.el.CompositeFunctionMapper;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.context.FacesContext;
import org.apache.faces.impl.state.StateFlowContext;
import org.apache.scxml.Context;
import org.apache.scxml.SCXMLExecutor;
import org.apache.scxml.SCXMLExpressionException;
import org.apache.scxml.env.AbstractBaseEvaluator;
import static org.apache.faces.impl.state.evaluator.StateFlowEvaluatorProvider.SUPPORTED_DATA_MODEL;
import org.apache.faces.impl.state.utils.Util;
import static org.apache.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import org.apache.scxml.SCXMLIOProcessor;
import org.apache.scxml.SCXMLSystemContext;
import org.apache.scxml.env.EffectiveContextMap;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowEvaluator extends AbstractBaseEvaluator {

    public static final String CURRENT_STACK_KEY = "javax.faces.state.CURRENT_STACK";
    public static final String FLOW_EL_CONTEXT_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();
    public static final String FLOW_ISTANCE_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();

    private final transient ThreadLocal<ContextWrapper> eccashe;

    private transient ContextWrapper ec;
    private transient ExpressionFactory ef;

    public StateFlowEvaluator() {
        super();
        eccashe = new ThreadLocal<>();
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    @Override
    public boolean requiresGlobalContext() {
        return false;
    }

    private <V> V wrap(Context ctx, String expr, Callable<V> call) throws SCXMLExpressionException {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (ef == null) {
            ef = fc.getApplication().getExpressionFactory();
        }

        ec = eccashe.get();
        if (ec == null) {
            ec = new ContextWrapper(fc);
            eccashe.set(ec);
        }

        Map<String, SCXMLIOProcessor> ioProcessors = (Map<String, SCXMLIOProcessor>) ctx.get(SCXMLSystemContext.IOPROCESSORS_KEY);
        String sessionId = (String) ctx.get(SCXMLSystemContext.SESSIONID_KEY);
        if (ioProcessors.containsKey(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId)) {
            SCXMLExecutor executor = (SCXMLExecutor) ioProcessors.get(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId);
            fc.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
            ec.putContext(SCXMLExecutor.class, executor);
        } else {
            fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
        }

        ctx = getEffectiveContext(ctx);

        ec.putContext(Context.class, ctx);
        ec.putContext(FacesContext.class, fc);
        try {
            return call.call();
        } catch (NullPointerException ex) {
            throw new SCXMLExpressionException(String.format("%s error: null pointer exception", expr), ex);
        } catch (Throwable ex) {
            throw new SCXMLExpressionException(String.format("%s error: %s", expr, Util.getErrorMessage(ex)), ex);
        } finally {
            ec.putContext(Context.class, newContext(null));
        }
    }

    private String resolve(Context ctx, String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public void evalAssign(Context ctx, String location, Object data) throws SCXMLExpressionException {
        wrap(ctx, location, () -> {
            if (ELText.isLiteral(location)) {
                ctx.set(location, data);
            } else {
                if (data == null) {
                    ValueExpression ve = ef.createValueExpression(ec, location, Object.class);
                    ve.setValue(ec, null);
                } else {
                    ValueExpression ve = ef.createValueExpression(ec, location, data.getClass());
                    ve.setValue(ec, data);
                }
            }
            return null;
        });

    }

    @Override
    public Object eval(Context ctx, String expr) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, expr), Object.class);
            return ve.getValue(ec);
        });
    }

    @Override
    public Boolean evalCond(Context ctx, String expr) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, expr), Boolean.class);
            return (Boolean) ve.getValue(ec);
        });
    }

    @Override
    public Object evalMethod(Context ctx, String expr, Class[] pclass, Object[] param) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            MethodExpression me = ef.createMethodExpression(ec, resolve(ctx, expr), Object.class, pclass);
            return me.invoke(ec, param);
        });
    }
    
    @Override
    public Object evalScript(Context ctx, String script) throws SCXMLExpressionException {
        return wrap(ctx, script, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, script), Object.class);
            return ve.getValue(ec);
        });
    }

    @Override
    public Context newContext(Context parent) {
        return new StateFlowContext(parent);
    }

    protected StateFlowContext getEffectiveContext(final Context nodeCtx) {
        return new StateFlowContext(nodeCtx, new EffectiveContextMap(nodeCtx));
    }

    public class ContextWrapper extends ELContext implements Serializable {

        private final ELContext ctx;
        private VariableMapper varMapper;
        private final FunctionMapper fnMapper;
        private final CompositeELResolver elResolver;

        private ContextWrapper(FacesContext facesContext) {
            super();
            this.ctx = facesContext.getELContext();

            this.varMapper = ctx.getVariableMapper();
            if (varMapper == null) {
                this.varMapper = new EvaluatorVariableMapper();
            }

            this.fnMapper = new CompositeFunctionMapper(new EvaluatorBuiltinFunctionMapper(), ctx.getFunctionMapper());

            this.elResolver = new CompositeELResolver();
            this.elResolver.add(new EvaluatorELResolver());
            this.elResolver.add(ctx.getELResolver());

        }

        @Override
        public FunctionMapper getFunctionMapper() {
            return this.fnMapper;
        }

        @Override
        public VariableMapper getVariableMapper() {
            return this.varMapper;
        }

        @Override
        public ELResolver getELResolver() {
            return elResolver;
        }

    }

    static class BuiltinVariableMapper extends VariableMapper implements Serializable {

        private final VariableMapper mapper;

        public BuiltinVariableMapper(VariableMapper mapper) {
            super();
            this.mapper = mapper;
        }

        @Override
        public ValueExpression resolveVariable(String variable) {
            return mapper.resolveVariable(variable);
        }

        @Override
        public ValueExpression setVariable(String variable, ValueExpression expression) {
            return mapper.setVariable(variable, expression);
        }
    }

}
