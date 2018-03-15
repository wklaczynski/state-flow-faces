/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import javax.faces.state.NamespacePrefixesHolder;
import javax.faces.state.ModelException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Logger;
import javax.el.FunctionMapper;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.FunctionMapperHolder;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class Action implements Serializable, NamespacePrefixesHolder, FunctionMapperHolder {

    protected static final Logger log = Logger.getLogger("javax.faces.state");

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";
    
    /**
     * Link to its parent or container.
     */
    private Executable parent;

    /**
     * The current XML namespaces in the flow chart document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map namespaces;
    
    /**
     * The current FunctionMapper in the flow chart document for this action node,
     */
    private FunctionMapper functionMapper;
    
    /**
     * Constructor.
     */
    public Action() {
        super();
        this.parent = null;
        this.namespaces = null;
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public final Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    @Override
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    @Override
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return functionMapper;
    }
    
    @Override
    public void setFunctionMapper(FunctionMapper functionMapper) {
        this.functionMapper = functionMapper;
    }
    

    /**
     * Return the {@link TransitionTarget} whose {@link Context} this action
     * executes in.
     *
     * @return The parent {@link TransitionTarget}
     * @throws ModelException For an unknown TransitionTarget subclass
     */
    public final TransitionTarget getParentTransitionTarget() throws ModelException {
        TransitionTarget tt = parent.getParent();
        if (tt instanceof State) {
            return tt;
        } else if (tt instanceof Initial) {
            return (TransitionTarget) tt.getParent();
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + tt.getClass().getName());
        }
    }

    /**
     * Execute this action instance.
     *
     * @param evtDispatcher The EventDispatcher for this execution instance
     * @param errRep        The ErrorReporter to broadcast any errors
     *                      during execution.
     * @param instance    The state machine execution instance information.
     * @param derivedEvents The collection to which any internal events
     *                      arising from the execution of this action
     *                      must be added.
     *
     * @throws ModelException If the execution causes the model to enter
     *                        a non-deterministic state.
     * @throws FlowExpressionException If the execution involves trying
     *                        to evaluate an expression which is malformed.
     */
    public abstract void execute(final FlowEventDispatcher evtDispatcher,
        final FlowErrorReporter errRep, final FlowInstance instance,
        final Collection<FlowTriggerEvent> derivedEvents)
    throws ModelException, FlowExpressionException;

    /**
     * Return the key under which the current document namespaces are saved in
     * the parent state's context.
     *
     * @return The namespaces key
     */
    protected static String getNamespacesKey() {
        return NAMESPACES_KEY;
    }

}
