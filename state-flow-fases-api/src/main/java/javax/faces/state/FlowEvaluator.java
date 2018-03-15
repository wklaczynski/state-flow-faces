/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
