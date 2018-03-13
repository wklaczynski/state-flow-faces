/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import javax.faces.state.PathResolverHolder;
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
import javax.faces.state.PathResolver;
import javax.faces.state.semantics.ErrorConstants;
import javax.faces.state.utils.StateFlowHelper;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Assign extends Action implements PathResolverHolder {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Left hand side expression evaluating to a previously defined variable.
     */
    private String name;

    /**
     * Left hand side expression evaluating to a location within a previously
     * defined XML data tree.
     */
    private String location;

    /**
     * The source where the new XML instance for this location exists.
     */
    private String src;

    /**
     * Expression evaluating to the new value of the variable.
     */
    private String expr;

    /**
     * {@link PathResolver} for resolving the "src" result.
     */
    private PathResolver pathResolver;

    /**
     * Constructor.
     */
    public Assign() {
        super();
    }

    /**
     * Get the variable to be assigned a new value.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the variable to be assigned a new value.
     *
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the expr that will evaluate to the new value.
     *
     * @return Returns the expr.
     */
    public String getExpr() {
        return expr;
    }

    /**
     * Set the expr that will evaluate to the new value.
     *
     * @param expr The expr to set.
     */
    public void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the location for a previously defined XML data tree.
     *
     * @return Returns the location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Set the location for a previously defined XML data tree.
     *
     * @param location The location.
     */
    public void setLocation(final String location) {
        this.location = location;
    }

    /**
     * Get the source where the new XML instance for this location exists.
     *
     * @return Returns the source.
     */
    public String getSrc() {
        return src;
    }

    /**
     * Set the source where the new XML instance for this location exists.
     *
     * @param src The source.
     */
    public void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the {@link PathResolver}.
     *
     * @return Returns the pathResolver.
     */
    @Override
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Set the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    @Override
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
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
        // "location" gets preference over "name"
        if (!StateFlowHelper.isStringEmpty(location)) {
            Node oldNode = eval.evalLocation(ctx, location);
            if (oldNode != null) {
                //// rvalue may be ...
                // a Node, if so, import it at location
                Node newNode;
                try {
                    if (src != null && src.trim().length() > 0) {
                        newNode = getSrcNode();
                    } else {
                        newNode = eval.evalLocation(ctx, expr);
                    }
                    // Remove all children
                    Node removeChild = oldNode.getFirstChild();
                    while (removeChild != null) {
                        Node nextChild = removeChild.getNextSibling();
                        oldNode.removeChild(removeChild);
                        removeChild = nextChild;
                    }
                    if (newNode != null) {
                        // Adopt new children
                        for (Node child = newNode.getFirstChild();
                                child != null;
                                child = child.getNextSibling()) {
                            Node importedNode = oldNode.getOwnerDocument().
                                    importNode(child, true);
                            oldNode.appendChild(importedNode);
                        }
                    }
                } catch (FlowExpressionException see) {
                    // or something else, stuff toString() into lvalue
                    Object valueObject = eval.eval(ctx, expr);
                    StateFlowHelper.setNodeValue(oldNode, valueObject.toString());
                }
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE , "<assign>: data node ''{0}'' updated", oldNode.getNodeName());
                }
                FlowTriggerEvent ev = new FlowTriggerEvent(name + ".change", FlowTriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            } else {
                log.log(Level.SEVERE , "<assign>: location does not point to"
                        + " a <data> node");
            }
        } else {
            // lets try "name" (usage as in Sep '05 WD, useful with <var>)
            if (!ctx.has(name)) {
                errRep.onError(ErrorConstants.UNDEFINED_VARIABLE, name  + " = null", parentTarget);
            } else {
                @SuppressWarnings("UnusedAssignment")
                Object varObj = null;
                if (src != null && src.trim().length() > 0) {
                    varObj = getSrcNode();
                } else {
                    varObj = eval.eval(ctx, expr);
                }
                ctx.set(name, varObj);
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE , "<assign>: Set variable ''{0}'' to ''{1}''", new Object[]{name, String.valueOf(varObj)});
                }
                FlowTriggerEvent ev = new FlowTriggerEvent(name + ".change", FlowTriggerEvent.CHANGE_EVENT);
                derivedEvents.add(ev);
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
    }

    /**
     * Get the {@link Node} the "src" attribute points to.
     *
     * @return The node the "src" attribute points to.
     */
    private Node getSrcNode() {
        String resolvedSrc = src;
        if (pathResolver != null) {
            resolvedSrc = pathResolver.resolvePath(src);
        }
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                    parse(resolvedSrc);
        } catch (Throwable t) {
            log.log(Level.SEVERE, t.getMessage(), t);
        }
        if (doc == null) {
            return null;
        }
        return doc.getDocumentElement();
    }

}
