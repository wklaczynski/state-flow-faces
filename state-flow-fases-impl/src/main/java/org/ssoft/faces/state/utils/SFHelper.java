/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.utils;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.faces.state.model.TransitionTarget;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SFHelper {

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";
    
    
    /**
     * Creates a set which contains given states and all their ancestors
     * recursively up to the upper bound. Null upperBound means root of the
     * state machine.
     *
     * @param states The Set of States
     * @param upperBounds The Set of upper bound States
     * @return transitive closure of a given state set
     */
    public static Set getAncestorClosure(final Set states,
            final Set upperBounds) {
        Set closure = new HashSet(states.size() * 2);
        for (Iterator i = states.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            while (tt != null) {
                if (!closure.add(tt)) {
                    //tt is already a part of the closure
                    break;
                }
                if (upperBounds != null && upperBounds.contains(tt)) {
                    break;
                }
                tt = tt.getParent();
            }
        }
        return closure;
    }

    /**
     * Retrieve a DOM node value as a string depending on its type.
     *
     * @param node A node to be retreived
     * @return The value as a string
     */
    public static String getNodeValue(final Node node) {
        String result = "";
        if (node == null) {
            return result;
        }
        switch (node.getNodeType()) {
            case Node.ATTRIBUTE_NODE:
                result = node.getNodeValue();
                break;
            case Node.ELEMENT_NODE:
                if (node.hasChildNodes()) {
                    Node child = node.getFirstChild();
                    StringBuilder buf = new StringBuilder();
                    while (child != null) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            buf.append(((CharacterData) child).getData());
                        }
                        child = child.getNextSibling();
                    }
                    result = buf.toString();
                }
                break;
            case Node.TEXT_NODE:
            case Node.CDATA_SECTION_NODE:
                result = ((CharacterData) node).getData();
                break;
            default:
                String err = "Trying to get value of a strange Node type: "
                        + node.getNodeType();
                //Logger.logln(Logger.W, err );
                throw new IllegalArgumentException(err);
        }
        return result.trim();
    }

    /**
     * Return true if the string is empty.
     *
     * @param attr The String to test
     * @return Is string empty
     */
    public static boolean isStringEmpty(final String attr) {
        return attr == null || attr.trim().length() == 0;
    }
    
    
    
}
