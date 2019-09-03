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

import java.util.Stack;
import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteExpressionFactory extends ExpressionFactory {

    public static final String EXECUTION_PARH_STACK_HINT = ExecuteExpressionFactory.class.getName() + ":EXECUTION_PARH_STACK_HINT";

    private final ExpressionFactory wrapped;

    public static Stack<String> getBuildPathStack(FacesContext fc) {
        Stack<String> pathStack = (Stack<String>) fc.getAttributes().get(EXECUTION_PARH_STACK_HINT);
        if (pathStack == null) {
            pathStack = new Stack<>();
            fc.getAttributes().put(EXECUTION_PARH_STACK_HINT, pathStack);
        }
        return pathStack;
    }

    public ExecuteExpressionFactory(ExpressionFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
        return new ExecuteValueExpression(wrapped.createValueExpression(context, expression, expectedType));
    }

    @Override
    public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
        return new ExecuteValueExpression(wrapped.createValueExpression(instance, expectedType));
    }

    @Override
    public MethodExpression createMethodExpression(ELContext context, String expression, Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
        return new ExecuteMethodExpression(wrapped.createMethodExpression(context, expression, expectedReturnType, expectedParamTypes));
    }

    @Override
    public Object coerceToType(Object obj, Class<?> targetType) {
        return wrapped.coerceToType(obj, targetType);
    }

}
