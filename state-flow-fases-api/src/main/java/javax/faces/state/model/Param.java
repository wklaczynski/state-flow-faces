/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import javax.faces.state.NamespacePrefixesHolder;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Param implements NamespacePrefixesHolder, Serializable {

    /**
     * The param name.
     */
    private String name;

    /**
     * The param expression, may be null.
     */
    private String expr;

    /**
     * The current XML namespaces in the document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map namespaces;

    /**
     * Default no-args constructor for Digester.
     */
    public Param() {
        name = null;
        expr = null;
    }
    /**
     * Get the name for this param.
     *
     * @return String The param name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name for this param.
     *
     * @param name The param name.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the expression for this param value.
     *
     * @return String The expression for this param value.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression for this param value.
     *
     * @param expr The expression for this param value.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
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
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    @Override
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

}

