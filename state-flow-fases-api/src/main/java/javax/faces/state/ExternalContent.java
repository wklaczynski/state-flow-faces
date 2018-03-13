/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.List;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface ExternalContent {

    /**
     * Return the list of external namespaced children as
     * DOM node instances.
     *
     * @return The list of (external namespaced) child nodes.
     */
    List getExternalNodes();

}
