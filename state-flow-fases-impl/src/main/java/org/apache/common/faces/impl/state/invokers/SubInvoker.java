/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.faces.impl.state.invokers;

import java.util.Map;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.ParentSCXMLIOProcessor;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import org.apache.common.scxml.io.StateHolder;
import org.apache.common.scxml.io.StateHolderSaver;
import org.apache.common.scxml.model.ModelException;
import org.apache.common.scxml.model.SCXML;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document may not
 * contain external namespace elements, further invokes etc.
 */
public class SubInvoker implements Invoker, StateHolder {

    private final static Logger logger = Logger.getLogger(SubInvoker.class.getName());

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * invokeId ID.
     */
    private transient String invokeId;
    /**
     * Invoking parent SCXMLExecutor
     */
    private transient SCXMLExecutor parentSCXMLExecutor;
    /**
     * The invoked state machine executor.
     */
    private transient SCXMLExecutor executor;
    /**
     * Cancellation status.
     */
    private boolean cancelled;

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getInvokeId() {
        return invokeId;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
        this.parentSCXMLExecutor = parentSCXMLExecutor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        // not used
        return executor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final String url, final Map<String, Object> params)
            throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        SCXML scxml;
        try {
            String viewId = url;
            int pos = viewId.indexOf("META-INF/resources/");
            if (pos >= 0) {
                viewId = viewId.substring(pos + 18);
            }

            scxml = handler.createStateMachine(fc, viewId);
        } catch (Throwable ex) {
            throw new InvokerException(ex);
        }
        execute(handler, scxml, params);
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    protected void execute(StateFlowHandler handler, SCXML scxml, final Map<String, Object> params) throws InvokerException {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();

            executor = handler.createChildExecutor(fc, parentSCXMLExecutor, invokeId, scxml);
            handler.execute(fc, executor, params);
        } catch (Throwable me) {
            throw new InvokerException(me);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final TriggerEvent event) throws InvokerException {
        if (cancelled) {
            return;
        }

        if (executor != null) {

            executor.addEvent(event);

            try {
                executor.triggerEvents();
            } catch (Throwable me) {
                throw new InvokerException(me);
            }
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        if (executor.getParentSCXMLIOProcessor() != null) {
            ParentSCXMLIOProcessor ioProcessor = executor.getParentSCXMLIOProcessor();
            if (!ioProcessor.isClosed()) {
                executor.addEvent(new EventBuilder("cancel.invoke." + invokeId, TriggerEvent.CANCEL_EVENT).build());
                ioProcessor.close();
            }
        }
    }

    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[4];

        values[0] = StateHolderSaver.saveObjectState(context, this);

        SCXML stateMachine = executor.getStateMachine();

        values[1] = stateMachine.getMetadata().get("faces-viewid");
        values[2] = stateMachine.getMetadata().get("faces-chartid");
        values[3] = executor.saveState(context);

        return values;
    }

    @Override
    public void restoreState(Context context, Object state) {
        if (state == null) {
            return;
        }
        Object[] values = (Object[]) state;

        StateHolderSaver.restoreObjectState(context, values[0], this);
        
        StateFlowHandler handler = StateFlowHandler.getInstance();
        FacesContext fc = FacesContext.getCurrentInstance();

        String viewId = (String) values[1];
        String id = (String) values[2];

        SCXML stateMachine = null;
        try {
            stateMachine = handler.createStateMachine(fc, viewId, id);
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }

        if (stateMachine == null) {
            throw new FacesException(String.format("Restored state flow %s in %s not found.", viewId, id));
        }

        try {
            executor = handler.createChildExecutor(fc, parentSCXMLExecutor, invokeId, stateMachine);
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }

        executor.restoreState(context, values[3]);
    }

}
