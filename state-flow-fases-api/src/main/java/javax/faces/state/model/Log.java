/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.util.Collection;
import java.util.logging.Level;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Log extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An expression evaluating to a string to be logged.
     */
    private String expr;

    /**
     * An expression which returns string which may be used, for example,
     * to indicate the purpose of the log.
     */
    private String label;

    /**
     * Constructor.
     */
    public Log() {
        super();
    }

    /**
     * Get the log expression.
     *
     * @return Returns the expression.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the log expression.
     *
     * @param expr The expr to set.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the log label.
     *
     * @return Returns the label.
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Set the log label.
     *
     * @param label The label to set.
     */
    public final void setLabel(final String label) {
        this.label = label;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
    throws ModelException, FlowExpressionException {
        FlowContext ctx = scInstance.getContext(getParentTransitionTarget());
        FlowEvaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        log.log(Level.INFO, "{0}: {1}", new Object[]{label, String.valueOf(eval.eval(ctx, expr))});
        ctx.setLocal(getNamespacesKey(), null);
    }
}


