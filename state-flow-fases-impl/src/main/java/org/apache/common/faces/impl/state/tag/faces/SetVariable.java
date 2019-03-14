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

import java.util.logging.Level;
import org.apache.common.faces.state.scxml.ActionExecutionContext;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.Evaluator;
import org.apache.common.faces.state.scxml.EventBuilder;
import org.apache.common.faces.state.scxml.SCXMLConstants;
import org.apache.common.faces.state.scxml.SCXMLExpressionException;
import org.apache.common.faces.state.scxml.TriggerEvent;
import org.apache.common.faces.state.scxml.model.Action;
import org.apache.common.faces.state.scxml.model.CommonsSCXML;
import org.apache.common.faces.state.scxml.model.CustomAction;
import org.apache.common.faces.state.scxml.model.CustomActionWrapper;
import org.apache.common.faces.state.scxml.model.ModelException;

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
 * <li>wrap the {@link SetVariable} instance in a {@link CustomActionWrapper} (for which
 * the {@link #CUSTOM_ACTION} can be useful) before adding it to the object
 * model</li>
 * </ul>
 * before write the SCXML model with {@link SCXMLWriter}. The writing will fail
 * otherwise!
 * </p>
 */
public class SetVariable extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The name of the variable to be created.
     */
    private String name;

    /**
     * The expression that evaluates to the initial value of the variable.
     */
    private String expr;

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
     * Get the name of the (new) variable.
     *
     * @return String Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name of the (new) variable.
     *
     * @param name The name to set.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = exctx.getContext(getParentEnterableState());
        Evaluator eval = exctx.getEvaluator();
        Object varObj = eval.eval(ctx, expr);
        ctx.setLocal(name, varObj);
        if (exctx.getAppLog().isLoggable(Level.FINE)) {
            exctx.getAppLog().log(Level.FINE, "<var>: Defined variable ''{0}'' with initial value ''{1}''", new Object[]{name, String.valueOf(varObj)});
        }
        TriggerEvent ev = new EventBuilder(name + ".change", TriggerEvent.CHANGE_EVENT).build();
        exctx.getInternalIOProcessor().addEvent(ev);
    }
}
