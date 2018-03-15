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
package javax.faces.state.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class TransitionTarget implements Serializable {

    /**
     * Identifier for this transition target. Other parts of the SCXML document
     * may refer to this &lt;state&gt; using this ID.
     */
    private String id;

    /**
     * Optional property holding executable content to be run upon entering this
     * transition target.
     */
    private OnEntry onEntry;

    /**
     * Optional property holding executable content to be run upon exiting this
     * transition target.
     */
    private OnExit onExit;

    /**
     * Optional property holding the data model for this transition target.
     */
    private Datamodel datamodel;

    /**
     * The parent of this transition target (may be null, if the parent is the
     * document root).
     */
    private TransitionTarget parent;

    /**
     * A list of outgoing Transitions from this target, by document order.
     */
    private final List<Transition> transitions;

    /**
     * <p>
     * The assigned client identifier for this state.</p>
     */
    private String clientId = null;

    /**
     * List of history states owned by a given state (applies to non-leaf
     * states).
     */
    private final List<History> history;

    /**
     * The Map containing immediate children of this State, keyed by
     * their IDs. Incompatible with the parallel or invoke property.
     */
    private final Map<String, TransitionTarget> children;

    public TransitionTarget() {
        this(null);
    }
    
    
    /**
     * Constructor.
     * @param id
     */
    public TransitionTarget(String id) {
        super();
        this.id = id;
        this.parent = null;
        this.transitions = new ArrayList<>();
        this.history = new ArrayList<>();
        this.children = new LinkedHashMap();
    }

    /**
     * Get the identifier for this transition target (may be null).
     *
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the identifier for this transition target.
     *
     * @param id The id to set.
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the onentry property.
     *
     * @return Returns the onEntry.
     */
    public OnEntry getOnEntry() {
        return onEntry;
    }

    /**
     * Set the onentry property.
     *
     * @param onEntry The onEntry to set.
     */
    public void setOnEntry(final OnEntry onEntry) {
        this.onEntry = onEntry;
        this.onEntry.setParent(this);
    }

    /**
     * Get the onexit property.
     *
     * @return Returns the onExit.
     */
    public OnExit getOnExit() {
        return onExit;
    }

    /**
     * Set the onexit property.
     *
     * @param onExit The onExit to set.
     */
    public void setOnExit(final OnExit onExit) {
        this.onExit = onExit;
        this.onExit.setParent(this);
    }

    /**
     * Get the data model for this transition target.
     *
     * @return Returns the data model.
     */
    public Datamodel getDatamodel() {
        return datamodel;
    }

    /**
     * Set the data model for this transition target.
     *
     * @param datamodel The Datamodel to set.
     */
    public void setDatamodel(final Datamodel datamodel) {
        this.datamodel = datamodel;
    }

    /**
     * Get the parent TransitionTarget.
     *
     * @return Returns the parent state (null if parent is &lt;scxml&gt;
     * element)
     */
    public TransitionTarget getParent() {
        return parent;
    }

    /**
     * Set the parent TransitionTarget.
     *
     * @param parent The parent state to set
     */
    public void setParent(final TransitionTarget parent) {
        this.parent = parent;
    }
    
    /**
     * Get the map of child states (may be empty).
     *
     * @return Map Returns the children.
     */
    public Map<String, TransitionTarget> getChildren() {
        return children;
    }

    /**
     * Add a child transition target.
     *
     * @param tt
     *            a child transition target
     */
    public void addChild(final TransitionTarget tt) {
        this.children.put(tt.getId(), tt);
        tt.setParent(this);
    }
    

    /**
     * Get the list of all outgoing transitions from this target, that will be
     * candidates for being fired on the given event.
     *
     * @param event The event
     * @return List Returns the candidate transitions for given event
     */
    public final List<Transition> getTransitionsList(final String event) {
        List matchingTransitions = null; // TODO v1.0 we returned null <= v0.6
        for (int i = 0; i < transitions.size(); i++) {
            Transition t = (Transition) transitions.get(i);
            if ((event == null && t.getEvent() == null)
                    || (event != null && event.equals(t.getEvent()))) {
                if (matchingTransitions == null) {
                    matchingTransitions = new ArrayList();
                }
                matchingTransitions.add(t);
            }
        }
        return matchingTransitions;
    }

    /**
     * Add a transition to the map of all outgoing transitions for this
     * transition target.
     *
     * @param transition The transitions to set.
     */
    public final void addTransition(final Transition transition) {
        transitions.add(transition);
        transition.setParent(this);
    }

    /**
     * Get the outgoing transitions for this target as a java.util.List.
     *
     * @return List Returns the transitions list.
     */
    public final List<Transition> getTransitionsList() {
        return transitions;
    }

    /**
     * This method is used by XML digester.
     *
     * @param h History pseudo state
     */
    public final void addHistory(final History h) {
        history.add(h);
        h.setParent(this);
    }

    /**
     * Does this state have a history pseudo state.
     *
     * @return boolean true if a given state contains at least one history
     * pseudo state
     */
    public final boolean hasHistory() {
        return (!history.isEmpty());
    }

    /**
     * Get the list of history pseudo states for this state.
     *
     * @return a list of all history pseudo states contained by a given state
     * (can be empty)
     * @see #hasHistory()
     */
    public final List<History> getHistory() {
        return history;
    }

    protected String createUniqueId(Object element) {
        if (element instanceof TransitionTarget) {
            TransitionTarget target = (TransitionTarget) element;
            if (!children.containsValue(target)) {
                throw new IllegalArgumentException("Parallel element no constain "
                        + "child element: " + element);
            }

            String result = "state_????";
            int i = 1;
            for (Map.Entry<String, TransitionTarget> entry : children.entrySet()) {
                if (entry.getValue().equals(element)) {
                    result = entry.getKey();
                    break;
                }
            }
            return result;
        } else {
            throw new IllegalArgumentException("Parallel element no support child "
                    + "element type: " + element.getClass().getName());
        }
    }

    public String getClientId() {
        if (this.clientId == null) {
            String parentId = null;

            if (this.parent != null) {
                parentId = this.parent.getClientId();
            }

            this.clientId = getId();
            if (this.clientId == null) {
                String generatedId = createUniqueId(parent);
                setId(generatedId);
                this.clientId = getId();
            }
            if (parentId != null) {
                StringBuilder idBuilder
                        = new StringBuilder(parentId.length() + 
                                1 + this.clientId.length());
                
                this.clientId = idBuilder
                        .append(parentId)
                        .append(":")
                        .append(getId()).toString();
            }
        }
        return clientId;
    }

    public Object findElement(String expr) {
        if (expr == null) {
            throw new NullPointerException();
        }
        if (expr.length() == 0) {
            throw new IllegalArgumentException("\"\"");
        }

        Object result = null;
        for (String cid : children.keySet()) {
            if (cid.equals(expr)) {
                result = children.get(cid);
                break;
            }
            if (expr.startsWith(cid)) {
                result = children.get(cid).findElement(expr);
                break;
            }
        }
        return (result);
    }

}
