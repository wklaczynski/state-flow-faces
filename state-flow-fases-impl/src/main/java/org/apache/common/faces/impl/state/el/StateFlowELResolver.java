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
package org.apache.common.faces.impl.state.el;

import javax.el.CompositeELResolver;
import static org.apache.common.faces.impl.state.StateFlowImplConstants.LOCAL_XPATH_RESOLVER;
import org.apache.common.faces.impl.state.config.StateWebConfiguration;
import org.apache.common.faces.impl.state.el.xpath.XPathELResolver;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateFlowELResolver extends CompositeELResolver {

    /**
     *
     */
    public StateFlowELResolver() {
        super();
        StateWebConfiguration swc = StateWebConfiguration.getInstance();

        add(new StateFlowScopesELResolver());

        String olxr = swc.getOptionValue(LOCAL_XPATH_RESOLVER, "true");
        if (Boolean.parseBoolean(olxr)) {
            add(new XPathELResolver());
        }
    }

}
