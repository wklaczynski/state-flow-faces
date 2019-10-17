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
package javax.faces.state.execute;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.component.UIStateChartFacetRender;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.utils.ComponentUtils;
import static javax.faces.state.StateFlow.FACES_VIEW_ROOT_EXECUTOR_ID;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteContextManager {

    private static final String MANAGER_KEY
                                = ExecuteContextManager.class.getName();

    public enum StackType {
        TreeCreation,
        Evaluation
    }

    private final StackHandler treeCreation = new TreeCreationStackHandler();
    private final StackHandler runtime = new RuntimeStackHandler();
    private final Map<String, ExecuteContext> executeMap = new HashMap<>();

    private ExecuteContextManager() {
    }

    /**
     * @param ctx the <code>FacesContext</code> for the current request
     * @return the <code>ExecuteContextManager</code> for the current request
     */
    public static ExecuteContextManager getManager(FacesContext ctx) {

        ExecuteContextManager manager
                              = (ExecuteContextManager) ctx.getAttributes().get(MANAGER_KEY);
        if (manager == null) {
            manager = new ExecuteContextManager();
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
    public boolean push(ExecuteContext executeContext) {
        return getStackHandler(StackType.Evaluation).push(executeContext);
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
    public boolean push(ExecuteContext executeContext, StackType stackType) {
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
    public ExecuteContext peek() {
        return getStackHandler(StackType.Evaluation).peek();
    }

    /**
     * @param stackType the stack to push to the executor to
     *
     * @return the top-level executor from the specified stack without removing
     * the element
     */
    public ExecuteContext peek(StackType stackType) {
        return getStackHandler(stackType).peek();
    }

    public ExecuteContext getParentExecutor(StackType stackType,
            FacesContext ctx,
            ExecuteContext forExecutor) {
        return getStackHandler(stackType).getParentExecuteContext(ctx, forExecutor);
    }

    public void initExecuteContext(FacesContext context, String path, ExecuteContext executeContext) {
        executeMap.put(path, executeContext);
    }

    public ExecuteContext getCurrentExecuteContext(FacesContext context) {

//        SCXMLExecutor executor = (SCXMLExecutor) context.getAttributes().get(CURRENT_EXECUTOR_HINT);
//        if (executor != null) {
//            Context ctx = executor.getRootContext();
//            ExecuteContext viewContext = new ExecuteContext(null, executor, ctx);
//            return viewContext;
//        }
        ExecuteContext executeContext = peek();
        if (executeContext != null) {
            return executeContext;
        }

        UIComponent current = UIComponent.getCurrentComponent(context);
        return findExecuteContextByComponent(context, current);
    }

    public ExecuteContext findExecuteContextByPath(FacesContext context, String path) {
        ExecuteContext executeContext = executeMap.get(path);
        return executeContext;
    }

    public ExecuteContext findExecuteContextByComponent(FacesContext context, UIComponent component) {
        UIViewRoot viewRoot = context.getViewRoot();
        ExecuteContext executeContext = null;
        String executorId;
        SCXMLExecutor executor = null;
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String path = null;

        executorId = (String) context.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
        if (viewRoot != null) {
            executorId = (String) viewRoot.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
            if (executorId != null) {

                if (component != null) {
                    if (component instanceof UIParameter) {
                        component = component.getParent();
                    }

                    UIComponent currentComponent = component;
                    if (currentComponent instanceof UIStateChartExecutor) {
                        currentComponent = currentComponent.getParent();
                    }

                    UIStateChartFacetRender render = ComponentUtils
                            .lokated(UIStateChartFacetRender.class, currentComponent);
                    if (render != null) {
                        path = render.getExecutePath(context);
                        executorId = render.getExecutorId();
                        executor = handler.getExecutor(context, executorId);
                    } else {
                        UIStateChartExecutor execute = ComponentUtils
                                .lokated(UIStateChartExecutor.class, currentComponent);

                        if (execute != null) {
                            executorId = execute.getExecutorId();
                            executor = handler.getExecutor(context, executorId);
                            path = executor.getId();
                        }
                    }
                }

                if (executorId == null) {
                    executorId = (String) viewRoot.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
                }
                if (path == null) {
                    path = executorId + ":" + viewRoot.getViewId();
                }

                executeContext = executeMap.get(path);
            }

        } else {
            path = executorId;
        }

        if (executeContext == null && executorId != null) {
            if (executor == null) {
                executor = handler.getExecutor(context, executorId);
            }

            if (executor != null) {
                Context ctx = executor.getRootContext();
                executeContext = new ExecuteContext(path, executor, ctx);
                executeMap.put(path, executeContext);
            }
        }

        return executeContext;
    }

    @SuppressWarnings("UnusedAssignment")
    public String computeExecutePath(FacesContext context, UIComponent component) {
        UIViewRoot viewRoot = context.getViewRoot();
        String executorId = null;
        String path = null;

        if (component != null) {

            if (component instanceof UIParameter) {
                component = component.getParent();
            }
            UIComponent currentComponent = component;

            if (currentComponent instanceof UIStateChartExecutor) {
                currentComponent = component.getParent();
            }

            UIStateChartFacetRender render = ComponentUtils
                    .assigned(UIStateChartFacetRender.class, currentComponent);
            if (render != null) {
                executorId = render.getExecutorId();
                path = render.getExecutePath(context);
            } else {
                UIStateChartExecutor execute = ComponentUtils
                        .assigned(UIStateChartExecutor.class, currentComponent);

                if (execute != null) {
                    executorId = execute.getExecutorId();
                    path = executorId;
                }
            }

            if (path == null) {
                if (viewRoot != null) {
                    executorId = (String) viewRoot.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
                    if (executorId != null) {
                        path = executorId + ":" + viewRoot.getViewId();
                    }
                }
            }

        }

        return path;
    }

    public String computeExecutorId(FacesContext context, UIComponent component) {
        UIViewRoot viewRoot = context.getViewRoot();
        String executorId = null;

        if (component != null) {
            UIStateChartFacetRender render = ComponentUtils
                    .lokated(UIStateChartFacetRender.class, component);
            if (render != null) {
                executorId = render.getExecutorId();
            } else {
                UIStateChartExecutor execute = ComponentUtils
                        .lokated(UIStateChartExecutor.class, component);

                if (execute != null) {
                    executorId = execute.getExecutorId();
                }
            }

            if (executorId == null) {
                if (viewRoot != null) {
                    executorId = (String) viewRoot.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
                }
            }

        }

        return executorId;
    }

    public ExecuteContext findExecuteContextByComponentId(FacesContext ctx,
            String id) {

        UIViewRoot viewRoot = ctx.getViewRoot();
        if (viewRoot == null) {
            return null;
        }
        UIComponent component = viewRoot.findComponent(id);
        if (component == null) {
            return null;
        }

        return findExecuteContextByComponent(ctx, component);
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

        boolean push(ExecuteContext executeContext);

        boolean push();

        void pop();

        ExecuteContext peek();

        ExecuteContext getParentExecuteContext(FacesContext ctx,
                ExecuteContext forExecutor);

        void delete();

        Stack<ExecuteContext> getStack(boolean create);

    }

    private abstract class BaseStackHandler implements StackHandler {

        protected Stack<ExecuteContext> stack;

        @Override
        public void delete() {

            stack = null;

        }

        @Override
        public Stack<ExecuteContext> getStack(boolean create) {

            if (stack == null && create) {
                stack = new Stack<>();
            }
            return stack;

        }

        @Override
        public ExecuteContext peek() {

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
        public boolean push(ExecuteContext executeContext) {

            if (executeContext != null) {
                Stack<ExecuteContext> s = getStack(true);
                s.push(executeContext);
                return true;
            }

            return false;
        }

        @Override
        public ExecuteContext getParentExecuteContext(FacesContext ctx, ExecuteContext forExecuteContext) {
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
        public boolean push(ExecuteContext executeContext) {

            if (executeContext != null) {
                Stack<ExecuteContext> s = getStack(true);
                s.push(executeContext);
                return true;
            }
            return false;

        }

        @Override
        public ExecuteContext getParentExecuteContext(FacesContext ctx, ExecuteContext forExecuteContext) {

            Stack<ExecuteContext> s = getStack(false);
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
