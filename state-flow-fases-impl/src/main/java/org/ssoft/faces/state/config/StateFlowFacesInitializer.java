/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.config;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import static org.ssoft.faces.state.FlowConstants.ANNOTATED_CLASSES;
import javax.faces.state.annotation.FlowAction;
import javax.faces.state.annotation.FlowInvoker;

/**
 *
 * @author Waldemar Kłaczyński
 */
@SuppressWarnings({"UnusedDeclaration"})
@HandlesTypes({
    FlowAction.class,
    FlowInvoker.class
})
public class StateFlowFacesInitializer implements ServletContainerInitializer {

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
