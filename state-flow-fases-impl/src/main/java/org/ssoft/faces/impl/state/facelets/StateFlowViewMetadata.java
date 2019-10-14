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
package org.ssoft.faces.impl.state.facelets;

import java.util.UUID;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewMetadata extends ViewMetadata {

    private final ViewDeclarationLanguage vdl;
    private final ViewMetadata wraped;
    private final String viewId;

    /**
     *
     * @param vdl
     * @param wraped
     * @param viewId
     */
    public StateFlowViewMetadata(ViewDeclarationLanguage vdl, ViewMetadata wraped, String viewId) {
        this.wraped = wraped;
        this.viewId = viewId;
        this.vdl = vdl;
    }

    @Override
    public String getViewId() {
        return wraped.getViewId();
    }

    @Override
    public UIViewRoot createMetadataView(FacesContext fc) {
        
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = (String) fc.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        String path = executorId + ":" + viewId;
        ExecuteExpressionFactory.getBuildPathStack(fc).push(path);

        boolean pushed = false;

        SCXMLExecutor rootexecutor = handler.getRootExecutor(fc, executorId);

        if (rootexecutor != null) {
            String executePath = executorId;
            Context ectx = rootexecutor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, rootexecutor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);
        }

       UIViewRoot viewRoot = wraped.createMetadataView(fc);

        ExecuteExpressionFactory.getBuildPathStack(fc).pop();

        if (executorId != null) {
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        if (pushed) {
            manager.pop();
        }
        
        return viewRoot;
    }

}
