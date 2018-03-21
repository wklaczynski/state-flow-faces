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

import javax.faces.state.NamespacePrefixesHolder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Transition extends Executable implements NamespacePrefixesHolder {

    /**
     * Property that specifies the trigger for this transition.
     */
    private String event;

    /**
     * Optional guard condition.
     */
    private ValueExpression cond;

    /**
     * Optional guard condition.
     */
    private String type;

    /**
     * Optional property that specifies the new state(s) or parallel
     * element to transition to. May be specified by reference or in-line.
     * If multiple state(s) are specified, they must belong to the regions
     * of the same parallel.
     */
    private final List<TransitionTarget> targets;

    /**
     * The transition target ID (used by XML Digester only).
     */
    private String next;

    /**
     * The path(s) for this transition, one per target, in the same order
     * as <code>targets</code>.
     * @see Path
     */
    private final List<Path> paths;

    /**
     * The current XML namespaces in the document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map namespaces;

    /**
     * Constructor.
     */
    public Transition() {
        super();
        this.targets = new ArrayList();
        this.paths = new ArrayList();
    }

    /**
     * Get the guard condition (may be null).
     *
     * @return Returns the cond.
     */
    public final ValueExpression getCond() {
        return cond;
    }

    /**
     * Set the guard condition.
     *
     * @param cond The cond to set.
     */
    public final void setCond(final ValueExpression cond) {
        this.cond = cond;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    /**
     * Get the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @return Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * Set the event that will trigger this transition (pending
     * evaluation of the guard condition in favor).
     *
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Get the XML namespaces at this action node in the document.
     *
     * @return Returns the map of namespaces.
     */
    @Override
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the document.
     *
     * @param namespaces The document namespaces.
     */
    @Override
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

    /**
     * Get the list of transition targets (may be an empty list).
     *
     * @return Returns the target(s) as specified in SCXML markup.
     * <p>Remarks: Is <code>empty</code> for &quot;stay&quot; transitions.
     * Contains parent (the source node) for &quot;self&quot; transitions.</p>
     */
    public final List<TransitionTarget> getTargets() {
        return targets;
    }

    /**
     * Get the list of runtime transition target, which always contains
     * atleast one TransitionTarget instance.
     *
     * @return Returns the actual targets of a transition at runtime.
     * <p>Remarks: For both the &quot;stay&quot; and &quot;self&quot;
     * transitions it returns parent (the source node). This method should
     * never return an empty list or <code>null</code>.</p>
     */
    public final List<TransitionTarget> getRuntimeTargets() {
        if (targets.isEmpty()) {
            List runtimeTargets = new ArrayList();
            runtimeTargets.add(getParent());
            return runtimeTargets;
        }
        return targets;
    }

    /**
     * Get the ID of the transition target (may be null, if, for example,
     * the target is specified inline).
     *
     * @return String Returns the transition target ID
     *                (used by Digester only).
     * @see #getTargets()
     */
    public final String getNext() {
        return next;
    }

    /**
     * Set the transition target by specifying its ID.
     *
     * @param next The the transition target ID (used by Digester only).
     */
    public final void setNext(final String next) {
        this.next = next;
    }

    /**
     * Get the path(s) of this transiton.
     *
     * @see Path
     * @return List returns the list of transition path(s)
     */
    public final List<Path> getPaths() {
        if (paths.isEmpty()) {
            if (targets.size() > 0) {
                for (int i = 0; i < targets.size(); i++) {
                    paths.add(i, new Path(getParent(),
                        (TransitionTarget) targets.get(i)));
                }
            } else {
                paths.add(new Path(getParent(), null));
            }
        }
        return paths;
    }

    @Override
    public String toString() {
        return "Transition{" + "event=" + event + ", cond=" + cond + ", type=" + type + ", next=" + next + '}';
    }
    
    
    
}
