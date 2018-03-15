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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.faces.state.utils.StateFlowHelper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Path  implements Serializable {

    /**
     * The list of TransitionTargets in the &quot;up segment&quot;.
     */
    private final List upSeg = new ArrayList();

    /**
     * The list of TransitionTargets in the &quot;down segment&quot;.
     */
    private final List downSeg = new ArrayList();

    /**
     * &quot;Lowest&quot; transition target which is not being exited nor
     * entered by the transition.
     */
    private TransitionTarget scope = null;

    /**
     * Whether the path crosses region border(s).
     */
    private boolean crossRegion = false;

    /**
     * Constructor.
     *
     * @param source The source TransitionTarget
     * @param target The target TransitionTarget
     */
    Path(final TransitionTarget source, final TransitionTarget target) {
        if (target == null) {
            //a local "stay" transition
            scope = source;
            //all segments remain empty
        } else {
            TransitionTarget tt = StateFlowHelper.getLCA(source, target);
            if (tt != null) {
                scope = tt;
                if (scope == source || scope == target) {
                    scope = scope.getParent();
                }
            }
            tt = source;
            while (tt != scope) {
                upSeg.add(tt);
                if (tt instanceof State) {
                    State st = (State) tt;
                    if (st.isRegion()) {
                        crossRegion = true;
                    }
                }
                tt = tt.getParent();
            }
            tt = target;
            while (tt != scope) {
                downSeg.add(0, tt);
                if (tt instanceof State) {
                    State st = (State) tt;
                    if (st.isRegion()) {
                        crossRegion = true;
                    }
                }
                tt = tt.getParent();
            }
        }
    }

    /**
     * Does this &quot;path&quot; cross regions.
     *
     * @return true when the path crosses a region border(s)
     * @see State#isRegion()
     */
    public final boolean isCrossRegion() {
        return crossRegion;
    }

    /**
     * Get the list of regions exited.
     *
     * @return List a list of exited regions sorted bottom-up;
     *         no order defined for siblings
     * @see State#isRegion()
     */
    public final List getRegionsExited() {
        List ll = new LinkedList();
        for (Iterator i = upSeg.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State st = (State) o;
                if (st.isRegion()) {
                    ll.add(st);
                }
            }
        }
        return ll;
    }

    /**
     * Get the list of regions entered.
     *
     * @return List a list of entered regions sorted top-down; no order
     *         defined for siblings
     * @see State#isRegion()
     */
    public final List getRegionsEntered() {
        List ll = new LinkedList();
        for (Iterator i = downSeg.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof State) {
                State st = (State) o;
                if (st.isRegion()) {
                    ll.add(st);
                }
            }
        }
        return ll;
    }

    /**
     * Get the farthest transition target from root which is not being exited
     * nor entered by the transition (null if scope is document root).
     *
     * @return Scope of the transition path, null means global transition
     *         (document level). Scope is the least transition target
     *         which is not being exited nor entered by the transition.
     */
    public final TransitionTarget getPathScope() {
        return scope;
    }

    /**
     * Get the upward segment.
     *
     * @return List upward segment of the path up to the scope
     */
    public final List getUpwardSegment() {
        return upSeg;
    }

    /**
     * Get the downward segment.
     *
     * @return List downward segment from the scope to the target
     */
    public final List getDownwardSegment() {
        return downSeg;
    }
}
