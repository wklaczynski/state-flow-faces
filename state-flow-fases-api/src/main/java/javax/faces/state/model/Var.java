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
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Var extends Action {

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
     * Constructor.
     */
    public Var() {
        super();
    }

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
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {
        FlowContext ctx = scInstance.getContext(getParentTransitionTarget());
        FlowEvaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        Object varObj = eval.eval(ctx, expr);
        ctx.setLocal(getNamespacesKey(), null);
        ctx.setLocal(name, varObj);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "<var>: Defined variable ''{0}'' with initial value ''{1}''", new Object[]{name, String.valueOf(varObj)});
        }
        FlowTriggerEvent ev = new FlowTriggerEvent(name + ".change", FlowTriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);
    }

}
