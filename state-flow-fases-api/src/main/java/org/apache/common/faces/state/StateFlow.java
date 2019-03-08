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
package org.apache.common.faces.state;

import java.util.ArrayDeque;
import java.util.Map;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.env.EffectiveContextMap;
import org.apache.common.scxml.env.SimpleContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlow {

    /**
     *
     */
    public static interface Name {

        /**
         *
         * @param path
         * @return
         */
        String get(String path);
    }

    private static class NameResolver implements Name {

        private final String prefix;
        private final String sufix;

        public NameResolver(String prefix, String sufix) {
            this.prefix = prefix;
            this.sufix = sufix;
        }

        @Override
        public String get(String path) {
            return (prefix != null ? prefix : "") + path + (sufix != null ? sufix : "");
        }

    }

    /**
     *
     */
    public static final String STATECHART_FACET_NAME = "javax_stateflow_metadata";

    /**
     *
     */
    public static final String STATE_MACHINE_HINT = "javax.faces.flow.STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String CUSTOM_ACTIONS_HINT = "javax.faces.flow.CUSTOM_ACTIONS_HINT";

    /**
     *
     */
    public static final String CUSTOM_INVOKERS_HINT = "javax.faces.flow.CUSTOM_INVOKERS_HINT";

    /**
     *
     */
    public static final String CURRENT_EXECUTOR_HINT = "javax.faces.flow.CURRENT_EXECUTOR_HINT";

    /**
     *
     */
    public static final String CURRENT_COMPONENT_HINT = "javax.faces.flow.CURRENT_COMPONENT_HINT";

    /**
     *
     */
    public static final String CONTROLLER_SET_HINT = "javax.faces.flow.CONTROLLER_SET_HINT";

    /**
     *
     */
    public static final String DEFINITION_SET_HINT = "javax.faces.flow.DEFINITION_SET_HINT";

    /**
     *
     */
    public static final String STATEFLOW_COMPONENT_NAME = "javax_faces_stateflow";

    /**
     *
     */
    public static final String DEFAULT_STATECHART_NAME = "main";

    /**
     *
     */
    public static final String SKIP_START_STATE_MACHINE_HINT = "javax.faces.flow.SKIP_START_STATE_MACHINE_HINT";

    /**
     *
     */
    public static final String BUILD_STATE_MACHINE_HINT = "javax.faces.flow.BUILD_STATE_MACHINE_HINT";

    /**
     *
     */
    private static final String CURRENT_EXECUTOR_STACK_KEY = "javax.faces.flow.CURRENT_EXECUTOR_STACK_KEY";

    /**
     *
     */
    public static final Name VIEW_INVOKE_CONTEXT = new NameResolver(
            "state.flow.faces:", ":ViewState");

    /**
     *
     */
    public static final String CURRENT_INVOKED_VIEW_ID
                               = "state.flow.faces:CurrentViewId";

    /**
     *
     */
    public static final String FACES_VIEW_STATE
                               = "com.sun.faces.FACES_VIEW_STATE";

    /**
     *
     */
    public static final String OUTCOME_EVENT_PREFIX = "faces.view.action.";

    /**
     *
     */
    public static final String PHASE_EVENT_PREFIX = "faces.phase.";

    /**
     *
     */
    public static final String DECODE_DISPATCHER_EVENTS = "faces.dipatrcher.events.decode";

    /**
     *
     */
    public static final String ENCODE_DISPATCHER_EVENTS = "faces.dipatcher.events.encode";

    /**
     *
     */
    public static final String BEFORE_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "before.";

    /**
     *
     */
    public static final String AFTER_PHASE_EVENT_PREFIX = PHASE_EVENT_PREFIX + "after.";

    /**
     *
     */
    public static final String BEFORE_RESTORE_VIEW = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.RESTORE_VIEW.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_RESTORE_VIEW = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.RESTORE_VIEW.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_RENDER_VIEW = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_RENDER_VIEW = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.RENDER_RESPONSE.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_INVOKE_APPLICATION = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_INVOKE_APPLICATION = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.INVOKE_APPLICATION.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_APPLY_REQUEST_VALUES = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_APPLY_REQUEST_VALUES = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.APPLY_REQUEST_VALUES.getName().toLowerCase();

    /**
     *
     */
    public static final String BEFORE_PROCESS_VALIDATIONS = BEFORE_PHASE_EVENT_PREFIX
            + PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();

    /**
     *
     */
    public static final String AFTER_PROCESS_VALIDATIONS = AFTER_PHASE_EVENT_PREFIX
            + PhaseId.PROCESS_VALIDATIONS.getName().toLowerCase();

    /**
     *
     * @param context
     */
    public static final void initViewContext(FacesContext context) {
        ArrayDeque<StateFlowViewContext> executorELStack = getExecutorStack(context);
        executorELStack.clear();
        resolveViewContext(context);
    }

    public static final void setViewContext(FacesContext context, String viewId, StateFlowViewContext viewContext) {
        context.getAttributes().put(VIEW_INVOKE_CONTEXT.get(viewId), viewContext);
        resolveViewContext(context);
    }

    public static final void resolveViewContext(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (viewRoot != null && handler.isActive(context)) {
            StateFlowViewContext viewContext = (StateFlowViewContext) context.getAttributes()
                    .get(VIEW_INVOKE_CONTEXT.get(viewRoot.getViewId()));

            if (null == viewContext) {
                clearCurrentViewContext(context);
                SCXMLExecutor root = handler.getRootExecutor(context);

                if (root == null) {
                    throw new NullPointerException("StateFlowViewContext for \""
                            + viewRoot.getViewId() + "\" mus be active!");
                }
                Context ctx = root.getRootContext();
                viewContext = new StateFlowViewContext("", root, ctx);
            }

            if (viewContext != null) {
                initCurrentViewContext(context, viewContext);
            } else {
                clearCurrentViewContext(context);
            }
        }
    }

    private static void initCurrentViewContext(FacesContext context, StateFlowViewContext viewContext) {
        context.getAttributes().put(CURRENT_EXECUTOR_HINT, viewContext.getExecutor());
        context.getELContext().putContext(SCXMLExecutor.class, viewContext.getExecutor());
        Context stateContext = getEffectiveContext(viewContext.getContext());
        context.getELContext().putContext(Context.class, stateContext);
    }

    private static void clearCurrentViewContext(FacesContext context) {
        context.getAttributes().remove(CURRENT_EXECUTOR_HINT);
    }

    public static final void pushExecutorToEL(FacesContext context, String path) {
        pushExecutorToEL(context, null, path);
    }

    public static final void pushExecutorToEL(FacesContext context, SCXMLExecutor root, String path) {
        if (context == null) {
            throw new NullPointerException("FacesContext mus be set!");
        }
        if (path == null) {
            throw new NullPointerException("Parametr \"path\" mus be set!");
        }

        StateFlowViewContext viewContext = (StateFlowViewContext) context.getAttributes()
                .get(VIEW_INVOKE_CONTEXT.get(path));

        if (null == viewContext) {
            if (root == null) {
                throw new NullPointerException("StateFlowViewContext for \"" + path + "\" mus be active!");
            }
            Context ctx = root.getRootContext();
            viewContext = new StateFlowViewContext(path, root, ctx);
        }

        ArrayDeque<StateFlowViewContext> executorELStack = getExecutorStack(context);

        executorELStack.push(viewContext);
        initCurrentViewContext(context, viewContext);

    }

    public static final void popExecutorFromEL(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        ArrayDeque<StateFlowViewContext> executorELStack = getExecutorStack(context);

        executorELStack.pop();

        if (!executorELStack.isEmpty()) {

            StateFlowViewContext viewContext = executorELStack.peek();
            initCurrentViewContext(context, viewContext);
        } else {
            resolveViewContext(context);
        }
    }

    private static ArrayDeque<StateFlowViewContext> getExecutorStack(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<StateFlowViewContext> elStack = (ArrayDeque<StateFlowViewContext>) contextAttributes.get(CURRENT_EXECUTOR_STACK_KEY);

        if (elStack == null) {
            elStack = new ArrayDeque<>();
            contextAttributes.put(CURRENT_EXECUTOR_STACK_KEY, elStack);
        }

        return elStack;
    }

    private static Context getEffectiveContext(final Context nodeCtx) {
        return new SimpleContext(nodeCtx, new EffectiveContextMap(nodeCtx));
    }

}
