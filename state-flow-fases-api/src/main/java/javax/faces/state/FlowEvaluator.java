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
package javax.faces.state;

import javax.faces.state.model.TransitionTarget;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FlowEvaluator {

    /**
     * Evaluate an expression.
     *
     * @param ctx variable context
     * @param expr expression
     * @return a result of the evaluation
     * @throws FlowExpressionException A malformed exception
     */
    Object eval(FlowContext ctx, String expr) throws FlowExpressionException;

    /**
     * Evaluate a condition. Manifests as "cond" attributes of
     * &lt;transition&gt;, &lt;if&gt; and &lt;elseif&gt; elements.
     *
     * @param ctx variable context
     * @param expr expression
     * @return true/false
     * @throws FlowExpressionException A malformed exception
     */
    Boolean evalCond(FlowContext ctx, String expr) throws FlowExpressionException;

    /**
     * Evaluate a location that returns a Node within an XML data tree.
     * Manifests as "location" attributes of &lt;assign&gt; element.
     *
     * @param ctx variable context
     * @param expr expression
     * @return The location node.
     * @throws FlowExpressionException A malformed exception
     */
    Node evalLocation(FlowContext ctx, String expr) throws FlowExpressionException;

    /**
     * Create a new child context.
     *
     * @param target
     * @param parent parent context
     * @return new child context
     */
    FlowContext newContext(TransitionTarget target, FlowContext parent);

    
}
