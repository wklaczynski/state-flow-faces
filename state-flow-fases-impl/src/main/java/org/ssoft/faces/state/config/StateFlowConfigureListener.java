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
package org.ssoft.faces.state.config;

import javax.faces.state.StateFlowHandler;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.ssoft.faces.state.impl.StateFlowHandlerImpl;
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
