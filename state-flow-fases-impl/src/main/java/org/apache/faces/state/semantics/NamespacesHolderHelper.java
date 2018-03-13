/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.semantics;

import java.util.Collections;
import java.util.Map;
import javax.faces.state.NamespacePrefixesHolder;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class NamespacesHolderHelper implements NamespacePrefixesHolder {

    private Map<String, String> namespaces;

    public NamespacesHolderHelper(Map<String, String> namespaces) {
        this.namespaces = Collections.unmodifiableMap(namespaces);
    }

    @Override
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    @Override
    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = Collections.unmodifiableMap(namespaces);
    }

}
