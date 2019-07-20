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

import com.sun.faces.facelets.el.ELText;
import java.util.Map;
import java.util.concurrent.Callable;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import org.ssoft.faces.impl.state.StateFlowContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.env.AbstractBaseEvaluator;
import org.ssoft.faces.impl.state.utils.Util;
import static javax.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.scxml.env.EffectiveContextMap;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.SCXML_DATA_MODEL;
import javax.faces.state.scxml.model.SCXML;
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

        Context.setCurrentInstance(ctx);

        if (ef == null) {
            ef = fc.getApplication().getExpressionFactory();
        }

        ec = eccashe.get();
        if (ec == null) {
            ec = new StateFlowELContext(fc);
            eccashe.set(ec);
        }

        try {
            Map<String, SCXMLIOProcessor> ioProcessors = (Map<String, SCXMLIOProcessor>) ctx.get(SCXMLSystemContext.IOPROCESSORS_KEY);
            String sessionId = (String) ctx.get(SCXMLSystemContext.SESSIONID_KEY);
            if (ioProcessors.containsKey(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId)) {
                SCXMLExecutor executor = (SCXMLExecutor) ioProcessors.get(SCXMLIOProcessor.SCXML_SESSION_EVENT_PROCESSOR_PREFIX + sessionId);
                fc.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                ec.putContext(SCXMLExecutor.class, executor);

                SCXML scxml = executor.getStateMachine();
                ec.addFunctionMaper(new EvaluatorBuiltinFunctionMapper(ec, scxml));

                Map<String, String> namespaces = scxml.getNamespaces();
                String modelName = scxml.getDatamodelName();

            } else {
                fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
            }

            ctx = getEffectiveContext(ctx);

            ec.putContext(Context.class, ctx);
            ec.putContext(FacesContext.class, fc);
            return call.call();
        } catch (NullPointerException ex) {
            throw new SCXMLExpressionException(String.format("%s error: null pointer exception", expr.toString()), ex);
        } catch (Throwable ex) {
            throw new SCXMLExpressionException(String.format("%s error: %s", expr.toString(), Util.getErrorMessage(ex)), ex);
        } finally {
            ec.reset();
            Context.setCurrentInstance(null);
            fc.getAttributes().remove(CURRENT_EXECUTOR_HINT);
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