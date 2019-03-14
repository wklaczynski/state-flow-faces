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

import javax.faces.context.FacesContext;
import org.apache.common.faces.state.scxml.PathResolver;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.model.SCXML;
import org.apache.common.faces.state.annotation.ChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PathResolverProducer extends CdiProducer<PathResolver> {

    /**
     * Serialization version
     */
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public PathResolverProducer() {

        super.name("scxmlParhResolver")
                .scope(ChartScoped.class)
                .beanClassAndType(PathResolver.class)
                .create((e) -> {
                    StateFlowHandler fh = StateFlowHandler.getInstance();
                    SCXMLExecutor executor = fh.getCurrentExecutor(FacesContext.getCurrentInstance());
                    SCXML stateMachine = executor.getStateMachine();
                    return stateMachine != null ? stateMachine.getPathResolver() : null;
                });
    }

}
