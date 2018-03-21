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
package org.ssoft.faces.state.cdi;

import javax.faces.context.FacesContext;
import javax.scxml.PathResolver;
import javax.faces.state.annotation.StateChartScoped;
import javax.faces.state.faces.StateFlowHandler;
import javax.scxml.SCXMLExecutor;
import javax.scxml.model.SCXML;

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
        StateFlowHandler fh = StateFlowHandler.getInstance();
        SCXMLExecutor executor = fh.getExecutor(FacesContext.getCurrentInstance());
        SCXML stateMachine = executor.getStateMachine();
        
        
        super.name("flowPathResolver")
             .scope(StateChartScoped.class)
             .beanClassAndType(PathResolver.class)   
             .create(e -> null);
    }

}
