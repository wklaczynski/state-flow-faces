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
package org.apache.faces.impl.state;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.context.FacesContext;
import org.apache.scxml.Context;
import org.apache.scxml.SCXMLExecutor;
import org.apache.scxml.SCXMLExpressionException;
import org.apache.scxml.env.AbstractBaseEvaluator;
import static org.apache.faces.impl.state.StateFlowEvaluatorProvider.SUPPORTED_DATA_MODEL;
import org.apache.faces.impl.state.el.BuiltinFunctionMapper;
import org.apache.faces.impl.state.el.CompositeFunctionMapper;
import org.apache.faces.impl.state.el.DefaultVariableMapper;
import org.apache.faces.impl.state.el.FlowELResolver;
import org.apache.scxml.SCXMLIOProcessor;
import org.apache.scxml.SCXMLSystemContext;

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

    private <V> V wrap(Context ctx, Callable<V> call) throws SCXMLExpressionException {
        FacesContext fc = FacesContext.getCurrentInstance();

        Map<String, SCXMLIOProcessor> ioProcessors = (Map<String, SCXMLIOProcessor>) ctx.get(SCXMLSystemContext.IOPROCESSORS_KEY);
        SCXMLExecutor executor = (SCXMLExecutor) ioProcessors.get(SCXMLIOProcessor.INTERNAL_EVENT_PROCESSOR);

        if (ef == null) {
            ef = fc.getApplication().getExpressionFactory();
        }

        ec = eccashe.get();
        if (ec == null) {
            ec = new ContextWrapper(fc);
            eccashe.set(ec);
        }

        ec.putContext(Context.class, ctx);

        ec.putContext(SCXMLExecutor.class, executor);
        ec.putContext(FacesContext.class, fc);
        try {
            return call.call();
        } catch (Exception ex) {
            throw new SCXMLExpressionException(ex);
        } finally {
            ec.putContext(Context.class, newContext(null));
        }
    }

    private String resolve(Context ctx, String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public Object eval(Context ctx, String expr) throws SCXMLExpressionException {
        return wrap(ctx, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, expr), Object.class);
            return ve.getValue(ec);
        });
    }

    @Override
    public Boolean evalCond(Context ctx, String expr) throws SCXMLExpressionException {
        return wrap(ctx, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, expr), Boolean.class);
            return (Boolean) ve.getValue(ec);
        });
    }

    @Override
    public Object evalScript(Context ctx, String script) throws SCXMLExpressionException {
        return wrap(ctx, () -> {
            ValueExpression ve = ef.createValueExpression(ec, resolve(ctx, script), Object.class);
            return ve.getValue(ec);
        });
    }

    @Override
    public Context newContext(Context parent) {
        return new StateFlowContext(parent);
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
                this.varMapper = new DefaultVariableMapper();
            }

            this.fnMapper = new CompositeFunctionMapper(new BuiltinFunctionMapper(), ctx.getFunctionMapper());

            this.elResolver = new CompositeELResolver();
            this.elResolver.add(new FlowELResolver());
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
