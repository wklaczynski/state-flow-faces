/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import javax.faces.state.NamespacePrefixesHolder;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.el.FunctionMapper;
import javax.faces.state.FunctionMapperHolder;
import javax.faces.state.utils.StateFlowHelper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChart implements Serializable, NamespacePrefixesHolder, FunctionMapperHolder {

    public static final String STATECHART_FACET_NAME = "javax_stateflow_metadata";
    
    
    /**
     * The initial TransitionTarget for the executor.
     */
    private TransitionTarget initialTarget;

    /**
     * Optional property holding the data model for this document.
     * This gets merged with the root context and potentially hides any
     * (namesake) variables in the root context.
     */
    private Datamodel datamodel;
    
    /**
     * The initial transition target ID (used by XML Digester only).
     */
    private String initial;
    
    
    /**
     * The immediate child targets of this document root.
     */
    private final Map<String, TransitionTarget> children;

    /**
     * A global map of all States and Parallels associated with this
     * state machine, keyed by their id.
     */
    private final Map<String, TransitionTarget> targets;
    
    /**
     * The XML namespaces defined on the document root node,
     * preserved primarily for serialization.
     */
    private Map namespaces;
    
    
    /**
     * The current FunctionMapper in the flow chart document for this action node,
     */
    private FunctionMapper functionMapper;
    
    
    /**
     * Constructor.
     */
    public StateChart() {
        this.children = new LinkedHashMap();
        this.targets = new HashMap();
    }

    /**
     * Get the initial TransitionTarget.
     *
     * @return Returns the initial target for this state machine.
     */
    public final TransitionTarget getInitialTarget() {
        return initialTarget;
    }

    /**
     * Set the initial TransitionTarget.
     *
     * @param initialTarget The initial target to set.
     */
    public final void setInitialTarget(final TransitionTarget initialTarget) {
        this.initialTarget = initialTarget;
    }
    

    /**
     * Get the immediate child targets of the root.
     *
     * @return Map Returns map of the child targets.
     */
    public final Map<String, TransitionTarget> getChildren() {
        return children;
    }

    /**
     * Get the data model placed at document root.
     *
     * @return Returns the data model.
     */
    public final Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model at document root.
     *
     * @param datamodel The Datamodel to set.
     */
    public final void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Add an immediate child target of the root.
     *
     * @param tt The transition target to be added to the states Map.
     */
    public final void addChild(final TransitionTarget tt) {
        children.put(tt.getId(), tt);
    }

    /**
     * Get the targets map, which is a Map of all States and Parallels
     * associated with this state machine, keyed by their id.
     *
     * @return Map Returns the targets.
     */
    public final Map<String, TransitionTarget> getTargets() {
        return targets;
    }

    /**
     * Add a target to this document.
     *
     * @param target The target to be added to the targets Map.
     */
    public final void addTarget(final TransitionTarget target) {
        String id = target.getId();
        if (!StateFlowHelper.isStringEmpty(id)) {
            // Target is not anonymous, so makes sense to map it
            targets.put(id, target);
        }
    }


    /**
     * Get the ID of the initial transition target.
     *
     * @return String Returns the initial transition target ID
     *     (used by XML Digester only).
     * @see #getInitialTarget()
     */
    public final String getInitial() {
        return initial;
    }

    /**
     * Set the ID of the initial transition target.
     *
     * @param initial The initial transition target ID
     *     (used by XML Digester only).
     * @see #setInitialTarget(TransitionTarget)
     */
    public final void setInitial(final String initial) {
        this.initial = initial;
    }
    
    /**
     * Get the namespace definitions specified on the element.
     * May be <code>null</code>.
     *
     * @return The namespace definitions specified on the element,
     *         may be <code>null</code>.
     */
    @Override
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the namespace definitions specified on the SCXML element.
     *
     * @param namespaces The namespace definitions specified on the
     *                   element.
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
    
    
}
