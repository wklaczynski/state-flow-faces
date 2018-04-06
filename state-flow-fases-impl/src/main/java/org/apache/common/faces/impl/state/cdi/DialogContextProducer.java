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
package org.apache.common.faces.impl.state.cdi;

import java.lang.reflect.Type;
import java.util.Map;
import javax.el.ELContext;
import javax.enterprise.context.RequestScoped;
import javax.faces.context.FacesContext;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.env.EffectiveContextMap;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogContextProducer extends CdiProducer<Map<String, Object>> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    public DialogContextProducer() {
        super.name("dialogScope")
                .scope(RequestScoped.class)
                .qualifiers(new DialogContextAnnotationLiteral())
                .types(new ParameterizedTypeImpl(Map.class, new Type[]{String.class, Object.class}),
                        Map.class,
                        Object.class)
                .beanClass(Map.class)
                .create(e -> getParams());
    }

    private Map<String, Object> getParams() {
        FacesContext fc = FacesContext.getCurrentInstance();
        ELContext elContext = fc.getELContext();
        Map<String, Object> result = null;
        SCXMLExecutor executor = (SCXMLExecutor) elContext.getContext(SCXMLExecutor.class);
        if (executor != null) {
            result = new EffectiveContextMap(executor.getRootContext());
        }
        return result;
    }

}
