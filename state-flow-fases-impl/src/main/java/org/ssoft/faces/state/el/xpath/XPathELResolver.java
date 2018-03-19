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
package org.ssoft.faces.state.el.xpath;

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
import org.ssoft.faces.state.log.FlowLogger;
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
    
    
    private static final String XPATH_FACTORY_CLASS_NAME = "org.apache.taglibs.standard.tag.common.xml.JSTLXPathFactory";
    private static XPathFactory XPATH_FACTORY;
    private static HashMap exprCache;

    private static DocumentBuilderFactory dbf = null;
    private static DocumentBuilder db = null;
    private static Document d = null;

    String modifiedXPath = null;

    static {
//        if (System.getSecurityManager() != null) {
//            AccessController.doPrivileged(new PrivilegedAction() {
//                @Override
//                public Object run() {
//                    System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, XPATH_FACTORY_CLASS_NAME);
//                    return null;
//                }
//            });
//        } else {
//            System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, XPATH_FACTORY_CLASS_NAME);
//        }
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
                context.setPropertyResolved(true);
                NamedNodeMap map = (NamedNodeMap) base;
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
                    return result.getClass();
                } else {
                    if (index >= map.getLength()) {
                        throw new PropertyNotFoundException();
                    }
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
            } else if (base instanceof NamedNodeMap) {
                context.setPropertyResolved(true);
                NamedNodeMap map = (NamedNodeMap) base;
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
                    return result.getClass();
                } else {
                    if (index >= map.getLength()) {
                        throw new PropertyNotFoundException();
                    }
                    return Node.class;
                }
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
                            return node.getAttributes();
                        case "nodeValue":
                            return node.getNodeValue();
                        case "nodeName":
                            return node.getNodeName();
                        case "nodeType":
                            return node.getNodeType();
                        case "namespaceURI":
                            return node.getNamespaceURI();
                        case "localName":
                            return node.getLocalName();
                        case "prefix":
                            return node.getPrefix();
                        case "textContent":
                            return node.getTextContent();
                        case "childNodes":
                            return new XPathNodeList(node.getChildNodes());
                        default:
                            return selectNodes(context, base, xpathString);
                    }
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
        XPathVariableResolver jxvr = new JSTLXPathVariableResolver(context);
        Node contextNode = adaptParamsForXalan(base, expression.trim());
        expression = modifiedXPath;

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

        modifiedXPath = xpath;
        String origXPath = xpath;
        boolean whetherOrigXPath = true;

        try {
            Object varObject = base;

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
                            xpath = "/*" + xpath;
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

                    xpath = "/*" + xpath;
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

        modifiedXPath = xpath;
        return boundDocument;
    }

}
