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
package org.apache.common.faces.impl.state.tag.faces;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.common.faces.impl.state.utils.Util;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.SCXMLConstants;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.model.Action;
import javax.faces.state.scxml.model.CommonsSCXML;
import javax.faces.state.scxml.model.CustomAction;
import javax.faces.state.scxml.model.CustomActionWrapper;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.Param;
import javax.faces.state.scxml.model.ParamsContainer;

/**
 * The class in this SCXML object model that corresponds to the
 * {@link CustomAction} &lt;var&gt; SCXML element.
 * <p>
 * When manually constructing or modifying a SCXML model using this custom
 * action, either:
 * <ul>
 * <li>derive from {@link CommonsSCXML}, or</li>
 * <li>make sure to add the {@link SCXMLConstants#XMLNS_COMMONS_SCXML} namespace
 * with the {@link SCXMLConstants#XMLNS_COMMONS_SCXML_PREFIX} prefix to the
 * SCXML object, or</li>
 * <li>wrap the {@link Var} instance in a {@link CustomActionWrapper} (for which
 * the {@link #CUSTOM_ACTION} can be useful) before adding it to the object
 * model</li>
 * </ul>
 * before write the SCXML model with {@link SCXMLWriter}. The writing will fail
 * otherwise!
 * </p>
 */
public class MethodCall extends Action implements ParamsContainer {

    private static final Map<String, String> classmap = new ConcurrentHashMap<>();
    
    static{
        classmap.put("string", String.class.getName());
        classmap.put("boolean", Boolean.class.getName());
        classmap.put("int", Integer.class.getName());
        classmap.put("long", Long.class.getName());
        classmap.put("float", Float.class.getName());
        classmap.put("double", Double.class.getName());
        classmap.put("currency", Currency.class.getName());
        classmap.put("date", Date.class.getName());
    }
    
    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The expression that evaluates to the initial value of the variable.
     */
    private String expr;

    /**
     * The List of the params getString be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * Get the expression that evaluates to the initial value of the variable.
     *
     * @return String Returns the expr.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression that evaluates to the initial value of the variable.
     *
     * @param expr The expr to set.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    @Override
    public List<Param> getParams() {
        return paramsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = exctx.getContext(getParentEnterableState());
        Evaluator eval = exctx.getEvaluator();
        ClassLoader loader = Util.getCurrentLoader(this);
        try {
            Class[] classes = new Class[paramsList.size()];
            Object[] objects = new Object[paramsList.size()];
            for (int i = 0; i < paramsList.size(); i++) {
                Param param = paramsList.get(i);
                String location = param.getLocation();
                if(classmap.containsKey(location)) {
                    location = classmap.get(location);
                }
                classes[i] = loader.loadClass(location);
                objects[i] = eval.eval(ctx, param.getExpr());
            }

            eval.evalMethod(ctx, expr, classes, objects);
        } catch (SCXMLExpressionException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new ModelException(ex);
        }
    }
}
