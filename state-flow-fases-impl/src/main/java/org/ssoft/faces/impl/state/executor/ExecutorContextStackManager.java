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
package org.ssoft.faces.impl.state.executor;

import java.util.Stack;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.state.StateChartExecuteContext;
import javax.faces.state.StateFlowHandler;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecutorContextStackManager {

    private static final String MANAGER_KEY
                                = ExecutorContextStackManager.class.getName();

    public enum StackType {
        TreeCreation,
        Evaluation
    }

    private final StackHandler treeCreation = new TreeCreationStackHandler();
    private final StackHandler runtime = new RuntimeStackHandler();

    private ExecutorContextStackManager() {
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return the <code>ExecutorContextStackManager</code> for the current
     * request
     */
    public static ExecutorContextStackManager getManager(FacesContext ctx) {

        ExecutorContextStackManager manager
                                    = (ExecutorContextStackManager) ctx.getAttributes().get(MANAGER_KEY);
        if (manager == null) {
            manager = new ExecutorContextStackManager();
            ctx.getAttributes().put(MANAGER_KEY, manager);
        }

        return manager;

    }

    /**
     * <p>
     * Pushes the specified executor to the <code>Evaluation</code> stack.
     * </p>
     *
     * @param executeContext the executor to push
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push(StateChartExecuteContext executeContext) {
        return getStackHandler(StackType.TreeCreation).push(executeContext);
    }

    /**
     * <p>
     * Pushes the specified executor to the desired <code>StackType</code>
     * stack.
     * </p>
     *
     * @param executeContext the executor to push
     * @param stackType the stack to push to the executor to
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push(StateChartExecuteContext executeContext, StackType stackType) {
        return getStackHandler(stackType).push(executeContext);
    }

    /**
     * <p>
     * Pushes a executor derived by the push logic to the
     * <code>Evaluation</code> stack.
     * </p>
     *
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push() {
        return getStackHandler(StackType.TreeCreation).push();
    }

    /**
     * <p>
     * Pushes a executor derived by the push logic to the specified stack.
     * </p>
     *
     * @param stackType the stack to push to the executor to
     *
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push(StackType stackType) {
        return getStackHandler(stackType).push();
    }

    /**
     * <p>
     * Pops the top-level executor from the stack.
     * </p>
     *
     * @param stackType the stack to pop the top level executor from
     */
    public void pop(StackType stackType) {
        getStackHandler(stackType).pop();
    }

    /**
     * <p>
     * Pops the top-level executor from the <code>Evaluation</code> stack.
     * </p>
     */
    public void pop() {
        getStackHandler(StackType.TreeCreation).pop();
    }

    /**
     * @return the top-level executor from the <code>Evaluation</code> stack
     * without removing the element
     */
    public StateChartExecuteContext peek() {
        return getStackHandler(StackType.TreeCreation).peek();
    }

    /**
     * @param stackType the stack to push to the executor to
     *
     * @return the top-level executor from the specified stack without removing
     * the element
     */
    public StateChartExecuteContext peek(StackType stackType) {
        return getStackHandler(stackType).peek();
    }

    public StateChartExecuteContext getParentExecutor(StackType stackType,
            FacesContext ctx,
            StateChartExecuteContext forExecutor) {
        return getStackHandler(stackType).getParentExecuteContext(ctx, forExecutor);
    }

    public StateChartExecuteContext findExecuteContextByComponent(FacesContext ctx,
            UIComponent component) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        return handler.getExecuteContextByComponent(ctx, component);
    }

    private StackHandler getStackHandler(StackType type) {

        StackHandler handler = null;
        switch (type) {
            case TreeCreation:
                handler = treeCreation;
                break;
            case Evaluation:
                handler = runtime;
                break;
        }
        return handler;

    }

    private interface StackHandler {

        boolean push(StateChartExecuteContext executeContext);

        boolean push();

        void pop();

        StateChartExecuteContext peek();

        StateChartExecuteContext getParentExecuteContext(FacesContext ctx,
                StateChartExecuteContext forExecutor);

        void delete();

        Stack<StateChartExecuteContext> getStack(boolean create);

    }

    private abstract class BaseStackHandler implements StackHandler {

        protected Stack<StateChartExecuteContext> stack;

        @Override
        public void delete() {

            stack = null;

        }

        @Override
        public Stack<StateChartExecuteContext> getStack(boolean create) {

            if (stack == null && create) {
                stack = new Stack<>();
            }
            return stack;

        }

        @Override
        public StateChartExecuteContext peek() {

            if (stack != null && !stack.isEmpty()) {
                return stack.peek();
            }
            return null;

        }

    }

    private final class RuntimeStackHandler extends BaseStackHandler {

        @Override
        public void delete() {

            Stack s = getStack(false);
            if (s != null) {
                s.clear();
            }

        }

        @Override
        public void pop() {

            Stack s = getStack(false);
            if (s != null && !s.isEmpty()) {
                s.pop();
            }

        }

        @Override
        public boolean push() {

            return push(null);

        }

        @Override
        public boolean push(StateChartExecuteContext executeContext) {

            Stack<StateChartExecuteContext> tstack = ExecutorContextStackManager.this.treeCreation.getStack(false);
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Stack<StateChartExecuteContext> stack = getStack(false);
            StateChartExecuteContext cse;
            if (tstack != null) {
                cse = executeContext;
            } else {
                stack = getStack(false);

                if (executeContext == null) {
                    if (stack != null && !stack.isEmpty()) {
                        cse = getExecuteContextParent(stack.peek());
                    } else {
                        StateFlowHandler handler = StateFlowHandler.getInstance();
                        cse = getExecuteContextParent((handler
                                .getCurrentExecuteContext(FacesContext.getCurrentInstance())));
                    }
                } else {
                    cse = executeContext;
                }
            }

            if (cse != null) {
                if (stack == null) {
                    stack = getStack(true);
                }
                stack.push(cse);
                return true;
            }
            return false;

        }

        @Override
        public StateChartExecuteContext getParentExecuteContext(FacesContext ctx, StateChartExecuteContext forExecuteContext) {
            return getExecuteContextParent(forExecuteContext);
        }

        private StateChartExecuteContext getExecuteContextParent(StateChartExecuteContext comp) {
            return null;
        }

    }

    private final class TreeCreationStackHandler extends BaseStackHandler {

        @Override
        public void pop() {

            Stack s = getStack(false);
            if (s != null && !stack.isEmpty()) {
                stack.pop();
                if (stack.isEmpty()) {
                    delete();
                }
            }

        }

        @Override
        public boolean push() {

            return false;

        }

        @Override
        public boolean push(StateChartExecuteContext executeContext) {

            if (executeContext != null) {
                Stack<StateChartExecuteContext> s = getStack(true);
                s.push(executeContext);
                return true;
            }
            return false;

        }

        @Override
        public StateChartExecuteContext getParentExecuteContext(FacesContext ctx, StateChartExecuteContext forExecuteContext) {

            Stack<StateChartExecuteContext> s = getStack(false);
            if (s == null) {
                return null;
            } else {
                int idx = s.indexOf(forExecuteContext);
                if (idx == 0) {
                    return null;
                }
                return (s.get(idx - 1));
            }
        }

    }

}
