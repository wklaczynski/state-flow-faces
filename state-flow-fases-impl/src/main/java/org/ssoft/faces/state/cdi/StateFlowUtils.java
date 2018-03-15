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
package org.ssoft.faces.state.cdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowUtils {
    
    private static final String STORAGE_MAP_KEY = "_____@@@ContextTransitionMap____";
    
    public static StateFlowExecutor getExecutor() {
        StateFlowExecutor result;
        FacesContext context = FacesContext.getCurrentInstance();
        result = getExecutor(context);

        return result;
    }

    public static StateFlowExecutor getExecutor(FacesContext context) {
        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        if (null == flowHandler) {
            return null;
        }
        StateFlowExecutor result = flowHandler.getExecutor(context);
        return result;
    }

    public static Map<TransitionTarget, Object> getContextsMap(final FacesContext fc,
            final StateFlowExecutor executor) {

        if (executor == null) {
            return null;
        }
        FlowContext context = executor.getRootContext();

        Map<TransitionTarget, Object> instance = (Map<TransitionTarget, Object>) context.get(STORAGE_MAP_KEY);
        if (instance == null) {
            instance = Collections.synchronizedMap(new HashMap<>());
            context.set(STORAGE_MAP_KEY, instance);
        }
        return instance;
    }

    public static FlowContext getTransitionContext(final FacesContext fc,
            final StateFlowExecutor executor,
            final TransitionTarget transitionTarget) {

        if (executor == null) {
            return null;
        }
        Map<TransitionTarget, Object> contexts = StateFlowUtils.getContextsMap(fc, executor);
        FlowContext context = (FlowContext) contexts.get(transitionTarget);
        if (context == null) {
            TransitionTarget parent = transitionTarget.getParent();
            if (parent == null) {
                context = executor.getEvaluator().newContext(transitionTarget, executor.getRootContext());
            } else {
                context = executor.getEvaluator().newContext(transitionTarget, getTransitionContext(fc, executor, parent));
            }
            contexts.put(transitionTarget, context);
        }
        return context;
    }
    
    
    
    
}
