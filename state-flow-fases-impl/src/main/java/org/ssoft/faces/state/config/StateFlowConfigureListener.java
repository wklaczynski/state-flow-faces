/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.config;

import javax.faces.state.StateFlowHandler;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.ssoft.faces.state.StateFlowHandlerImpl;
import org.ssoft.faces.state.cdi.ParallelScopeCDIContext;
import org.ssoft.faces.state.cdi.StateChartScopeCDIContex;
import org.ssoft.faces.state.cdi.StateScopeCDIContex;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowConfigureListener implements ServletContextListener, HttpSessionListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        StateFlowHandlerImpl flowHandlerImpl = new StateFlowHandlerImpl(context);
        context.setAttribute(StateFlowHandler.KEY, flowHandlerImpl);

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Do Nothing
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {

    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        StateChartScopeCDIContex.sessionDestroyed(se);
        StateScopeCDIContex.sessionDestroyed(se);
        ParallelScopeCDIContext.sessionDestroyed(se);
    }

}
