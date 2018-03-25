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
package org.apache.faces.impl.state.tag;

import com.sun.faces.facelets.compiler.UIInstructions;
import org.apache.scxml.model.If;
import org.apache.scxml.model.Initial;
import org.apache.scxml.model.Transition;
import org.apache.scxml.model.Executable;
import org.apache.scxml.model.History;
import org.apache.scxml.model.Action;
import org.apache.scxml.model.TransitionTarget;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;
import org.apache.scxml.model.EnterableState;
import org.apache.scxml.model.Parallel;
import org.apache.scxml.model.SCXML;
import org.apache.scxml.model.State;
import org.apache.scxml.model.TransitionalState;
import org.apache.faces.impl.state.log.FlowLogger;
import org.apache.faces.impl.state.utils.Util;
import static org.apache.faces.state.StateFlow.CUSTOM_ACTIONS_HINT;
import org.apache.scxml.io.ContentParser;
import org.apache.scxml.model.CustomAction;
import org.apache.scxml.model.ParsedValue;

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

    protected static final Pattern inFct = Pattern.compile("In\\(");
    protected static final Pattern dataFct = Pattern.compile("Data\\(");

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

    public boolean isProductionMode(FaceletContext ctx) {
        ProjectStage projectStage = ctx.getFacesContext().getApplication().getProjectStage();
        return projectStage == ProjectStage.Production;
    }

    public boolean isVerifyMode(FaceletContext ctx) {
        ProjectStage projectStage = ctx.getFacesContext().getApplication().getProjectStage();
        return projectStage != ProjectStage.Production;
    }

    protected void verifyAssign(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentFlow) {
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

    public abstract void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException;

    protected void decorate(FaceletContext ctx, UIComponent parent, Object element) throws IOException {

    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        SCXML chart = (SCXML) getElement(parent, SCXML.class);
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);

        verifyAssign(ctx, parent, chart, currentFlow);
        try {
            apply(ctx, parent, chart, currentFlow);
        } catch (FaceletException th) {
            throw th;
        } catch (Throwable th) {
            throw new TagException(tag, Util.getErrorMessage(th), th);
        }
    }

    public T findElement(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Map<String, Object> elementMap = (Map<String, Object>) getElement(parent, ELEMENT_MAP);
        Object element = elementMap.get(tag.toString());
        return (T) element;
    }

    protected void applyNext(FaceletContext ctx, UIComponent parent, Object element) throws IOException {
        applyNext(ctx, tag, type, parent, element, () -> {
            nextHandler.apply(ctx, parent);
            return null;
        });
    }

    public static void applyNext(FaceletContext ctx, Tag tag, Class type, UIComponent parent, Object element, Callable call) throws IOException {
        Map<String, Object> elementMap = (Map<String, Object>) getElement(parent, ELEMENT_MAP);
        if (elementMap != null) {
            elementMap.put(tag.toString(), element);
        }
        Map<Object, Tag> tagMap = (Map<Object, Tag>) getElement(parent, TAG_MAP);
        if (tagMap != null) {
            tagMap.put(element, tag);
        }

        pushElement(parent, type, element);
        pushElement(parent, CURRENT_FLOW_OBJECT, element);
        try {
            call.call();
        } catch (FacesException | IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            popElement(parent, CURRENT_FLOW_OBJECT);
            popElement(parent, type);
        }
    }

    
    public static List<CustomAction> getCustomActions(FaceletContext ctx, UIComponent parent) {
        List<CustomAction> customActions = (List<CustomAction>) getElement(parent, CUSTOM_ACTIONS_HINT);
        return customActions;
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

    protected void addChild(FaceletContext ctx, UIComponent parent, EnterableState child) throws IOException {
        SCXML chart = getElement(parent, SCXML.class);
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof SCXML) {
            SCXML chat = (SCXML) currentFlow;
            if (chat.getChildren().contains(child)) {
                throw new TagException(this.tag, "transition target already defined.");
            }
            chat.addChild(child);
        } else if (currentFlow instanceof State) {
            State target = (State) currentFlow;
            if (target.getChildren().contains(child)) {
                throw new TagException(this.tag, "transition target already defined.");
            }
            target.addChild(child);
        } else if (currentFlow instanceof Parallel) {
            Parallel target = (Parallel) currentFlow;
            if (!(child instanceof TransitionalState)) {
                throw new TagException(this.tag, "transition target in <parallel> must be transition state type <state> or <prallel>.");
            }
            if (target.getChildren().contains(child)) {
                throw new TagException(this.tag, "transition target already defined.");
            }
            target.addChild((TransitionalState) child);
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element!");
        }
    }

    protected void addHistory(FaceletContext ctx, UIComponent parent, History child) throws IOException {
        SCXML chart = getElement(parent, SCXML.class);
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof TransitionalState) {
            TransitionalState target = (TransitionalState) currentFlow;
            if (target.getHistory().contains(child)) {
                throw new TagException(this.tag, "transition target already defined.");
            }
            target.addHistory(child);
        } else {
            throw new TagException(this.tag, "can not stored history element on parent element.");
        }

    }

    protected String generateUniqueId(FaceletContext ctx, UIComponent parent, TransitionTarget child, String prefix) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof SCXML) {
            SCXML chat = (SCXML) currentFlow;
            return prefix + chat.getChildren().size();
        } else if (currentFlow instanceof TransitionalState) {
            TransitionalState target = (TransitionalState) currentFlow;
            return prefix + target.getChildren().size();
        } else {
            throw new TagException(this.tag, "can not support generate unique id this element on parent element.");
        }
    }

    protected void addTransitionTarget(FaceletContext ctx, UIComponent parent, TransitionTarget target) throws IOException {
        String tid = target.getId();
        if (!(tid == null && tid.isEmpty())) {
            SCXML chart = getElement(parent, SCXML.class);
            if (chart.getTargets().containsKey(target.getId())) {
                throw new TagException(this.tag, "transition target already defined.");
            }
            chart.addTarget(target);
        }
    }

    protected void addTransition(FaceletContext ctx, UIComponent parent, Transition transition) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof Initial) {
            Initial target = (Initial) currentFlow;
            target.setTransition(transition);
        } else if (currentFlow instanceof TransitionalState) {
            TransitionalState target = (TransitionalState) currentFlow;
            target.addTransition(transition);
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element.");
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
            throw new TagException(this.tag, "can not stored this element on parent element.");
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
