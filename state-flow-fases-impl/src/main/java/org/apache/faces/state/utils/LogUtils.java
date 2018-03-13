/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.utils;

import java.util.Iterator;
import java.util.LinkedList;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;

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
