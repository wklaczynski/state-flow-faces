/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface NamespacePrefixesHolder {

    /**
     * Get the map of namespaces, with keys as prefixes and values as URIs.
     *
     * @param namespaces The namespaces prefix map.
     */
    void setNamespaces(Map<String, String> namespaces);

    /**
     * Get the map of namespaces, with keys as prefixes and values as URIs.
     *
     * @return The namespaces prefix map.
     */
    Map<String, String> getNamespaces();

}

