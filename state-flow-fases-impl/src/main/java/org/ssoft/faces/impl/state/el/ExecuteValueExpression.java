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
import java.util.Stack;
import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import static javax.faces.state.StateFlow.DISABLE_EXPRESSION_MAP;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.view.facelets.ComponentHandler;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class ExecuteValueExpression extends ValueExpression {

    private final ValueExpression originalVE;
    private String executePath;

    public ExecuteValueExpression(ValueExpression originalVE) {
        this.originalVE = originalVE;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ctx.getAttributes().containsKey(DISABLE_EXPRESSION_MAP)) {
            UIComponent component = UIComponent.getCurrentComponent(ctx);
            if (component != null) {
                if (ComponentHandler.isNew(component)) {
                    Stack<String> buildPathStack = ExecuteExpressionFactory.getBuildPathStack(ctx);
                    if (!buildPathStack.isEmpty()) {
                        executePath = ExecuteExpressionFactory.getBuildPathStack(ctx).peek();
                    }
                    component.subscribeToEvent(PostAddToViewEvent.class, new SetExecuteIdListener(this));
                } else {
                    resolveExecutePath(ctx, component);
                }
            }
        }
    }

    private void resolveExecutePath(FacesContext ctx, UIComponent component) {
        if (originalVE.isLiteralText()) {
            return;
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(ctx);
        executePath = manager.computeExecutePath(ctx, component.getParent());
    }

    @Override
    public Object getValue(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getValue(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public void setValue(ELContext elContext, Object o) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            originalVE.setValue(elContext, o);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public boolean isReadOnly(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.isReadOnly(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public Class<?> getType(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getType(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public Class<?> getExpectedType() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getExpectedType();
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public String getExpressionString() {
        return originalVE.getExpressionString();
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        return originalVE.equals(o);
    }

    @Override
    public int hashCode() {
        return originalVE.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return originalVE.isLiteralText();
    }

    @Override
    public String toString() {
        return originalVE.toString();
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

        private ExecuteValueExpression ex;

        public SetExecuteIdListener() {
        }

        public SetExecuteIdListener(ExecuteValueExpression ex) {
            this.ex = ex;
        }

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            FacesContext ctx = event.getFacesContext();
            resolveExecutePath(ctx, event.getComponent());
        }
    }

}
