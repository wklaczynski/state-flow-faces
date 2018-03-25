/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.scxml.model;

import java.util.Map;
import java.util.logging.Logger;

/**
 * A custom action is simply a tuple consisting of a namespace URI,
 * the local name for the custom action and the corresponding
 * {@link Action} class.
 *
 */
public class CustomAction {

    /**
     * Error logged while attempting to define a custom action
     * in a null or empty namespace.
     */
    private static final String ERR_NO_NAMESPACE =
        "Cannot define a custom SCXML action with a null or empty namespace";

    /**
     * The SCXML namespace, to which custom actions may not be added.
     */
    private static final String NAMESPACE_SCXML =
        "http://www.w3.org/2005/07/scxml";

    /**
     * Error logged while attempting to define a custom action
     * with the SCXML namespace.
     */
    private static final String ERR_RESERVED_NAMESPACE =
        "Cannot define a custom SCXML action within the SCXML namespace '"
        + NAMESPACE_SCXML + "'";

    /**
     * Error logged while attempting to define a custom action
     * in a null or empty local name.
     */
    private static final String ERR_NO_LOCAL_NAME =
        "Cannot define a custom SCXML action with a null or empty local name";

    /**
     * Error logged while attempting to define a custom action
     * which does not extend {@link Action}.
     */
    private static final String ERR_NOT_AN_ACTION =
        "Custom SCXML action does not extend Action superclass";

    /**
     * The namespace this custom action belongs to.
     */
    private String namespaceURI;

    /**
     * The local name of the custom action.
     */
    private String localName;

    /**
     * The implementation of this custom action.
     */
    private Class<? extends Action> actionClass;
    
    
    private Map<String, Object> metadata;
    

    /**
     * Constructor, if the namespace or local name is null or empty,
     * or if the implementation is not an {@link Action}, an
     * {@link IllegalArgumentException} is thrown.
     *
     * @param namespaceURI The namespace URI for this custom action.
     * @param localName The local name for this custom action.
     * @param actionClass The {@link Action} subclass implementing this
     *                    custom action.
     */
    public CustomAction(final String namespaceURI, final String localName,
            final Class<? extends Action> actionClass) {
        Logger log = Logger.getLogger("javax.faces.state");
        if (namespaceURI == null || namespaceURI.trim().length() == 0) {
            log.severe(ERR_NO_NAMESPACE);
            throw new IllegalArgumentException(ERR_NO_NAMESPACE);
        }
        if (namespaceURI.trim().equalsIgnoreCase(NAMESPACE_SCXML)) {
            log.severe(ERR_RESERVED_NAMESPACE);
            throw new IllegalArgumentException(ERR_RESERVED_NAMESPACE);
        }
        if (localName == null || localName.trim().length() == 0) {
            log.severe(ERR_NO_LOCAL_NAME);
            throw new IllegalArgumentException(ERR_NO_LOCAL_NAME);
        }
        if (actionClass == null || !Action.class.isAssignableFrom(actionClass)) {
            log.severe(ERR_NOT_AN_ACTION);
            throw new IllegalArgumentException(ERR_NOT_AN_ACTION);
        }
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.actionClass = actionClass;
    }

    /**
     * Get this custom action's implementation.
     *
     * @return Returns the action class.
     */
    public Class<? extends Action> getActionClass() {
        return actionClass;
    }

    /**
     * Get the local name for this custom action.
     *
     * @return Returns the local name.
     */
    public String getLocalName() {
        return localName;
    }

    /**
     * Get the namespace URI for this custom action.
     *
     * @return Returns the namespace URI.
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }
    
    /**
     * Get the metadata definitions specified on action element. May be
     * <code>null</code>.
     *
     * @return The metadata definitions specified on the action element, may
     * be
     * <code>null</code>.
     */
    public final Map<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Set the namespace definitions specified on the action element.
     *
     * @param metadata The metadata definitions specified on the action element.
     */
    public final void setMetadata(final Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
}

