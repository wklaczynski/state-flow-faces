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
import java.io.Serializable;
import java.util.Map;
import javax.el.ValueExpression;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Data implements NamespacePrefixesHolder, Serializable {

    /**
     * The identifier of this data instance.
     * For backwards compatibility this is also the name.
     */
    private String id;

    /**
     * The URL to get the XML data tree from.
     */
    private ValueExpression src;

    /**
     * The expression that evaluates to the value of this data instance.
     */
    private ValueExpression expr;

    /**
     * The child XML data tree, parsed as a Node, cloned per execution
     * instance.
     */
    private Node node;

    /**
     * The current XML namespaces in the SCXML document for this action node,
     * preserved for deferred XPath evaluation. Easier than to scrape node
     * above, given the Builtin API.
     */
    private Map namespaces;

    /**
     * Constructor.
     */
    public Data() {
        this.id = null;
        this.src = null;
        this.expr = null;
        this.node = null;
    }

    /**
     * Get the id.
     *
     * @return String An identifier.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The identifier.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the URL where the XML data tree resides.
     *
     * @return String The URL.
     */
    public final ValueExpression getSrc() {
        return src;
    }

    /**
     * Set the URL where the XML data tree resides.
     *
     * @param src The source URL.
     */
    public final void setSrc(final ValueExpression src) {
        this.src = src;
    }

    /**
     * Get the expression that evaluates to the value of this data instance.
     *
     * @return ValueExpression The expression.
     */
    public final ValueExpression getExpr() {
        return expr;
    }

    /**
     * Set the expression that evaluates to the value of this data instance.
     *
     * @param expr The expression.
     */
    public final void setExpr(final ValueExpression expr) {
        this.expr = expr;
    }

    /**
     * Get the XML data tree.
     *
     * @return Node The XML data tree, parsed as a <code>Node</code>.
     */
    public final Node getNode() {
        return node;
    }

    /**
     * Set the XML data tree.
     *
     * @param node The XML data tree, parsed as a <code>Node</code>.
     */
    public final void setNode(final Node node) {
        this.node = node;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    @Override
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    @Override
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

}

