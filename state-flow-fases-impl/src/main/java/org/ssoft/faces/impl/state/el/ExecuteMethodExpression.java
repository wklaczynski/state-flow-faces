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

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.BUILD_STATE_MACHINE_HINT;
import static javax.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.validator.ValidatorException;
import org.ssoft.faces.impl.state.execute.ExecutorContextStackManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteMethodExpression extends MethodExpression {

    private final MethodExpression delegate;
    private final UIComponent component;


    public ExecuteMethodExpression(MethodExpression delegate) {
        this.delegate = delegate;
        FacesContext ctx = FacesContext.getCurrentInstance();
        if (!ctx.getAttributes().containsKey(CURRENT_EXECUTOR_HINT) && !ctx.getAttributes().containsKey(BUILD_STATE_MACHINE_HINT)) {
            component = UIComponent.getCurrentComponent(ctx);
        } else {
            component = null;
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
        if(component == null) {
            return false;
        }
        
        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(ctx);
        ExecuteContext executeContext = manager.findExecuteContextByComponent(ctx, component);
        if(executeContext != null) {
            return manager.push(executeContext);
        }
        return false;
    }


    private void popExecutor(FacesContext ctx) {
        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(ctx);
        manager.pop();
    }

}
