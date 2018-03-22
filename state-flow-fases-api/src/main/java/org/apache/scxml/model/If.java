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
package org.apache.scxml.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.scxml.ActionExecutionContext;
import org.apache.scxml.Context;
import org.apache.scxml.Evaluator;
import org.apache.scxml.EventBuilder;
import org.apache.scxml.SCXMLExpressionException;
import org.apache.scxml.TriggerEvent;
import org.apache.scxml.semantics.ErrorConstants;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;if&gt; SCXML element, which serves as a container for conditionally
 * executed elements. &lt;else&gt; and &lt;elseif&gt; can optionally
 * appear within an &lt;if&gt; as immediate children, and serve to partition
 * the elements within an &lt;if&gt;.
 *
 */
public class If extends Action implements ActionsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An conditional expression which can be evaluated to true or false.
     */
    private String cond;

    /**
     * The set of executable elements (those that inheriting from
     * Action) that are contained in this &lt;if&gt; element.
     */
    private final List<Action> actions;

    /**
     * The boolean value that dictates whether the particular child action
     * should be executed.
     */
    private boolean execute;

    /**
     * Constructor.
     */
    public If() {
        super();
        this.actions = new ArrayList<>();
        this.execute = false;
    }

    /**
     * Get the executable actions contained in this &lt;if&gt;.
     *
     * @return Returns the actions.
     */
    public final List<Action> getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in
     * this &lt;if&gt;.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * Get the conditional expression.
     *
     * @return Returns the cond.
     */
    public final String getCond() {
        return cond;
    }

    /**
     * Set the conditional expression.
     *
     * @param cond The cond to set.
     */
    public final void setCond(final String cond) {
        this.cond = cond;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        Evaluator eval = exctx.getEvaluator();
        Boolean rslt;
        try {
            rslt = eval.evalCond(ctx, cond);
            if (rslt == null) {
                if (exctx.getAppLog().isLoggable(Level.FINE)) {
                    exctx.getAppLog().log(Level.FINE, "Treating as false because the cond expression was evaluated as null: ''{0}''", cond);
                }
                rslt = Boolean.FALSE;
            }
        } catch (SCXMLExpressionException e) {
            rslt = Boolean.FALSE;
            exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
            exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR, "Treating as false due to error: "
                    + e.getMessage(), this, "cond", e);
        }
        execute = rslt;
        // The "if" statement is a "container"
        for (Action aa : actions) {
            if (execute && !(aa instanceof ElseIf)) {
                aa.execute(exctx);
            } else if (execute && aa instanceof ElseIf) {
                break;
            } else if (aa instanceof Else) {
                execute = true;
            } else if (aa instanceof ElseIf) {
                execute = eval.evalCond(ctx, ((ElseIf) aa).getCond());
            }
        }
    }

}

