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

import javax.faces.state.PathResolverHolder;
import javax.faces.state.NamespacePrefixesHolder;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.el.ValueExpression;
import javax.faces.state.PathResolver;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Invoke implements NamespacePrefixesHolder, PathResolverHolder, Serializable {

    /**
     * The type of target to be invoked.
     */
    private String targettype;

    /**
     * The source URL for the external service.
     */
    private ValueExpression src;

    /**
     * The ID of the invoke message.
     */
    private String id;
    
    /**
     * The Map of the params to be sent to the invoked process.
     *
     * Remove with deprecated getParams() in 1.0
     */
    private final Map<String, ValueExpression> params;

    /**
     * The List of the params to be sent to the invoked process.
     */
    private final List<Param> paramsList;

    /**
     * The &lt;finalize&gt; child, may be null.
     */
    private Finalize finalize;
    
    /**
     * {@link PathResolver} for resolving the "src" or "srcexpr" result.
     */
    private PathResolver pathResolver;

    /**
     * The current XML namespaces in the document for this action node,
     * preserved for deferred XPath evaluation.
     */
    private Map namespaces;

    /**
     * Default no-args constructor for Digester.
     */
    public Invoke() {
        params = new HashMap<>();
        paramsList = new ArrayList();
    }

    /**
     * Get the target type for this &lt;invoke&gt; element.
     *
     * @return String Returns the targettype.
     */
    public final String getTargettype() {
        return targettype;
    }

    /**
     * Set the target type for this &lt;invoke&gt; element.
     *
     * @param targettype The targettype to set.
     */
    public final void setTargettype(final String targettype) {
        this.targettype = targettype;
    }

    /**
     * Get the URL for the external service.
     *
     * @return String The URL.
     */
    public final ValueExpression getSrc() {
        return src;
    }

    /**
     * Set the URL for the external service.
     *
     * @param src The source URL.
     */
    public final void setSrc(final ValueExpression src) {
        this.src = src;
    }

    /**
     * Get the id for the external service.
     *
     * @return String The URL.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the id for the external service.
     *
     * @param id The source URL.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public final List<Param> params() {
        return paramsList;
    }

    /**
     * Add this param to this invoke.
     *
     * @param param The invoke parameter.
     */
    public final void addParam(final Param param) {
        params.put(param.getName(), param.getExpr());
        paramsList.add(param);
    }

    /**
     * Get the Finalize for this Invoke.
     *
     * @return Finalize The Finalize for this Invoke.
     */
    public final Finalize getFinalize() {
        return finalize;
    }

    /**
     * Set the Finalize for this Invoke.
     *
     * @param finalize The Finalize for this Invoke.
     */
    public final void setFinalize(final Finalize finalize) {
        this.finalize = finalize;
    }

    /**
     * Get the {@link PathResolver}.
     *
     * @return Returns the pathResolver.
     */
    @Override
    public PathResolver getPathResolver() {
        return pathResolver;
    }

    /**
     * Set the {@link PathResolver}.
     *
     * @param pathResolver The pathResolver to set.
     */
    @Override
    public void setPathResolver(final PathResolver pathResolver) {
        this.pathResolver = pathResolver;
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
