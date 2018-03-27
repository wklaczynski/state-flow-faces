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
package org.apache.common.faces.impl.state.utils;

import java.util.Iterator;
import java.util.LinkedList;
import org.apache.common.scxml.model.Transition;
import org.apache.common.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class LogUtils {
    /**
     * Create a human readable log view of this transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that is taken
     * @return String The human readable log entry
     */
    public static String transToString(final TransitionTarget from, final TransitionTarget to, final Transition transition) {
        StringBuilder buf = new StringBuilder("transition (");
        buf.append("event = ").append(transition.getEvent());
        buf.append(", cond = ").append(transition.getCond());
        buf.append(", from = ").append(getTTPath(from));
        buf.append(", to = ").append(getTTPath(to));
        buf.append(')');
        return buf.toString();
    }

    /**
     * Write out this TransitionTarget location in a XPath style format.
     *
     * @param tt The TransitionTarget whose &quot;path&quot; is to needed
     * @return String The XPath style location of the TransitionTarget within
     *                the SCXML document
     */
    public static String getTTPath(final TransitionTarget tt) {
        TransitionTarget parent = tt.getParent();
        if (parent == null) {
            return "/" + tt.getId();
        } else {
            LinkedList pathElements = new LinkedList();
            pathElements.addFirst(tt);
            while (parent != null) {
                pathElements.addFirst(parent);
                parent = parent.getParent();
            }
            StringBuilder names = new StringBuilder();
            for (Iterator i = pathElements.iterator(); i.hasNext();) {
                TransitionTarget pathElement = (TransitionTarget) i.next();
                names.append('/').append(pathElement.getId());
            }
            return names.toString();
        }
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private LogUtils() {
        super();
    }
    
}
