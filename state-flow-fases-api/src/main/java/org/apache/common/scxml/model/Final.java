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
package org.apache.common.scxml.model;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.Evaluator;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.SCXMLExecutionContext;
import org.apache.common.scxml.SCXMLExpressionException;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.semantics.ErrorConstants;

/**
 * The class in this SCXML object model that corresponds to the &lt;final&gt;
 * SCXML element.
 *
 * @since 0.7
 */
public class Final extends EnterableState {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private DoneData doneData;

    /**
     * Default no-args constructor.
     */
    public Final() {
        super();
    }

    /**
     * @return Returns the State parent
     */
    @Override
    public State getParent() {
        return (State) super.getParent();
    }

    /**
     * Set the parent State.
     *
     * @param parent The parent state to set
     */
    public final void setParent(State parent) {
        super.setParent(parent);
    }

    /**
     * {@inheritDoc}
     *
     * @return Returns always true (a state of type Final is always atomic)
     */
    @Override
    public final boolean isAtomicState() {
        return true;
    }

    public DoneData getDoneData() {
        return doneData;
    }

    public void setDoneData(final DoneData doneData) {
        this.doneData = doneData;
    }
    
    public Object processDoneData(SCXMLExecutionContext exctx) throws ModelException {
        Object result = null;
        if (doneData != null) {
            try {
                Content content = doneData.getContent();
                Evaluator eval = exctx.getEvaluator();
                Context ctx = exctx.getScInstance().getGlobalContext();
                if (content != null) {
                    if (content.getExpr() != null) {
                        Object evalResult;
                        try {
                            evalResult = eval.eval(ctx, content.getExpr());
                        } catch (SCXMLExpressionException e) {
                            exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION,
                                    TriggerEvent.ERROR_EVENT).build());
                            exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR,
                                    "Failed to evaluate <donedata> <content> expression due to error: " + e.getMessage()
                                    + ", Using empty value instead.", this, "expr", e);
                            evalResult = "";
                        }
                        result = eval.cloneData(evalResult);
                    } else if (content.getParsedValue() != null) {
                        result = eval.cloneData(content.getParsedValue().getValue());
                    }
                } else {
                    Map<String, Object> payloadDataMap = new LinkedHashMap<>();
                    PayloadBuilder.addParamsToPayload(exctx.getScInstance().getGlobalContext(),
                            exctx.getEvaluator(), doneData.getParams(), payloadDataMap);
                    if (!payloadDataMap.isEmpty()) {
                        result = payloadDataMap;
                    }
                }
            } catch (Throwable e) {
                result = null;
                exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
                exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR,
                        "Failed to process final donedata due to error: " + e.getMessage(), this, "expr", e);
            }
        }
        return result;
    }
}
