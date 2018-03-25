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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;
import org.apache.faces.impl.state.log.FlowLogger;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class XPathELResolver extends ELResolver {

    /**
     * Log.
     */
    public static final Logger log = FlowLogger.FLOW.getLogger();

    private static XPathFactory XPATH_FACTORY;
    private static HashMap exprCache;

    private static DocumentBuilderFactory dbf = null;
    private static DocumentBuilder db = null;
    private static Document d = null;

    private final ThreadLocal<String> modifiedXPath = new ThreadLocal<>();

    static {
        XPATH_FACTORY = XPathFactory.newInstance();
    }

    private synchronized static void staticInit() {
        if (exprCache == null) {
            exprCache = new HashMap();
        }
    }

    private final boolean isReadOnly;
    static private Class<?> theUnmodifiableListClass = Collections.unmodifiableList(new ArrayList<>()).getClass();

    public XPathELResolver() {
        this.isReadOnly = false;
    }

    public XPathELResolver(boolean isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null) {
            if (base instanceof Node) {
                context.setPropertyResolved(true);
                Node node = (Node) base;
                Object result = selectObject(context, node, (String) property);
                if (result == null) {
                    throw new PropertyNotFoundException();
                }
                return result.getClass();

            } else if (base instanceof NamedNodeMap) {
                Node result = getFromNodeMap(context, (NamedNodeMap) base, property);
                if (result != null) {
                    return result.getClass();
                } else {
                    return Node.class;
                }
            } else if (base instanceof NodeList) {
                context.setPropertyResolved(true);
                NodeList list = (NodeList) base;
                int index = toIndex(property);
                if (index < 0) {
                    Object result = selectObject(context, list, (String) property);
                    if (result == null) {
                        throw new PropertyNotFoundException();
                    }
                    return result.getClass();
                } else {
                    if (index >= list.getLength()) {
                        throw new PropertyNotFoundException();
                    }
                    return Node.class;
                }
            }
        }
        return null;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null) {
            if (base instanceof Node) {
                context.setPropertyResolved(true);
                Node node = (Node) base;
                Object result = selectObject(context, node, (String) property);
                return result;
            } else if (base instanceof XPathNodeList) {
                context.setPropertyResolved(true);
                XPathNodeList list = (XPathNodeList) base;
                int index = toIndex(property);
                if (index < 0) {
                    Object result = selectObject(context, list, (String) property);
                    return result;
                } else {
                    if (index >= list.size()) {
                        return null;
                    }
                    return list.get(index);
                }
            } else if (base instanceof XPathNodeMap) {
                context.setPropertyResolved(true);
                XPathNodeMap map = (XPathNodeMap) base;
                Object result = selectObject(context, map, (String) property);
                return result;
            } else if (base instanceof NamedNodeMap) {
                Node result = getFromNodeMap(context, (NamedNodeMap) base, property);
                return result;
            } else if (base instanceof NodeList) {
                context.setPropertyResolved(true);
                NodeList list = (NodeList) base;
                int index = toIndex(property);
                if (index < 0) {
                    Object result = selectObject(context, list, (String) property);
                    return result;
                } else {
                    if (index >= list.getLength()) {
                        return null;
                    }
                    return list.item(index);
                }
            }
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null) {
            if (base instanceof Node) {
                context.setPropertyResolved(true);
                Node node = (Node) base;

            }
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {

        if (context == null) {
            throw new NullPointerException();
        }

        if (base != null) {
            if (base instanceof Node) {
                context.setPropertyResolved(true);

                return false;
            } else if (base instanceof NodeList) {
                context.setPropertyResolved(true);
                NodeList list = (NodeList) base;
                int index = toIndex(property);
                if (index < 0) {
                } else {
                    if (index >= list.getLength()) {
                        throw new PropertyNotFoundException();
                    }
                    return list.getClass() == theUnmodifiableListClass || isReadOnly;
                }
            }
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null && base instanceof List) {
            return Integer.class;
        }
        return null;
    }

    private int toIndex(Object p) {

        try {
            if (p instanceof Integer) {
                return ((Integer) p);
            }
            if (p instanceof Character) {
                return ((Character) p);
            }
            if (p instanceof Number) {
                return ((Number) p).intValue();
            }
            if (p instanceof String) {
                return Integer.parseInt((String) p);
            }
        } catch (NumberFormatException ex) {
        }
        return -1;
    }

    public Object selectObject(ELContext context, Object base, String xpathString) throws ELException {
        if (base != null) {
            if (base instanceof Node) {
                Node node = (Node) base;
                if (null != xpathString) {
                    switch (xpathString) {
                        case "attributes":
                        case "$attr":
                            return node.getAttributes();
                        case "nodeValue":
                        case "$value":
                            return node.getNodeValue();
                        case "nodeName":
                        case "$name":
                            return node.getNodeName();
                        case "nodeType":
                        case "$type":
                            return node.getNodeType();
                        case "namespaceURI":
                        case "namespace":
                        case "$ns":
                            return node.getNamespaceURI();
                        case "localName":
                        case "$lname":
                        case "$ln":
                            return node.getLocalName();
                        case "prefix":
                            return node.getPrefix();
                        case "textContent":
                        case "$text":
                            return node.getTextContent();
                        case "childNodes":
                        case "$child":
                            return new XPathNodeList(node.getChildNodes());
                    }
                    return selectNodes(context, base, xpathString);
                }
            } else if (base instanceof XPathNodeList) {
                XPathNodeList list = (XPathNodeList) base;
                if (list.isEmpty()) {
                    return null;
                }
                if (list.size() == 1) {
                    return selectObject(context, list.get(0), xpathString);
                } else {
                    return selectNodes(context, base, xpathString);
                }
            } else if (base instanceof XPathNodeMap) {
                XPathNodeMap map = (XPathNodeMap) base;
                XPathNodeList list = map.get(xpathString);
                if (list == null || list.isEmpty()) {
                    return null;
                }
                if (list.size() == 1) {
                    return list.get(0);
                } else {
                    return list;
                }
            } else if (base instanceof NodeList) {
                NodeList list = (NodeList) base;
                if (list.getLength() == 1) {
                    return selectObject(context, list.item(0), xpathString);
                } else {
                    return selectNodes(context, base, xpathString);
                }
            }
        }
        return null;
    }

    public Object selectNodes(ELContext context, Object base, String expression) throws ELException {
        staticInit();

        String query = expression.trim();
        if (base instanceof XPathNodeList) {
            XPathNodeList nlist = (XPathNodeList) base;

            if (query.equals("first()")) {
                return nlist.get(0);
            } else if (query.equals("last()")) {
                return nlist.get(nlist.size() - 1);
            } else if (query.startsWith("@")) {
                query = query.substring(1);
                String aname = query;
                if (containsOnlyAlphaNumeric(aname)) {
                    XPathNodeMap map = new XPathNodeMap();
                    for (Node node : nlist) {
                        Element el = (Element) node;
                        if (el.hasAttribute(aname)) {
                            String attribute = el.getAttribute(aname);
                            XPathNodeList lnodes = map.get(attribute);
                            if (lnodes == null) {
                                lnodes = new XPathNodeList();
                                map.put(attribute, lnodes);
                            }
                            lnodes.add(node);
                        }
                    }
                    return map;
                }
            }
            query = expression.trim();
        }

        if (base instanceof Node) {
            Node baseNode = (Node) base;
            if (hasChildElements(baseNode)) {
                XPathNodeList c = getChildrenByTagName(baseNode, query);
                if (c != null) {
                    return c;
                }
            }
        }

        if (base instanceof Element) {
            Element el = (Element) base;
            if (el.hasAttribute(query)) {
                return el.getAttributes().getNamedItem(query);
            }
        }

        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(context);
        Node contextNode = adaptParamsForXalan(base, query.trim());
        expression = modifiedXPath.get();

        String type = "NODESET";
        String typens = "http://www.w3.org/1999/XSL/Transform";

        int sep = expression.lastIndexOf(";");
        if (sep > 0) {
            type = expression.substring(sep + 1);
            expression = expression.substring(0, sep);
            sep = type.indexOf(":");
            if (sep > 0) {
                typens = type.substring(0, sep - 1);
                type = type.substring(sep);
            }
        }

        try {
            QName RETUTN_TYPE = new QName(typens, type);
            XPath xpath = XPATH_FACTORY.newXPath();
            Document document;
            if (contextNode instanceof Document) {
                document = (Document) contextNode;
            } else {
                document = contextNode.getOwnerDocument();
            }
            JSTLXPathNamespaceContext nc = new JSTLXPathNamespaceContext(document);

            xpath.setNamespaceContext(nc);
            xpath.setXPathVariableResolver(jxvr);
            Object nl = xpath.evaluate(expression, contextNode, RETUTN_TYPE);
            if (nl instanceof Node) {
                return new XPathNodeList(nl);
            } else if (nl instanceof NodeList) {
                return new XPathNodeList(nl);
            } else {
                return nl;
            }
        } catch (XPathExpressionException ex) {
            throw new ELException(ex.toString(), ex);
        }
    }

    public Node getFromNodeMap(ELContext context, NamedNodeMap map, Object property) {
        context.setPropertyResolved(true);
        int index = toIndex(property);
        if (index < 0) {
            String name = property.toString();
            String ns = null;
            int sep = name.indexOf(":");
            if (sep > 0) {
                ns = name.substring(0, sep - 1);
                name = name.substring(sep);
            }
            Node result;
            if (ns != null) {
                result = map.getNamedItem(name);
            } else {
                result = map.getNamedItem(name);
            }
            if (result == null) {
                throw new PropertyNotFoundException();
            }
            return result;
        } else {
            if (index >= map.getLength()) {
                throw new PropertyNotFoundException();
            }
            return null;
        }
    }

    static Document getDummyDocument() {
        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);
            }
            db = dbf.newDocumentBuilder();

            DOMImplementation dim = db.getDOMImplementation();
            d = dim.createDocument("http://java.sun.com/jstl", "dummyroot", null);
            return d;
        } catch (ParserConfigurationException | DOMException e) {
            log.log(Level.SEVERE, "getDummyDocument error.", e);
        }
        return null;
    }

    static Document getDummyDocumentWithoutRoot() {
        try {
            if (dbf == null) {
                dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                dbf.setValidating(false);
            }
            db = dbf.newDocumentBuilder();

            d = db.newDocument();
            return d;
        } catch (ParserConfigurationException e) {
            log.log(Level.SEVERE, "getDummyDocumentWithoutRoot error.", e);
        }
        return null;
    }

    protected Node adaptParamsForXalan(Object base, String xpath) {
        Node boundDocument = null;

        modifiedXPath.set(xpath);
        String origXPath = xpath;
        boolean whetherOrigXPath = true;

        try {
            Object varObject = base;

            if (xpath.startsWith("first()")
                    || xpath.startsWith("last()")
                    || xpath.startsWith("@")) {
                xpath = "[" + xpath + "]";
            }

            if (Class.forName("org.w3c.dom.Document").isInstance(varObject)) {
                Document document = ((Document) varObject);
                boundDocument = document;
            } else if (Class.forName(XPathNodeList.class.getName()).isInstance(varObject)) {
                Document newDocument = getDummyDocument();

                XPathNodeList jstlNodeList = (XPathNodeList) varObject;
                if (jstlNodeList.size() == 1) {
                    if (Class.forName("org.w3c.dom.Node").isInstance(jstlNodeList.get(0))) {
                        Node node = (Node) jstlNodeList.get(0);
                        Document doc = getDummyDocumentWithoutRoot();
                        Node importedNode = doc.importNode(node, true);
                        doc.appendChild(importedNode);
                        boundDocument = doc;
                        if (whetherOrigXPath) {
                            if (xpath.startsWith("[")) {
                                xpath = "//*" + xpath;
                            } else {
                                xpath = "/*" + xpath;
                            }
                        }
                    } else {
                        Object myObject = jstlNodeList.get(0);
                        xpath = myObject.toString();
                        boundDocument = newDocument;
                    }

                } else {

                    Element dummyroot = newDocument.getDocumentElement();
                    for (int i = 0; i < jstlNodeList.size(); i++) {
                        Node currNode = (Node) jstlNodeList.get(i);

                        Node importedNode = newDocument.importNode(currNode, true);

                        dummyroot.appendChild(importedNode);

                    }
                    boundDocument = newDocument;

                    if (xpath.startsWith("[")) {
                        xpath = "//*" + xpath;
                    } else {
                        xpath = "/*" + xpath;
                    }
                }
            } else if (Class.forName("org.w3c.dom.Node").isInstance(varObject)) {
                boundDocument = (Node) varObject;
            } else {
                boundDocument = getDummyDocument();
                xpath = origXPath;
            }
        } catch (ClassNotFoundException cnf) {
            log.log(Level.SEVERE, "adaptParamsForXalan error.", cnf);
        }

        modifiedXPath.set(xpath);
        return boundDocument;
    }

    private boolean hasChildElements(Node el) {
        NodeList children = el.getChildNodes();
        for (int i = 0, n = children.getLength(); i < n; i++) {
            if (children.item(i) instanceof Element) {
                return true;
            }
        }
        return false;
    }

    private XPathNodeList getChildrenByTagName(Node el, String tagChildName) {
        XPathNodeList l = new XPathNodeList();
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

    private boolean containsOnlyAlphaNumeric(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isLetterOrDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

}
