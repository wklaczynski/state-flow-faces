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

import java.lang.reflect.Type;
import java.util.Map;
import javax.faces.state.FlowInstance;
import javax.faces.state.NamespacePrefixesHolder;
import javax.faces.state.annotation.StateChartScoped;

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
             .scope(StateChartScoped.class)
             .qualifiers(new NamespaceMapAnnotationLiteral())
             .types(
                 new ParameterizedTypeImpl(Map.class, new Type[]{String.class, String.class}),
                 Map.class,
                 Object.class)
             .beanClass(Map.class)
             .create(e -> FlowInstance.current(NamespacePrefixesHolder.class).getNamespaces());
    }
    
    
}
