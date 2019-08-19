/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package org.ssoft.faces.impl.state.el;

import java.io.Serializable;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import static javax.faces.state.StateFlow.DISABLE_EXPRESSION_MAP;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.state.execute.ExecuteContextManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteMethodExpression extends MethodExpression {

    private final MethodExpression delegate;
    private String executePath;

    public ExecuteMethodExpression(MethodExpression delegate) {
        this.delegate = delegate;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ctx.getAttributes().containsKey(DISABLE_EXPRESSION_MAP))  {
            UIComponent component = UIComponent.getCurrentComponent(ctx);
            if (component != null) {
                if (ComponentHandler.isNew(component)) {
                    component.subscribeToEvent(PostAddToViewEvent.class, new SetExecuteIdListener(this));
                } else {
                    resolveExecutePath(ctx, component);
                }
            }
        }
    }

    private void resolveExecutePath(FacesContext ctx, UIComponent component) {
        if(delegate.isLiteralText()) {
            return ;
        }
        
//        if(!UIComponent.isCompositeComponent(component)) {
//            return;
//        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(ctx);
        ExecuteContext executeContext = manager.findExecuteContextByComponent(ctx, component.getParent());
        if (executeContext != null) {
            executePath = executeContext.getPath();
            //executePath = executeContext.getExecutor().getId();
        }
    }

    @Override
    public MethodInfo getMethodInfo(ELContext elContext) {
        return delegate.getMethodInfo(elContext);
    }

    @Override
    public Object invoke(ELContext elContext, Object[] objects) {
        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        try {
            boolean pushed = pushExecutor(ctx);
            try {
                return delegate.invoke(elContext, objects);
            } finally {
                if (pushed) {
                    popExecutor(ctx);
                }
            }
        } catch (ELException ele) {
            if (ele.getCause() != null && ele.getCause() instanceof ValidatorException) {
                throw (ValidatorException) ele.getCause();
            }
            throw ele;
        }
    }

    @Override
    public String getExpressionString() {
        return delegate.getExpressionString();
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return delegate.isLiteralText();
    }

    private boolean pushExecutor(FacesContext ctx) {
        if (executePath == null) {
            return false;
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(ctx);
        ExecuteContext executeContext = manager.findExecuteContextByPath(ctx, executePath);
        if (executeContext != null) {
            return manager.push(executeContext);
        }
        return false;
    }

    private void popExecutor(FacesContext ctx) {
        ExecuteContextManager manager = ExecuteContextManager.getManager(ctx);
        manager.pop();
    }

    private class SetExecuteIdListener implements ComponentSystemEventListener, Serializable {

        private ExecuteMethodExpression ex;

        public SetExecuteIdListener() {
        }

        public SetExecuteIdListener(ExecuteMethodExpression ex) {
            this.ex = ex;
        }

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            FacesContext ctx = event.getFacesContext();
            resolveExecutePath(ctx, event.getComponent());
        }
    }

}
