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
package org.ssoft.faces.impl.state.cdi;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import java.lang.reflect.Type;
import java.util.Map;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.env.EffectiveContextMap;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowContextProducer extends CdiProducer<Map<String, Object>> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public FlowContextProducer() {
        super.name("scxmlScope")
                .scope(RequestScoped.class)
                .qualifiers(new FlowContextAnnotationLiteral())
                .types(new ParameterizedTypeImpl(Map.class, new Type[]{String.class, Object.class}),
                        Map.class,
                        Object.class)
                .beanClass(Map.class)
                .create(e -> getParams());
    }

    private Map<String, Object> getParams() {
        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        FacesContext fc = FacesContext.getCurrentInstance();
        Map<String, Object> result = null;
        if (null != flowHandler) {
            result = new EffectiveContextMap(flowHandler.getFlowContext(fc, null));
        }
        return result;
    }

}
