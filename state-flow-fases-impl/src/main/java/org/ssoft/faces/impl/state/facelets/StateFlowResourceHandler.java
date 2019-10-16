/*
 * Copyright 2019 waldek.
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
package org.ssoft.faces.impl.state.facelets;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ResourceHandler;
import javax.faces.application.ResourceHandlerWrapper;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.BEFORE_HANDLE_RESOURCE;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.task.FacesProcessHolder;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;

public class StateFlowResourceHandler extends ResourceHandlerWrapper {

    private static final Logger LOGGER = Logger.getLogger(StateFlowResourceHandler.class.getName());

    private final ResourceHandler wrapped;

    public StateFlowResourceHandler(ResourceHandler wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ResourceHandler getWrapped() {
        return this.wrapped;
    }

//    @Override
//    public Resource createResource(String resourceName, String libraryName) {
//        Resource resource = super.createResource(resourceName, libraryName);
//        if (resource != null) {
//            return new StateFlowResource(resource);
//        } else {
//            return resource;
//        }
//    }
//
//    @Override
//    public Resource createResource(String resourceName, String libraryName, String contentType) {
//        Resource resource = super.createResource(resourceName, libraryName, contentType);
//        if (resource != null) {
//            return new StateFlowResource(resource);
//        } else {
//            return resource;
//        }
//    }
//
    @Override
    public void handleResourceRequest(FacesContext fc) throws IOException {
        Map<String, String> params = fc.getExternalContext().getRequestParameterMap();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = null;

        if (executorId == null) {
            executorId = params.get("exid");
        }
        SCXMLExecutor executor = null;
        
        if(executorId != null) {
           executor = handler.getExecutor(fc, executorId);
        }

        if (executor != null) {
            

            Context ctx = handler.getFlowContext(fc, executorId);

            SCXMLExecutor rootexecutor = handler.getRootExecutor(fc, executorId);

            ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
            String path = executorId;
            ExecuteExpressionFactory.getBuildPathStack(fc).push(path);

            boolean pushed;

            String executePath = executorId;
            Context ectx = executor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, executor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);

            if (rootexecutor != null) {
                String rootid = rootexecutor.getId();
                if (rootid != null) {
                    fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, rootid);
                    ctx.removeLocal(FACES_EXECUTOR_VIEW_ROOT_ID);
                }

                String resId = fc.getExternalContext().getRequestPathInfo();
                
                try {
                    EventDispatcher ed = rootexecutor.getEventdispatcher();
                    if (ed instanceof FacesProcessHolder) {
                        EventBuilder deb = new EventBuilder(BEFORE_HANDLE_RESOURCE,
                                TriggerEvent.CALL_EVENT)
                                .sendId(resId);

                        rootexecutor.triggerEvent(deb.build());
                    }
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }
            }

            super.handleResourceRequest(fc);

            if (pushed) {
                manager.pop();
            }

        } else {
            super.handleResourceRequest(fc);
        }
    }
}
