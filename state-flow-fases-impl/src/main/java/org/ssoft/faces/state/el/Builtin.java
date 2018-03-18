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
package org.ssoft.faces.state.el;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import static javax.faces.state.FlowContext.ALL_STATES_KEY;
import javax.faces.state.FlowInstance;
import javax.faces.state.model.TransitionTarget;
import javax.xml.transform.TransformerException;
import org.apache.xml.utils.PrefixResolver;
import org.apache.xpath.XPath;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.XPathContext;
import org.ssoft.faces.state.log.FlowLogger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static javax.faces.state.FlowInstance.FLOW_ISTANCE_KEY;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.model.State;
import static javax.faces.state.utils.StateFlowHelper.getNodeValue;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Builtin implements Serializable {

    public static final Logger log = FlowLogger.FLOW.getLogger();
    
    /**
     * Implements the In() predicate for flow documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Does this state belong to the given Set of States.
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public static boolean isMember(final String state) {
        FacesContext fc = FacesContext.getCurrentInstance();
        FlowInstance istance = (FlowInstance) fc.getAttributes().get(FLOW_ISTANCE_KEY);
        
        FlowContext flowContext = getStateContext(fc);
        Set<State> states = (Set<State>) flowContext.get(ALL_STATES_KEY);
        if(states == null) {
            return false;
        }
        
        return isMember(states, state);
    }

    /**
     * Implements the In() predicate for flow documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Does this state belong to the given Set of States.
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param allStates The Set of State objects to look in
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public static boolean isMember(final Set allStates, final String state) {
        for (Iterator i = allStates.iterator(); i.hasNext();) {
            TransitionTarget tt = (TransitionTarget) i.next();
            if (state.equals(tt.getId())) {
                return true;
            }
        }
        return false;
    }


    private static FlowContext getStateContext(final FacesContext fc) {
        FlowInstance istance = (FlowInstance) fc.getAttributes().get(FLOW_ISTANCE_KEY);
        StateFlowExecutor executor = istance.getExecutor();

        Set<State> states = executor.getCurrentStatus().getStates();
        
        if(states.isEmpty()) {
            return istance.getRootContext();
        }
        
        State state = states.iterator().next();
        FlowContext context = istance.getContext(state);
        return context;
    }    
    
    /**
     * Implements the Data() function for Commons flow documents, that
     * can be used to obtain a node from one of the XML data trees.
     * Manifests within "location" attribute of &lt;assign&gt; element,
     * for Commons JEXL and Commons EL based documents.
     *
     * @param namespaces The current document namespaces map at XPath location
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, or null if no nodes match.
     */
    public static Node dataNode(final Map namespaces, final Object data, final String path) {
        if (data == null || !(data instanceof Node)) {
            log.log(Level.SEVERE, "Data(): Cannot evaluate an XPath expression"
                + " in the absence of a context Node, null returned");
            return null;
        }
        Node dataNode = (Node) data;
        NodeList result;
        try {
            if (namespaces == null || namespaces.isEmpty()) {
                if (log.isLoggable(Level.FINE)) {
                    log.log(Level.FINE, "Turning off namespaced XPath evaluation "
                            + "since no namespace information is available for "
                            + "path: {0}", path);
                }
                result = XPathAPI.selectNodeList(dataNode, path);
            } else {
                XPathContext xpathSupport = new XPathContext();
                PrefixResolver prefixResolver = new DataPrefixResolver(namespaces);
                XPath xpath = new XPath(path, null, prefixResolver, XPath.SELECT);
                int ctxtNode = xpathSupport.getDTMHandleFromNode(dataNode);
                result = xpath.execute(xpathSupport, ctxtNode, prefixResolver).nodelist();
            }
        } catch (TransformerException te) {
            log.log(Level.SEVERE, te.getMessage(), te);
            return null;
        }
        int length = result.getLength();
        if (length == 0) {
            log.log(Level.WARNING, "Data(): No nodes matching the XPath"
                    + " expression \"{0}\", returning null", path);
            return null;
        } else {
            if (length > 1) {
                log.log(Level.WARNING, "Data(): Multiple nodes matching XPath"
                        + " expression \"{0}\", returning first", path);
            }
            return result.item(0);
        }
    }

    /**
     * A variant of the Data() function for Commons SCXML documents,
     * coerced to a Double, a Long or a String, whichever succeeds,
     * in that order.
     * Manifests within rvalue expressions in the document,
     * for Commons JEXL and Commons EL based documents..
     *
     * @param namespaces The current document namespaces map at XPath location
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, coerced to a String, or null
     *         if no nodes match.
     */
    public static Object data(final Map namespaces, final Object data, final String path) {
        Object retVal;
        String strVal = getNodeValue(dataNode(namespaces, data, path));
        // try as a double
        try {
            double d = Double.parseDouble(strVal);
            retVal = d;
        } catch (NumberFormatException notADouble) {
            // else as a long
            try {
                long l = Long.parseLong(strVal);
                retVal = l;
            } catch (NumberFormatException notALong) {
                // fallback to string
                retVal = strVal;
            }
        }
        return retVal;
    }

    /**
     * Implements the Data() function for Commons SCXML documents, that
     * can be used to obtain a node from one of the XML data trees.
     * Manifests within "location" attribute of &lt;assign&gt; element,
     * for Commons JEXL and Commons EL based documents.
     *
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, or null if no nodes match.
     */
    public static Node dataNode(final Object data, final String path) {
        if (data == null || !(data instanceof Node)) {
            log.log(Level.SEVERE, "Data(): Cannot evaluate an XPath expression"
                + " in the absence of a context Node, null returned");
            return null;
        }
        Node dataNode = (Node) data;
        NodeList result;
        try {
            result = XPathAPI.selectNodeList(dataNode, path);
        } catch (TransformerException te) {
            log.log(Level.SEVERE, te.getMessage(), te);
            return null;
        }
        int length = result.getLength();
        if (length == 0) {
            log.log(Level.WARNING, "Data(): No nodes matching the XPath "
                    + "expression \"{0}\", returning null", path);
            return null;
        } else {
            if (length > 1) {
                log.log(Level.WARNING, "Data(): Multiple nodes matching XPath "
                        + "expression \"{0}\", returning first", path);
            }
            return result.item(0);
        }
    }

    /**
     * A variant of the Data() function for Commons SCXML documents,
     * coerced to a Double, a Long or a String, whichever succeeds,
     * in that order.
     * Manifests within rvalue expressions in the document,
     * for Commons JEXL and Commons EL based documents..
     *
     * @param data The context Node, though the method accepts an Object
     *             so error is reported by Commons SCXML, rather
     *             than the underlying expression language.
     * @param path The XPath expression.
     * @return The first node matching the path, coerced to a String, or null
     *         if no nodes match.
     */
    public static Object data(final Object data, final String path) {
        Object retVal;
        String strVal = getNodeValue(dataNode(data, path));
        // try as a double
        try {
            double d = Double.parseDouble(strVal);
            retVal = d;
        } catch (NumberFormatException notADouble) {
            // else as a long
            try {
                long l = Long.parseLong(strVal);
                retVal = l;
            } catch (NumberFormatException notALong) {
                // fallback to string
                retVal = strVal;
            }
        }
        return retVal;
    }

    /**
     * Prefix resolver for XPaths pointing to &lt;data&gt; nodes.
     */
    private static class DataPrefixResolver implements PrefixResolver {

        /** Cached namespaces. */
        private final Map namespaces;

        /**
         * Constructor.
         * @param namespaces The prefix to namespace URI map.
         */
        private DataPrefixResolver(final Map namespaces) {
            this.namespaces = namespaces;
        }

        /** {@inheritDoc} */
        @Override
        public String getNamespaceForPrefix(final String prefix) {
            return (String) namespaces.get(prefix);
        }

        /** {@inheritDoc} */
        @Override
        public String getNamespaceForPrefix(final String prefix,
                final Node nsContext) {
            return (String) namespaces.get(prefix);
        }

        /** {@inheritDoc} */
        @Override
        public String getBaseIdentifier() {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean handlesNullPrefixes() {
            return false;
        }

    }

}

