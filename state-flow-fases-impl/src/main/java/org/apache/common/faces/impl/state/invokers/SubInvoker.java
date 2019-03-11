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
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.task.FacesProcessHolder;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.EventDispatcher;
import org.apache.common.scxml.InvokeContext;
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
import static org.apache.common.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import static org.apache.common.faces.state.StateFlow.DECODE_DISPATCHER_EVENTS;
import static org.apache.common.faces.state.StateFlow.DEFAULT_STATECHART_NAME;
import org.apache.common.faces.state.component.UIStateChartController;

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
    public void invoke(final InvokeContext ictx, final String url, final Map<String, Object> params)
            throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        SCXML scxml;
        try {
            String id;
            String viewId = url;
            int sep = viewId.lastIndexOf("#");
            if (sep > -1) {
                id = viewId.substring(sep + 1);
                viewId = viewId.substring(0, sep);
            } else {
                id = DEFAULT_STATECHART_NAME;
            }

            String controllerId = null;

            if ("@this".equals(viewId)) {
                String machineViewId = (String) parentSCXMLExecutor
                        .getStateMachine().getMetadata().get("faces-viewid");

                viewId = machineViewId;

                Context sctx = parentSCXMLExecutor.getRootContext();
                controllerId = (String) sctx.get(UIStateChartController.COMPONENT_ID);
            }

            int pos = viewId.indexOf("META-INF/resources/");
            if (pos >= 0) {
                viewId = viewId.substring(pos + 18);
            }

            if (controllerId == null) {
                scxml = handler.getStateMachine(fc, viewId, id);
            } else {
                UIViewRoot viewRoot = fc.getViewRoot();
                UIStateChartController controller = (UIStateChartController) viewRoot.findComponent(controllerId);
                scxml = controller.findStateMachine(fc, id);
            }

            if (scxml == null) {
                throw new InvokerException(String.format("Invoked scxml id='%s' not found in %s", id, viewId));
            }

            execute(handler, viewId, scxml, params);

        } catch (FacesException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvokerException(ex);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    /**
     *
     * @param handler
     * @param viewId
     * @param scxml
     * @param params
     * @throws InvokerException
     */
    protected void execute(StateFlowHandler handler, String viewId, SCXML scxml, final Map<String, Object> params) throws InvokerException {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            String id = parentSCXMLExecutor.getId() + ":" + viewId + "!" + getInvokeId();

            executor = handler.createChildExecutor(id, fc, parentSCXMLExecutor, invokeId, scxml);
            handler.execute(fc, executor, params);
        } catch (Throwable me) {
            throw new InvokerException(me);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final InvokeContext ictx, final TriggerEvent event) throws InvokerException {
        if (cancelled) {
            return;
        }
        FacesContext context = FacesContext.getCurrentInstance();

        if (executor != null) {

            try {
                executor.triggerEvent(event);

                if (event.getName().startsWith(DECODE_DISPATCHER_EVENTS)) {
                    EventDispatcher ed = executor.getEventdispatcher();
                    if (ed instanceof FacesProcessHolder) {
                        ((FacesProcessHolder) ed).processDecodes(context);
                    }
                }

                if (event.getName().startsWith(ENCODE_DISPATCHER_EVENTS)) {
                    EventDispatcher ed = executor.getEventdispatcher();
                    if (ed instanceof FacesProcessHolder) {
                        ((FacesProcessHolder) ed).encodeBegin(context);
                    }
                }

                executor.triggerEvents();
            } catch (FacesException ex) {
                throw ex;
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
        FacesContext context = FacesContext.getCurrentInstance();
        StateFlowHandler.getInstance().close(context, executor);
    }

    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[5];

        SCXML stateMachine = executor.getStateMachine();

        values[0] = StateHolderSaver.saveObjectState(context, this);

        values[1] = executor.getId();
        values[2] = stateMachine.getMetadata().get("faces-viewid");
        values[3] = stateMachine.getMetadata().get("faces-chartid");
        values[4] = executor.saveState(context);

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

        String executorId = (String) values[1];
        String viewId = (String) values[2];
        String id = (String) values[3];

        SCXML stateMachine = null;
        try {
            stateMachine = handler.getStateMachine(fc, viewId, id);
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }

        if (stateMachine == null) {
            throw new FacesException(String.format("Restored state flow %s in %s not found.", viewId, id));
        }

        try {
            executor = handler.createChildExecutor(executorId, fc, parentSCXMLExecutor, invokeId, stateMachine);
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }

        executor.restoreState(context, values[4]);
    }

}
