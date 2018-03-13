/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
public class If extends Action {

    /**
     * An conditional expression which can be evaluated to true or false.
     */
    private String cond;

    /**
     * The set of executable elements (those that inheriting from Action) that
     * are contained in this &lt;if&gt; element.
     */
    private final List actions;

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
        this.actions = new ArrayList();
        this.execute = false;
    }

    /**
     * Get the executable actions contained in this &lt;if&gt;.
     *
     * @return Returns the actions.
     */
    public final List getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in this
     * &lt;if&gt;.
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
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {
        TransitionTarget parentTarget = getParentTransitionTarget();
        FlowContext ctx = scInstance.getContext(parentTarget);
        FlowEvaluator eval = scInstance.getEvaluator();
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        execute = eval.evalCond(ctx, cond);
        ctx.setLocal(getNamespacesKey(), null);
        // The "if" statement is a "container"
        for (Iterator ifiter = actions.iterator(); ifiter.hasNext();) {
            Action aa = (Action) ifiter.next();
            if (execute && !(aa instanceof ElseIf) && !(aa instanceof Else)) {
                aa.execute(evtDispatcher, errRep, scInstance, derivedEvents);
            } else if (execute
                    && (aa instanceof ElseIf || aa instanceof Else)) {
                break;
            } else if (aa instanceof Else) {
                execute = true;
            } else if (aa instanceof ElseIf) {
                ctx.setLocal(getNamespacesKey(), getNamespaces());
                execute = eval.evalCond(ctx, ((ElseIf) aa).getCond());
                ctx.setLocal(getNamespacesKey(), null);
            }
        }
    }

}
