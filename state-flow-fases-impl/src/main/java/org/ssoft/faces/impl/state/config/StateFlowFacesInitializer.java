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
package org.ssoft.faces.impl.state.config;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ANNOTATED_CLASSES;
import javax.faces.state.annotation.StateChartAction;
import javax.faces.state.annotation.StateChartActions;
import javax.faces.state.annotation.StateChartInvoker;
import javax.faces.state.annotation.StateChartInvokers;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author Waldemar Kłaczyński
 */
@SuppressWarnings({"UnusedDeclaration"})
@HandlesTypes({
    StateChartAction.class,
    StateChartActions.class,
    StateChartInvoker.class,
    StateChartInvokers.class
})
@MetaInfServices(ServletContainerInitializer.class)
public class StateFlowFacesInitializer implements ServletContainerInitializer {

    /**
     *
     * @param classes
     * @param ctx
     * @throws ServletException
     */
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext ctx) throws ServletException {

        Set<Class<?>> annotatedClasses = new HashSet<>();
        if (classes != null) {
            annotatedClasses.addAll(classes);
        }
        ctx.setAttribute(ANNOTATED_CLASSES, annotatedClasses);

        if (shouldCheckMappings(classes, ctx)) {

            ctx.addListener(StateFlowConfigureListener.class);

        }

    }

    private boolean shouldCheckMappings(Set<Class<?>> classes, ServletContext context) {

        if (classes != null && !classes.isEmpty()) {
            return true;
        }

        return true;
    }

}
