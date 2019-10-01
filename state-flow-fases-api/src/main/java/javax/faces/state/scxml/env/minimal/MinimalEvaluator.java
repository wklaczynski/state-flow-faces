/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.faces.state.scxml.env.minimal;

import java.io.Serializable;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.EvaluatorProvider;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.model.SCXML;


/**
 * Minimal Evaluator implementing and providing support for the SCXML Null Data Model.
 * <p>
 * The SCXML Null Data Model only supports the SCXML "In(stateId)" builtin function.
 * </p>
 */
public class MinimalEvaluator implements Evaluator, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public static final String SUPPORTED_DATA_MODEL = Evaluator.NULL_DATA_MODEL;

    @Override
    public ValueExpression setVariable(Context ctx, String variable, ValueExpression expression) throws SCXMLExpressionException {
        return null;
    }

    /**
     *
     */
    public static class MinimalEvaluatorProvider implements EvaluatorProvider {

        @Override
        public String getSupportedDatamodel() {
            return SUPPORTED_DATA_MODEL;
        }

        @Override
        public Evaluator getEvaluator() {
            return new MinimalEvaluator();
        }

        @Override
        public Evaluator getEvaluator(final SCXML document) {
            return new MinimalEvaluator();
        }
    }

    @Override
    public String getSupportedDatamodel() {
        return SUPPORTED_DATA_MODEL;
    }

    @Override
    public boolean requiresGlobalContext() {
        return true;
    }

    @Override
    public Object cloneData(final Object data) {
        return data;
    }

    @Override
    public Object eval(final Context ctx, final String expr) throws SCXMLExpressionException {
        return expr;
    }

    @Override
    public Object eval(Context ctx, ValueExpression expr) throws SCXMLExpressionException {
        throw null;
    }
    
    @Override
    public Boolean evalCond(final Context ctx, final ValueExpression expr) throws SCXMLExpressionException {
        // only support the "In(stateId)" predicate
//        String predicate = expr != null ? expr..trim() : "";
//        if (predicate.startsWith("In(") && predicate.endsWith(")")) {
//            String stateId = predicate.substring(3, predicate.length()-1);
//            return Builtin.isMember(ctx, stateId);
//        }
        return false;
    }

    @Override
    public Object evalMethod(Context ctx, MethodExpression expr, Object[] param) throws SCXMLExpressionException {
        return null;
    }

    @Override
    public void evalAssign(final Context ctx, final ValueExpression location, final Object data) throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Assign expressions are not supported by the \"null\" datamodel");
    }

    @Override
    public Object evalScript(final Context ctx, final String script) throws SCXMLExpressionException {
        throw new UnsupportedOperationException("Scripts are not supported by the \"null\" datamodel");
    }

    @Override
    public Context newContext(final Context parent) {
        return parent instanceof MinimalContext ? parent : new MinimalContext(parent);
    }

    @Override
    public Invoker newInvoker(String type) throws InvokerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}