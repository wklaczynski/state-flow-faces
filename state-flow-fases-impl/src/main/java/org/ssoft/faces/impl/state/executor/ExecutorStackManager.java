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
import java.util.concurrent.Executor;
import javax.faces.application.Resource;
import javax.faces.context.FacesContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.view.Location;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecutorStackManager {

    private static final String MANAGER_KEY
                                = ExecutorStackManager.class.getName();

    public enum StackType {
        TreeCreation,
        Evaluation
    }

    private final StackHandler treeCreation = new TreeCreationStackHandler();
    private final StackHandler runtime = new RuntimeStackHandler();

    private ExecutorStackManager() {
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return the <code>ExecutorStackManager</code> for the current request
     */
    public static ExecutorStackManager getManager(FacesContext ctx) {

        ExecutorStackManager manager
                             = (ExecutorStackManager) ctx.getAttributes().get(MANAGER_KEY);
        if (manager == null) {
            manager = new ExecutorStackManager();
            ctx.getAttributes().put(MANAGER_KEY, manager);
        }

        return manager;

    }

    /**
     * <p>
     * Pushes the specified executor to the <code>Evaluation</code> stack.
     * </p>
     *
     * @param compositeComponent the executor to push
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push(SCXMLExecutor compositeComponent) {
        return getStackHandler(StackType.Evaluation).push(compositeComponent);
    }

    /**
     * <p>
     * Pushes the specified executor to the desired <code>StackType</code>
     * stack.
     * </p>
     *
     * @param executor the executor to push
     * @param stackType the stack to push to the executor to
     * @return <code>true</code> if the executor was pushed, otherwise returns
     * <code>false</code>
     */
    public boolean push(SCXMLExecutor executor, StackType stackType) {
        return getStackHandler(stackType).push(executor);
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
        return getStackHandler(StackType.Evaluation).push();
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
        getStackHandler(StackType.Evaluation).pop();
    }

    /**
     * @return the top-level executor from the <code>Evaluation</code> stack
     * without removing the element
     */
    public SCXMLExecutor peek() {
        return getStackHandler(StackType.Evaluation).peek();
    }

    /**
     * @param stackType the stack to push to the executor to
     *
     * @return the top-level executor from the specified stack without removing
     * the element
     */
    public SCXMLExecutor peek(StackType stackType) {
        return getStackHandler(stackType).peek();
    }

    public SCXMLExecutor getParentExecutor(StackType stackType,
            FacesContext ctx,
            SCXMLExecutor forComponent) {
        return getStackHandler(stackType).getParentExecutor(ctx, forComponent);
    }

    public SCXMLExecutor findExecutorUsingLocation(FacesContext ctx, Location location) {

        StackHandler sh = getStackHandler(StackType.TreeCreation);
        Stack<SCXMLExecutor> s = sh.getStack(false);
        if (s != null) {
            String path = location.getPath();
            for (int i = s.size(); i > 0; i--) {
//                SCXMLExecutor cc = s.get(i - 1);
//                Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
//                if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
//                    return cc;
//                }
            }
        } else {
            // runtime eval
            String path = location.getPath();
            StateFlowHandler handler = StateFlowHandler.getInstance();
            SCXMLExecutor ce = handler.getCurrentExecutor(ctx);
//            while (ce != null) {
//                Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
//                if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
//                    return ce;
//                }
//                cc = UIComponent.getCompositeComponentParent(cc);
//            }
        }

        StateFlowHandler handler = StateFlowHandler.getInstance();
        return handler.getCurrentExecutor(ctx);
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

        boolean push(SCXMLExecutor executor);

        boolean push();

        void pop();

        SCXMLExecutor peek();

        SCXMLExecutor getParentExecutor(FacesContext ctx, SCXMLExecutor forExecutor);

        void delete();

        Stack<SCXMLExecutor> getStack(boolean create);

    }

    private abstract class BaseStackHandler implements StackHandler {

        protected Stack<SCXMLExecutor> stack;

        @Override
        public void delete() {

            stack = null;

        }

        @Override
        public Stack<SCXMLExecutor> getStack(boolean create) {

            if (stack == null && create) {
                stack = new Stack<>();
            }
            return stack;

        }

        @Override
        public SCXMLExecutor peek() {

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
        public boolean push(SCXMLExecutor executor) {

            Stack<SCXMLExecutor> tstack = ExecutorStackManager.this.treeCreation.getStack(false);
            @SuppressWarnings("LocalVariableHidesMemberVariable")
            Stack<SCXMLExecutor> stack = getStack(false);
            SCXMLExecutor cse;
            if (tstack != null) {
                cse = executor;
            } else {
                stack = getStack(false);

                if (executor == null) {
                    if (stack != null && !stack.isEmpty()) {
                        cse = getExecutorParent(stack.peek());
                    } else {
                        StateFlowHandler instance = StateFlowHandler.getInstance();
                        cse = getExecutorParent((instance
                                .getCurrentExecutor(FacesContext.getCurrentInstance())));
                    }
                } else {
                    cse = executor;
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
        public SCXMLExecutor getParentExecutor(FacesContext ctx, SCXMLExecutor forExecutor) {
            return getExecutorParent(forExecutor);
        }

        private SCXMLExecutor getExecutorParent(SCXMLExecutor comp) {
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
        public boolean push(SCXMLExecutor executor) {

            if (executor != null) {
                Stack<SCXMLExecutor> s = getStack(true);
                s.push(executor);
                return true;
            }
            return false;

        }

        @Override
        public SCXMLExecutor getParentExecutor(FacesContext ctx, SCXMLExecutor forExecutor) {

            Stack<SCXMLExecutor> s = getStack(false);
            if (s == null) {
                return null;
            } else {
                int idx = s.indexOf(forExecutor);
                if (idx == 0) {
                    return null;
                }
                return (s.get(idx - 1));
            }
        }

    }

}
