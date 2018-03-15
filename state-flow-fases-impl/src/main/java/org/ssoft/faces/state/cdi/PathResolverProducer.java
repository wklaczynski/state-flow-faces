/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.cdi;

import javax.faces.state.FlowInstance;
import javax.faces.state.PathResolver;
import javax.faces.state.annotation.StateChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PathResolverProducer extends CdiProducer<PathResolver> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;
    
    public PathResolverProducer() {
        super.name("flowPathResolver")
             .scope(StateChartScoped.class)
             .beanClassAndType(PathResolver.class)   
             .create(e -> FlowInstance.current(PathResolver.class));
    }

}
