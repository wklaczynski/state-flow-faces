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
package org.ssoft.faces.state.tag;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.logging.Logger;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.state.PathResolver;
import javax.faces.state.PathResolverHolder;
import javax.faces.state.model.*;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 * @param <T>
 */
public abstract class AbstractFlowTagHandler<T extends Object> extends TagHandler {

    private static final Logger log = FlowLogger.TAGLIB.getLogger();

    public static final String CURRENT_FLOW_OBJECT = "facelets.flow.CURRENT_FLOW_OBJECT";
    public static final String ELEMENT_MAP = "facelets.flow.ELEMENT_MAP";
    public static final String TAG_MAP = "facelets.flow.TAG_MAP";

    public static final String ELEMENT_PREFIX = "facelets.flow.PARENT:";

    private final Map<String, Class> parentMap = new LinkedHashMap<>();
    private final Map<String, Class> topParentMap = new LinkedHashMap<>();

    protected final String alias;

    private final Class<T> type;

    public AbstractFlowTagHandler(TagConfig config, Class<T> type) {
        this(config, config.getTag().getLocalName(), type);
    }

    public AbstractFlowTagHandler(TagConfig config, String alias, Class<T> type) {
        super(config);
        this.alias = alias;
        this.type = type;
    }

    protected void in(String alias, Class type) {
        parentMap.put(alias, type);
        topParentMap.put(alias, type);
    }

    protected void top(String alias, Class type) {
        topParentMap.put(alias, type);
    }

    public String getAlias() {
        return alias;
    }

    public Class<T> getType() {
        return type;
    }

    public boolean isVerifyMode(FaceletContext ctx) {
        ProjectStage projectStage = ctx.getFacesContext().getApplication().getProjectStage();
        return projectStage != ProjectStage.Production;
    }

    protected void verifyAssign(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentFlow) {
        if (!isVerifyMode(ctx)) {
            return;
        }

        boolean accept = parentMap.isEmpty();
        for (Map.Entry<String, Class> entry : parentMap.entrySet()) {
            if (entry.getValue().equals(parentFlow.getClass())) {
                accept = true;
                break;
            }
        }

        if (!accept) {
            String elmess = buildTagsMessage(parentMap.keySet());
            throw new TagException(this.tag, String.format("does not contain in parent, parent mus be %s element type.", elmess));
        }

        accept = topParentMap.isEmpty();
        for (Map.Entry<String, Class> entry : topParentMap.entrySet()) {
            if (getElementStack(parent, entry.getValue()) != null) {
                accept = true;
                break;
            }
        }

        if (!accept) {
            String elmess = buildTagsMessage(topParentMap.keySet());
            throw new TagException(this.tag, String.format("does not contain in top, any top %s element type.", elmess));
        }
    }

