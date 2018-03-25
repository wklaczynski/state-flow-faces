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
package org.apache.faces.impl.state.el.xpath;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.xml.transform.TransformerException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DomELResolver extends ELResolver {

    private Boolean enabled;

    public DomELResolver() {
        super();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base instanceof NodeList) {
            return Integer.class;
        }
        return String.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Object value = getValue(context, base, property);
        if (value != null) {
            return value.getClass();
        } else {
            return null;
        }
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (property == null) {
            return null;
        }
        // get the property as a string
        String propertyAsString = normalize(property.toString());

        if (base instanceof ListOfNodes) {
            // if the base object is a list of nodes, the property must be an index
            int index = getPropertyAsIndex(property);
            if (index >= 0) {
                if (context != null) {
                    context.setPropertyResolved(true);
                }
                return ((ListOfNodes) base).get(index);
            } else {
                base = ((ListOfNodes) base).get(0);
            }
        } else if (base instanceof NodeList && !(base instanceof Node)) {
            // if the base object is a DOM NodeList, the property must be an index
            int index = getPropertyAsIndex(property);
            if (index >= 0) {
                if (context != null) {
                    context.setPropertyResolved(true);
                }
                return ((NodeList) base).item(index);
            } else {
                base = ((NodeList) base).item(0);
            }
        } else if (base instanceof MapOfNodes) {
            if (context != null) {
                context.setPropertyResolved(true);
            }
            MapOfNodes map = (MapOfNodes) base;
            return map.get(propertyAsString);
        }

        if (base instanceof Node) {
            Node baseNode = (Node) base;

            // if the property contains special characters, it is most probably an XPath expression
            if (!containsOnlyAlphaNumeric(propertyAsString)) {
                try {
                    // creates the XPath expression and evaluates it
                    NodeList nodeList = XPathAPI.selectNodeList(baseNode, propertyAsString);

                    ListOfNodes list = new ListOfNodes();

                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        list.add(node);
                    }

                    // if we found a node then we consider the expression resolved
                    if (!list.isEmpty()) {
                        if (context != null) {
                            context.setPropertyResolved(true);
                        }
                        return list;
                    }
                    return null;
                } catch (TransformerException te) {
                    throw new ELException("Cannot compile XPath.", te);
                }

            }

            // if the base bean is a node and has children, then get the children of which tag name is
            // the same as the given property
            if (hasChildElements(baseNode)) {
                ListOfNodes c = getChildrenByTagName(baseNode, propertyAsString);
                if (c != null) {
                    if (context != null) {
                        context.setPropertyResolved(true);
                    }
                    return c;
                }
            }
        }

        // evaluates the expression to an attribute of the base element
        if (base instanceof Element) {
            Element el = (Element) base;
            if (el.hasAttribute(propertyAsString)) {
                if (context != null) {
                    context.setPropertyResolved(true);
                }
                return el.getAttribute(propertyAsString);
            }
        }

        return null;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        // we cannot modify the DOM
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        // we don't modify the DOM
    }

    /**
     * @param property the EL property
     * @return the property as an integer, -1 if the property is not a number
     */
    private int getPropertyAsIndex(Object property) {
        int index = -1;
        if (property instanceof Number) {
            index = ((Number) property).intValue();
        } else if (property instanceof String) {
            try {
                index = Integer.parseInt(property.toString());
            } catch (NumberFormatException exc) {
            }
        }
        return index;
    }

    /**
     * @param s the string to be tested
     * @return true if the given string contains only alphanumeric characters,
     * false otherwise
     */
    private boolean containsOnlyAlphaNumeric(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isLetterOrDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    private String normalize(String s) {
        s = s.replaceAll("`", "'");
        return s;
    }

    /**
     * @param el the element to search for children with a given tag name
     * @param tagChildName the tag name
     * @return the first element with the given tag name
     */
    private ListOfNodes getChildrenByTagName(Node el, String tagChildName) {
        ListOfNodes l = new ListOfNodes();
        NodeList children = el.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            Node c = children.item(i);
            if (c instanceof Element) {
                Element ce = (Element) c;
                if (tagChildName.equals(ce.getTagName())) {
                    l.add(ce);
                }
            }
        }
        return l.isEmpty() ? null : l;
    }

    /**
     * @param el the DOM element
     * @return true if the given DOM element has at least one children element,
     * false otherwise
     */
    private boolean hasChildElements(Node el) {
        NodeList children = el.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            if (children.item(i) instanceof Element) {
                return true;
            }
        }
        return false;
    }

    private Object getChildrenByQuery(Object base, String query) {
        ListOfNodes list = null;
        if (base instanceof NodeList) {
            NodeList nodeList = (NodeList) base;
            list = new ListOfNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                list.add(node);
            }
        } else if (base instanceof ListOfNodes) {
            list = (ListOfNodes) base;
        }
        Object result = null;
        if (list != null) {
            result = getChildFromNodeListByQuery(list, query);
        }

        return null;
    }

    private Object getChildFromNodeListByQuery(ListOfNodes list, String query) {
        if (list.isEmpty()) {
            return null;
        }
        query = query.trim();
        if (query.equals("first()")) {
            return list.get(0);
        } else if (query.equals("last()")) {
            return list.get(list.size() - 1);
        } else if (query.startsWith("@")) {
            query = query.substring(1);
            String aname = query;
            if (!containsOnlyAlphaNumeric(aname)) {
                return null;
            }
            MapOfNodes map = new MapOfNodes();
            for (Node node : list) {
                Element el = (Element) node;
                if (el.hasAttribute(aname)) {
                    ListOfNodes lnodes = map.get(aname);
                    if (lnodes == null) {
                        lnodes = new ListOfNodes();
                        map.put(aname, lnodes);
                    }
                    lnodes.add(node);
                }
            }

            return map;
        } else {
            Node first = list.get(0);
            Node parent = first.getParentNode();
            String tname = first.getLocalName();
            if (query.startsWith("/")) {

            } else if (query.startsWith("first()")
                    || query.startsWith("last()")
                    || query.startsWith("@")) {
                query = tname + "[" + query + "]";
            }
            try {
                NodeList nodeList = XPathAPI.selectNodeList(parent, query);

                ListOfNodes result = new ListOfNodes();

                for (int i = 0; i < nodeList.getLength(); i++) {
                    Node node = nodeList.item(i);
                    list.add(node);
                }

                return result;

            } catch (TransformerException te) {
                throw new ELException(String.format("(Cannot compile XPath %s.)", query), te);
            }

        }
    }

    /**
     * Encapsulates a list of nodes to give EL the opportunity to work with as
     * with a normal collection. Also it evaluates to a string, as the first
     * node text content.
     */
    private static class ListOfNodes extends ArrayList<Node> {

        public ListOfNodes() {
        }

        @Override
        public String toString() {
            return get(0).getTextContent();
        }
    }

    private static class MapOfNodes extends LinkedHashMap<String, ListOfNodes> {

    }

}
