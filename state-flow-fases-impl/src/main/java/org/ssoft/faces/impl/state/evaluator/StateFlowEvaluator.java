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
package org.ssoft.faces.impl.state.evaluator;

import java.util.Map;
import java.util.concurrent.Callable;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static javax.faces.state.StateFlow.DISABLE_EXPRESSION_MAP;
import org.ssoft.faces.impl.state.StateFlowContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.env.AbstractBaseEvaluator;
import org.ssoft.faces.impl.state.utils.Util;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.scxml.env.EffectiveContextMap;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.SCXML_DATA_MODEL;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.state.execute.ExecuteContextManager;
import org.ssoft.faces.impl.state.invokers.FacesInvokerWrapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowEvaluator extends AbstractBaseEvaluator {

    /**
     *
     */
    public static final String CURRENT_STACK_KEY = "javax.faces.state.CURRENT_STACK";

    /**
     *
     */
    public static final String FLOW_EL_CONTEXT_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();

    /**
     *
     */
    public static final String FLOW_ISTANCE_KEY = "javax.faces.FLOW_CONTEXT_KEY".intern();

    private final transient ThreadLocal<StateFlowELContext> eccashe;

    private transient StateFlowELContext ec;
    private transient ExpressionFactory ef;

    /**
     *
     */
    public StateFlowEvaluator() {
        super();
        eccashe = new ThreadLocal<>();
    }

    @Override
    public String getSupportedDatamodel() {
        return SCXML_DATA_MODEL;
    }

    @Override
    public boolean requiresGlobalContext() {
        return false;
    }

    private <V, T> V wrap(Context ctx, T expr, Callable<V> call) throws SCXMLExpressionException {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (ef == null) {
            ef = fc.getApplication().getExpressionFactory();
        }

        ec = eccashe.get();
        if (ec == null) {
            ec = new StateFlowELContext(fc);
            eccashe.set(ec);
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        boolean pushed = false;

        try {
            Map<String, SCXMLIOProcessor> ioProcessors = (Map<String, SCXMLIOProcessor>) ctx.get(SCXMLSystemContext.IOPROCESSORS_KEY);
            String sessionId = (String) ctx.get(SCXMLSystemContext.SESSIONID_KEY);
            if (ioProcessors.containsKey(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId)) {
                SCXMLExecutor executor = (SCXMLExecutor) ioProcessors.get(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId);
                fc.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                ec.putContext(SCXMLExecutor.class, executor);

                ExecuteContext viewContext = new ExecuteContext(null, executor, ctx);
                pushed = manager.push(viewContext);

                ec.putContext(SCXMLExecutor.class, executor);
                SCXML scxml = executor.getStateMachine();
                ec.addFunctionMaper(new EvaluatorBuiltinFunctionMapper(ec, scxml));
            }

            ctx = getEffectiveContext(ctx);

            fc.getAttributes().put(DISABLE_EXPRESSION_MAP, true);
            ec.putContext(Context.class, ctx);
            ec.putContext(FacesContext.class, fc);
            return call.call();
        } catch (NullPointerException ex) {
            throw new SCXMLExpressionException(String.format("%s error: null pointer exception", expr.toString()), ex);
        } catch (Throwable ex) {
            throw new SCXMLExpressionException(String.format("%s error: %s", expr.toString(), Util.getErrorMessage(ex)), ex);
        } finally {
            ec.reset();
            fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            fc.getAttributes().remove(DISABLE_EXPRESSION_MAP);
            if (pushed) {
                manager.pop();
            }
        }
    }

    private String resolve(Context ctx, String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public void evalAssign(Context ctx, ValueExpression location, Object data) throws SCXMLExpressionException {
        wrap(ctx, location, () -> {
            if (location.isLiteralText()) {
                String name = (String) location.getValue(ec);
                if (data != null) {
                    ctx.set(name, data);
                } else {
                    ctx.remove(name);
                }
            } else {
                if (data != null) {
                    location.setValue(ec, data);
                } else {
                    location.setValue(ec, data);
                }
            }
            return null;
        });

    }

    @Override
    public ValueExpression setVariable(Context ctx, String variable, ValueExpression expression) throws SCXMLExpressionException {
        return wrap(ctx, expression, () -> {
            if (variable != null) {
                return ec.getVariableMapper().setVariable(variable, expression);
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
    public Object eval(Context ctx, ValueExpression expr) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            ValueExpression ve = expr;
            return ve.getValue(ec);
        });
    }

    @Override
    public Boolean evalCond(Context ctx, ValueExpression expr) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            ValueExpression ve = expr;
            return (Boolean) ve.getValue(ec);
        });
    }

    @Override
    public Object evalMethod(Context ctx, MethodExpression expr, Object[] param) throws SCXMLExpressionException {
        return wrap(ctx, expr, () -> {
            MethodExpression me = expr;
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

    /**
     *
     * @param nodeCtx
     * @return
     */
    protected StateFlowContext getEffectiveContext(final Context nodeCtx) {
        return new StateFlowContext(nodeCtx, new EffectiveContextMap(nodeCtx));
    }

    @Override
    protected Object cloneUnknownDataType(Object data) {
        return data;
    }

    /**
     * Create a new {@link Invoker}
     *
     * @param type The type of the target being invoked.
     * @return An {@link Invoker} for the specified type, if an invoker class is
     * registered against that type, <code>null</code> otherwise.
     * @throws InvokerException When a suitable {@link Invoker} cannot be
     * instantiated.
     */
    @Override
    public Invoker newInvoker(final String type) throws InvokerException {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        Map<String, Class<? extends Invoker>> customInvokers = handler.getCustomInvokers();

        Class<? extends Invoker> invokerClass = customInvokers.get(stripTrailingSlash(type));
        if (invokerClass == null) {
            throw new InvokerException("No Invoker registered for type \"" + stripTrailingSlash(type) + "\"");
        }
        try {
            Invoker invoker = invokerClass.newInstance();
            FacesInvokerWrapper wrapper = new FacesInvokerWrapper(invoker);
            return wrapper;
        } catch (InstantiationException | IllegalAccessException ie) {
            throw new InvokerException(ie.getMessage(), ie.getCause());
        }
    }

    private String stripTrailingSlash(final String uri) {
        return uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
    }

}