    private String buildTagsMessage(Collection<String> elements) {
        StringBuilder sb = new StringBuilder();
        if (elements.size() > 1) {
            sb.append(" one of [");
        }
        for (String element : elements) {
            sb.append("<").append(element).append(">,");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (elements.size() > 1) {
            sb.append("]");
        }
        return sb.toString();
    }

    public abstract void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException;

    protected void decorate(FaceletContext ctx, UIComponent parent, Object element) throws IOException {
        if (element instanceof PathResolverHolder) {
            PathResolver resolver = getElement(parent, PathResolver.class);
            PathResolverHolder holder = (PathResolverHolder) element;
            holder.setPathResolver(resolver);
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        StateChart chart = (StateChart) getElement(parent, StateChart.class);
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);

        verifyAssign(ctx, parent, chart, currentFlow);
        apply(ctx, parent, chart, currentFlow);
    }

    public T findElement(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Map<String, Object> elementMap = (Map<String, Object>) getElement(parent, ELEMENT_MAP);
        Object element = elementMap.get(tag.toString());
        return (T) element;
    }

    protected void applyNext(FaceletContext ctx, UIComponent parent, Object element) throws IOException {
        Map<String, Object> elementMap = (Map<String, Object>) getElement(parent, ELEMENT_MAP);
        if (elementMap != null) {
            elementMap.put(tag.toString(), element);
        }
        Map<Object, Tag> tagMap = (Map<Object, Tag>) getElement(parent, TAG_MAP);
        if (tagMap != null) {
            tagMap.put(element, tag);
        }

        pushElement(parent, getType(), element);
        pushElement(parent, CURRENT_FLOW_OBJECT, element);
        try {
            this.nextHandler.apply(ctx, parent);
        } finally {
            popElement(parent, CURRENT_FLOW_OBJECT);
            popElement(parent, getType());
        }
    }

    public static final void pushElement(UIComponent parent, String name, Object element) {
        Stack stack = (Stack) parent.getAttributes().get(name);
        if (stack == null) {
            stack = new Stack();
        }
        stack.push(element);
        parent.getAttributes().put(name, stack);
    }

    public static final Object getElement(UIComponent parent, String name) {
        Stack stack = (Stack) parent.getAttributes().get(name);
        if (stack == null || stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public static final Stack getElementStack(UIComponent parent, String name) {
        Stack stack = (Stack) parent.getAttributes().get(name);
        if (stack == null || stack.empty()) {
            return null;
        }
        return stack;
    }

    public static final Object popElement(UIComponent parent, String name) {
        Stack stack = (Stack) parent.getAttributes().get(name);
        if (stack == null) {
            return null;
        }
        Object result = stack.pop();
        if (stack.isEmpty()) {
            parent.getAttributes().remove(name);
        }
        return result;
    }

    public static final void pushElement(UIComponent parent, Class type, Object element) {
        String elname = ELEMENT_PREFIX + type.getName();
        pushElement(parent, elname, element);
    }

    public static final <E> E getElement(UIComponent parent, Class<E> type) {
        String elname = ELEMENT_PREFIX + type.getName();
        return (E) getElement(parent, elname);
    }

    public static final <E> Stack<E> getElementStack(UIComponent parent, Class<E> type) {
        String elname = ELEMENT_PREFIX + type.getName();
        return (Stack<E>) getElementStack(parent, elname);
    }

    public static final <E> E popElement(UIComponent parent, Class<E> type) {
        String elname = ELEMENT_PREFIX + type.getName();
        return (E) popElement(parent, elname);
    }

    protected void addChild(FaceletContext ctx, UIComponent parent, TransitionTarget child) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof StateChart) {
            StateChart chat = (StateChart) currentFlow;
            if (chat.getChildren().containsKey(child.getId())) {
                throw new TagException(this.tag, "transition target already defined!");
            }
            chat.addChild(child);
        } else if (currentFlow instanceof TransitionTarget) {
            TransitionTarget target = (TransitionTarget) currentFlow;
            if (target.getChildren().containsKey(child.getId())) {
                throw new TagException(this.tag, "transition target already defined!");
            }
            target.addChild(child);
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element!");
        }
    }

    protected String generateUniqueId(FaceletContext ctx, UIComponent parent, TransitionTarget child, String prefix) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof StateChart) {
            StateChart chat = (StateChart) currentFlow;
            return prefix+chat.getChildren().size();
        } else if (currentFlow instanceof TransitionTarget) {
            TransitionTarget target = (TransitionTarget) currentFlow;
            return prefix+target.getChildren().size();
        } else {
            throw new TagException(this.tag, "can not support generate unique id this element on parent element!");
        }
    }

    protected void addTransitionTarget(FaceletContext ctx, UIComponent parent, TransitionTarget target) throws IOException {
        StateChart chart = getElement(parent, StateChart.class);
        if (chart.getTargets().containsKey(target.getId())) {
            throw new TagException(this.tag, "transition target already defined!");
        }
        chart.addTarget(target);
    }

    protected void addTransition(FaceletContext ctx, UIComponent parent, Transition transition) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof Initial) {
            Initial target = (Initial) currentFlow;
            target.setTransition(transition);
        } else if (currentFlow instanceof TransitionTarget) {
            TransitionTarget target = (TransitionTarget) currentFlow;
            target.addTransition(transition);
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element!");
        }
    }

    protected void addAction(FaceletContext ctx, UIComponent parent, Action action) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof Executable) {
            Executable executable = (Executable) currentFlow;
            executable.addAction(action);
            action.setParent(executable);
        } else if (currentFlow instanceof If) {
            If ifaction = (If) currentFlow;
            ifaction.addAction(action);
            action.setParent(ifaction.getParent());
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element!");
        }
    }

    protected final <T> Iterator<T> findNextByType(Class<T> type) {
        return findNextByType(nextHandler, type);
    }

    public static final <T> Iterator<T> findNextByType(FaceletHandler nextHandler, Class<T> type) {
        List found = new ArrayList();
        if (type.isAssignableFrom(nextHandler.getClass())) {
            found.add(nextHandler);
        } else if (nextHandler instanceof CompositeFaceletHandler) {
            FaceletHandler[] hs = ((CompositeFaceletHandler) nextHandler).getHandlers();
            for (FaceletHandler h : hs) {
                if (type.isAssignableFrom(h.getClass())) {
                    found.add(h);
                }
            }
        }
        return found.iterator();
    }

    public final <T> Iterator<T> findNextByFlowType(Class<T> type, boolean eq) {
        return findNextByFlowType(nextHandler, type, eq);
    }

    public static final <T> Iterator<T> findNextByFlowType(FaceletHandler nextHandler, Class<T> type, boolean eq) {
        List found = new ArrayList();
        Iterator<AbstractFlowTagHandler> nextFlowTags = findNextByType(nextHandler, AbstractFlowTagHandler.class);
        while (nextFlowTags.hasNext()) {
            AbstractFlowTagHandler next = nextFlowTags.next();
            if (eq && type.equals(next.getType())) {
                found.add(next);
            } else if (!eq && type.isAssignableFrom(next.getType())) {
                found.add(next);
            }
        }
        return found.iterator();
    }

}
