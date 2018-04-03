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
package org.apache.common.faces.impl.state.facelets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialResponseWriter;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextWrapper;
import static org.apache.common.faces.impl.state.StateFlowImplConstants.STATE_FLOW_DISPATCH_TASK;
import org.apache.common.faces.state.task.DelayedEventTask;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateFlowPartialViewContext extends PartialViewContextWrapper {

    private static final String ERROR_NO_FLOW_PVC = "There is no current StateFlowPartialViewContext instance.";

    private Map<String, Object> arguments;
    private List<String> callbackScripts;
    private final PartialViewContext wrapped;
    private StateFlowPartialResponseWriter writer;

    @SuppressWarnings("LeakingThisInConstructor")
    public StateFlowPartialViewContext(PartialViewContext wrapped) {
        this.wrapped = wrapped;
        setCurrentInstance(this);
    }

    @Override
    public PartialViewContext getWrapped() {
        return wrapped;
    }

    @Override
    public PartialResponseWriter getPartialResponseWriter() {
        if (writer == null) {
            writer = new StateFlowPartialResponseWriter(this, super.getPartialResponseWriter());
        }

        return writer;
    }

    /**
     * Add a callback script to the partial response. This script will be
     * executed once the partial response is successfully retrieved at the
     * client side.
     *
     * @param callbackScript The callback script to be added to the partial
     * response.
     */
    public void addCallbackScript(String callbackScript) {
        if (callbackScripts == null) {
            callbackScripts = new ArrayList<>();
        }

        callbackScripts.add(callbackScript);
    }

    public static StateFlowPartialViewContext getCurrentInstance() {
        return getCurrentInstance(FacesContext.getCurrentInstance());
    }

    public static StateFlowPartialViewContext getCurrentInstance(FacesContext context) {
        StateFlowPartialViewContext instance = (StateFlowPartialViewContext) context.getAttributes().get(StateFlowPartialViewContext.class);

        if (instance != null) {
            return instance;
        }

        // Not found. Well, maybe the context attribute map was cleared for some reason. Get it once again.
        instance = unwrap(context.getPartialViewContext());

        if (instance != null) {
            setCurrentInstance(instance);
            return instance;
        }

        // Still not found. Well, it's end of story.
        throw new IllegalStateException(ERROR_NO_FLOW_PVC);
    }

    private static void setCurrentInstance(StateFlowPartialViewContext instance) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getAttributes().put(StateFlowPartialViewContext.class.getName(), instance);
    }

    private static StateFlowPartialViewContext unwrap(PartialViewContext context) {
        PartialViewContext unwrappedContext = context;

        while (!(unwrappedContext instanceof StateFlowPartialViewContext) && unwrappedContext instanceof PartialViewContextWrapper) {
            unwrappedContext = ((PartialViewContextWrapper) unwrappedContext).getWrapped();
        }

        if (unwrappedContext instanceof StateFlowPartialViewContext) {
            return (StateFlowPartialViewContext) unwrappedContext;
        } else {
            return null;
        }
    }

    private static class StateFlowPartialResponseWriter extends PartialResponseWriter {

        private final StateFlowPartialViewContext context;
        private final PartialResponseWriter wrapped;
        private boolean updating;

        public StateFlowPartialResponseWriter(
                StateFlowPartialViewContext viewContext,
                PartialResponseWriter wrapped) {
            super(wrapped);
            this.context = viewContext;
            this.wrapped = wrapped;
        }

        @Override
        public void startUpdate(String targetId) throws IOException {
            updating = true;
            wrapped.startUpdate(targetId);
        }

        @Override
        public void endUpdate() throws IOException {
            updating = false;
            wrapped.endUpdate();
        }

        @Override
        public void endDocument() throws IOException {
            if (updating) {
                endCDATA();
                endUpdate();
            } else {
                if (context.callbackScripts != null) {
                    for (String callbackScript : context.callbackScripts) {
                        startEval();
                        write(callbackScript);
                        endEval();
                    }
                }
                FacesContext fc = FacesContext.getCurrentInstance();
                Map<Object, Object> attrs = fc.getAttributes();
                DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
                if (curTask != null) {
                    long delay = curTask.getTime() - System.currentTimeMillis();
                    startEval();
                    write("setTimeout(function(){");
                    write("jsf.ajax.request(this,event,{");
                    write("execute:'@none',render:'@all'");
                    write("})},");
                    write(String.valueOf(delay));
                    write(")");
                    endEval();
                }

            }

            wrapped.endDocument();
        }

        @Override
        public void startDocument() throws IOException {
            wrapped.startDocument();
        }

        @Override
        public void startError(String errorName) throws IOException {
            wrapped.startError(errorName);
        }

        @Override
        public void startEval() throws IOException {
            wrapped.startEval();
        }

        @Override
        public void startExtension(Map<String, String> attributes) throws IOException {
            wrapped.startExtension(attributes);
        }

        @Override
        public void startInsertAfter(String targetId) throws IOException {
            wrapped.startInsertAfter(targetId);
        }

        @Override
        public void startInsertBefore(String targetId) throws IOException {
            wrapped.startInsertBefore(targetId);
        }

        @Override
        public void endError() throws IOException {
            wrapped.endError();
        }

        @Override
        public void endEval() throws IOException {
            wrapped.endEval();
        }

        @Override
        public void endExtension() throws IOException {
            wrapped.endExtension();
        }

        @Override
        public void endInsert() throws IOException {
            wrapped.endInsert();
        }

        @Override
        public void delete(String targetId) throws IOException {
            wrapped.delete(targetId);
        }

        @Override
        public void redirect(String url) throws IOException {
            wrapped.redirect(url);
        }

        @Override
        public void updateAttributes(String targetId, Map<String, String> attributes) throws IOException {
            wrapped.updateAttributes(targetId, attributes);
        }

    }

}
