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
package javax.faces.state.scxml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.state.scxml.PathResolver;
import static javax.faces.state.scxml.SCXMLConstants.META_ELEMENT_IDMAP;

/**
 * The class in this SCXML object model that corresponds to the &lt;scxml&gt;
 * root element, and serves as the &quot;document root&quot;.
 *
 */
public class SCXML extends SCComponent implements Serializable, Observable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Reserved prefix for auto generated TransitionTarget id values
     */
    public static final String GENERATED_TT_ID_PREFIX = "_generated_tt_id_";

    /**
     * The predefined observableId with value 0 (zero) for this SCXML state
     * machine
     */
    private static final Integer SCXML_OBSERVABLE_ID = 0;

    /**
     * The SCXML version of this document.
     */
    private String version;

    /**
     * The initial Transition for the SCXML executor.
     */
    private SimpleTransition initialTransition;

    /**
     * The initial transition target ID
     */
    private String initial;

    /**
     * The name for this state machine.
     */
    private String name;

    /**
     * The viewId for this state machine.
     */
    private String viewId;

    /**
     * The profile in use.
     */
    private String profile;

    /**
     * The exmode for this document.
     */
    private String exmode;

    /**
     * optional flag indicating if this document uses late or early (default)
     * binding
     */
    private Boolean lateBinding;

    /**
     * The datamodel name as specified as "datamodel" attribute on this document
     */
    private String datamodelName;

    /**
     * Optional property holding the data model for this SCXML document. This
     * gets merged with the root context and potentially hides any (namesake)
     * variables in the root context.
     */
    private Datamodel datamodel;

    /**
     * Optional property holding the initial script for this SCXML document.
     */
    private Script globalScript;

    /**
     * used to resolve SCXML context sensitive paths
     */
    private PathResolver pathResolver;

    /**
     * The immediate child targets of this SCXML document root.
     */
    private final List<EnterableState> children;

    /**
     * A global map of all States and Parallels associated with this state
     * machine, keyed by their id.
     */
    private final Map<String, TransitionTarget> targets;

    /**
     * The XML namespaces defined on the SCXML document root node, preserved
     * primarily for serialization.
     */
    private Map<String, String> namespaces;

    /**
     * The next auto-generated transition target unique id value
     *
     * @see #generateTransitionTargetId()
     */
    private long ttNextId;

    private Map<String, Object> metadata;

    /**
     * Constructor.
     */
    public SCXML() {
        this.children = new ArrayList<>();
        this.targets = new HashMap<>();
        this.metadata = new HashMap<>();
        this.metadata.put(META_ELEMENT_IDMAP, new HashMap<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Integer getObservableId() {
        return SCXML_OBSERVABLE_ID;
    }

    /**
     * Simple unique TransitionTarget id value generation
     *
     * @return a unique TransitionTarget id for this SCXML instance
     */
    public final String generateTransitionTargetId() {
        return GENERATED_TT_ID_PREFIX + ttNextId++;
    }

    /**
     *
     * @return
     */
    public final Script getGlobalScript() {
        return globalScript;
    }

    /**
     *
     * @param script
     */
    public final void setGlobalScript(Script script) {
        this.globalScript = script;
    }

    /**
     * Get the {@link PathResolver}.
     *
     * @return Returns the pathResolver.
     */
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Set the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
    }

    /**
     * Get the initial Transition.
     *
     * @return Returns the initial transition for this state machine.
     *
     * @since 2.0
     */
    public final SimpleTransition getInitialTransition() {
        return initialTransition;
    }

    /**
     * Set the initial Transition.
     * <p>
     * Note: the initial transition can/may not have executable content!</p>
     *
     * @param initialTransition The initial transition to set.
     *
     * @since 2.0
     */
    public final void setInitialTransition(final SimpleTransition initialTransition) {
        this.initialTransition = initialTransition;
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
     *
     * @param lateBinding
     */
    public final void setLateBinding(final Boolean lateBinding) {
        this.lateBinding = lateBinding;
    }

    /**
     *
     * @return
     */
    public final Boolean isLateBinding() {
        return lateBinding;
    }

    /**
     * Get the immediate child targets of the SCXML root.
     *
     * @return List Returns list of the child targets.
     *
     * @since 0.7
     */
    public final List<EnterableState> getChildren() {
        return children;
    }

    /**
     * Get the first immediate child of the SCXML root. Return null if there's
     * no child.
     *
     * @return Returns the first immediate child of the SCXML root. Return null
     * if there's no child.
     *
     * @since 2.0
     */
    public final EnterableState getFirstChild() {
        if (!children.isEmpty()) {
            return children.get(0);
        }
        return null;
    }

    /**
     * Add an immediate child of the SCXML root.
     *
     * @param es The child to be added.
     *
     * @since 0.7
     */
    public final void addChild(final EnterableState es) {
        children.add(es);
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
     * Add a target to this SCXML document.
     *
     * @param target The target to be added to the targets Map.
     */
    public final void addTarget(final TransitionTarget target) {
        targets.put(target.getId(), target);
    }

    /**
     * Get the SCXML document version.
     *
     * @return Returns the version.
     */
    public final String getVersion() {
        return version;
    }

    /**
     * Set the SCXML document version.
     *
     * @param version The version to set.
     */
    public final void setVersion(final String version) {
        this.version = version;
    }

    /**
     * Get the namespace definitions specified on the SCXML element. May be
     * <code>null</code>.
     *
     * @return The namespace definitions specified on the SCXML element, may be
     * <code>null</code>.
     */
    public final Map<String, String> getNamespaces() {
        return namespaces;
    }

    /**
     * Set the namespace definitions specified on the SCXML element.
     *
     * @param namespaces The namespace definitions specified on the SCXML
     * element.
     */
    public final void setNamespaces(final Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the metadata definitions specified on the all SCXML element. May be
     * <code>null</code>.
     *
     * @return The metadata definitions specified on the all SCXML element, may
     * be <code>null</code>.
     */
    public final Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Set the namespace definitions specified on the SCXML element.
     *
     * @param metadata The metadata definitions specified on the SCXML element.
     */
    public final void setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    /**
     * Get the the initial transition target.
     *
     * @return String Returns the initial transition target ID
     * @see #getInitialTransition()
     */
    public final String getInitial() {
        return initial;
    }

    /**
     * Set the initial transition target.
     *
     * @param initial The initial transition target
     * @see #setInitialTransition(SimpleTransition)
     */
    public final void setInitial(final String initial) {
        this.initial = initial;
    }

    /**
     * Get the name for this state machine.
     *
     * @return The name for this state machine.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name for this state machine.
     *
     * @param name The name for this state machine.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the viewId for this state machine.
     *
     * @return The viewId for this state machine.
     */
    public String getViewId() {
        return viewId;
    }

    /**
     * Set the viewId for this state machine.
     *
     * @param viewId The name for this state machine.
     */
    public void setViewId(String viewId) {
        this.viewId = viewId;
    }

    /**
     * Get the profile in use for this state machine.
     *
     * @return The profile in use.
     */
    public String getProfile() {
        return profile;
    }

    /**
     * Set the profile in use for this state machine.
     *
     * @param profile The profile to be used.
     */
    public void setProfile(String profile) {
        this.profile = profile;
    }

    /**
     * Get the exmode in use for this state machine.
     *
     * @return The exmode in use.
     */
    public String getExmode() {
        return exmode;
    }

    /**
     * Set the exmode to be used for this state machine.
     *
     * @param exmode The exmode to be used.
     */
    public void setExmode(String exmode) {
        this.exmode = exmode;
    }

    /**
     * Get the datamodel name as specified as attribute on this document
     *
     * @return The datamodel name of this document
     */
    public String getDatamodelName() {
        return datamodelName;
    }

    /**
     * Sets the datamodel name as specified as attribute on this document
     *
     * @param datamodelName The datamodel name
     */
    public void setDatamodelName(final String datamodelName) {
        this.datamodelName = datamodelName;
    }

    /**
     * Find the element by client id.
     *
     * @param expr elament id expression
     * @return Returns the found element.
     */
    public Object findElement(String expr) {
        if (expr.length() == 0) {
            throw new IllegalArgumentException("\"\"");
        }

        Map idMap = (Map) metadata.get(META_ELEMENT_IDMAP);

        Object result = idMap.get(expr);
        return (result);
    }
    
}
