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

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewDeclarationLanguage;
import jakarta.faces.view.ViewMetadata;
import java.util.UUID;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;
import static javax.faces.state.StateFlow.FACES_VIEW_ROOT_EXECUTOR_ID;

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
        String executorId = (String) fc.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
            fc.getAttributes().put(FACES_VIEW_ROOT_EXECUTOR_ID, executorId);
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        String path = executorId + ":" + viewId;
        ExecuteExpressionFactory.getBuildPathStack(fc).push(path);

        boolean pushed = false;

        SCXMLExecutor executor = handler.getExecutor(fc, executorId);

        if (executor != null) {
            String executePath = executorId;
            Context ectx = executor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, executor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);
        }

       UIViewRoot viewRoot = wraped.createMetadataView(fc);

        ExecuteExpressionFactory.getBuildPathStack(fc).pop();

        if (pushed) {
            manager.pop();
        }
        
        return viewRoot;
    }

}
