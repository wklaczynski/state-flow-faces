/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.cdi;

import java.lang.reflect.Type;
import java.util.Map;
import javax.faces.state.NamespacePrefixesHolder;
import javax.faces.state.annotation.DialogScoped;
import javax.faces.state.semantics.StateChartSemantics;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class NamespaceMapProducer extends CdiProducer<Map<String, String>> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;
    
    public NamespaceMapProducer() {
        super.name("flowNamespaces")
             .scope(DialogScoped.class)
             .qualifiers(new NamespaceMapAnnotationLiteral())
             .types(
                 new ParameterizedTypeImpl(Map.class, new Type[]{String.class, String.class}),
                 Map.class,
                 Object.class)
             .beanClass(Map.class)
             .create(e -> StateChartSemantics.getCurrent(NamespacePrefixesHolder.class).getNamespaces());
    }
    
    
}
