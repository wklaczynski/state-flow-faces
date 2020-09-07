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
package org.ssoft.faces.impl.state.invokers;

import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.task.FacesProcessHolder;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.ParentSCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.io.StateHolder;
import javax.faces.state.scxml.io.StateHolderSaver;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.SCXML;
import static javax.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import static javax.faces.state.StateFlow.DECODE_DISPATCHER_EVENTS;
import static javax.faces.state.StateFlow.FACES_CHART_CONTINER_NAME;
import static javax.faces.state.StateFlow.FACES_CHART_CONTINER_SOURCE;
import static javax.faces.state.StateFlow.VIEWROOT_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.FACES_CHART_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.DEFAULT_STATE_MACHINE_NAME;
import static javax.faces.state.StateFlow.STATE_CHART_FACET_NAME;
import static javax.faces.state.StateFlow.VIEW_RESTORED_HINT;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.env.AbstractSCXMLListener;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.Final;
import javax.faces.state.utils.ComponentUtils;

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
     * Cancellation status.
     */
    private String path;

    private String prevRootExecutorId;

    private String prevViewExecutorId;

    private String prevViewId;

    private String prevcId;

    private Object prevViewState;

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
            String scxmlId;
            String viewId = url;
            int sep = viewId.lastIndexOf("#");
            if (sep > -1) {
                scxmlId = viewId.substring(sep + 1);
                viewId = viewId.substring(0, sep);
            } else {
                scxmlId = DEFAULT_STATE_MACHINE_NAME;
            }

            Context sctx = parentSCXMLExecutor.getRootContext();

            int pos = viewId.indexOf("META-INF/resources/");
            if (pos >= 0) {
                viewId = viewId.substring(pos + 18);
            }

            String controllerType = VIEWROOT_CONTROLLER_TYPE;
            String continerName = STATE_CHART_FACET_NAME;
            Object continerSource = viewId;

            if ("@this".equals(viewId)) {
                viewId = (String) ictx
                        .getStateMachine().getMetadata().get("faces-viewid");

                controllerType = (String) sctx.get(FACES_CHART_CONTROLLER_TYPE);
                if (controllerType == null) {
                    controllerType = VIEWROOT_CONTROLLER_TYPE;
                }

                continerName = (String) sctx.get(FACES_CHART_CONTINER_NAME);
                continerSource = sctx.get(FACES_CHART_CONTINER_SOURCE);

            }

            UIComponent executeComponent = findExecuteComponent(fc);
            executeComponent.pushComponentToEL(fc, executeComponent);
            try {
                scxml = findStateMachine(fc, scxmlId, continerName, continerSource);

                if (scxml == null) {
                    throw new InvokerException(String.format(
                            "invoked scxml id='%s' not found in %s", scxmlId, viewId));
                }
                
                String parentid = parentSCXMLExecutor.getId();

                String childId = parentid + ":" + UUID.randomUUID().toString();

                executor = handler.createChildExecutor(childId, fc, parentSCXMLExecutor, invokeId, scxml);
            } finally {
                executeComponent.popComponentFromEL(fc);
            }

            prevRootExecutorId = handler.getViewExecutorId(fc);

            Context cctx = executor.getRootContext();
            cctx.setLocal(FACES_CHART_CONTROLLER_TYPE, controllerType);
            cctx.setLocal(FACES_CHART_CONTINER_NAME, continerName);
            cctx.setLocal(FACES_CHART_CONTINER_SOURCE, continerSource);

            path = executor.getId();

            ExecuteContext viewContext = new ExecuteContext(
                    path, invokeId, executor, executor.getGlobalContext());

            UIViewRoot currentViewRoot = fc.getViewRoot();
            if (currentViewRoot != null) {
                prevViewId = currentViewRoot.getViewId();

                StateManager sm = fc.getApplication().getStateManager();
                prevViewState = sm.saveView(fc);

                ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
                String executePath = prevRootExecutorId + ":" + prevViewId;
                ExecuteContext prevExecuteContext = manager.findExecuteContextByPath(fc, executePath);

                if (prevExecuteContext != null) {
                    prevViewExecutorId = prevExecuteContext.getExecutor().getId();
                }

                UIComponent cc = UIComponent.getCurrentComponent(fc);
                if (cc != null) {
                    prevcId = cc.getClientId();
                }

                manager.initExecuteContext(fc, path, viewContext);

            }

            executor.addListener(scxml, new AbstractSCXMLListener() {

                @Override
                public void onExit(EnterableState state) {
                    if (state instanceof Final) {
                        exitExecutor();
                    }
                }

            });

            handler.execute(fc, executor, params);

            if (!executor.isRunning()) {
                logger.warning(String.format(
                        "request to activate bean in executor \"%s\", "
                        + "but that executor is not active.", scxmlId));
            }

        } catch (FacesException | InvokerException ex) {
            throw ex;
        } catch (IOException | ModelException ex) {
            throw new InvokerException(ex.getMessage(), ex);
        }
    }

    public UIComponent findExecuteComponent(FacesContext context) {
        UIViewRoot viewRoot = context.getViewRoot();
        String sorceId = (String) context.getAttributes().get(CURRENT_COMPONENT_HINT);
        if (sorceId != null) {
            UIComponent source = viewRoot.findComponent(sorceId);
            if (source != null) {
                UIStateChartExecutor ec = ComponentUtils.passed(UIStateChartExecutor.class, source);
                if (ec != null) {
                    return ec;
                }
            }
        }
        return viewRoot;
    }

    public SCXML findStateMachine(FacesContext context, String scxmlId, String continerName, Object continerSource) throws IOException {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        if (continerSource instanceof URL) {

            URL url = (URL) continerSource;
            if (url == null) {
                throw new FacesException(
                        "Unable to localize scxml '"
                        + scxmlId
                        + "\" can not get path!");
            }

            String srcpath = localPath(context, url.getPath());

            if (continerName == null) {
                throw new FacesException(String.format(
                        "Can not find scxml definition \"%s\", "
                        + "view location not found in composite component %s.",
                        scxmlId, srcpath));
            }

            try {
                SCXML scxml = handler.getStateMachine(context, url, continerName, scxmlId);
                if (scxml == null) {
                    throw new FacesException(String.format(
                            "Can not find scxml definition id=\"%s\", not found"
                            + " in composite <f:metadata... %s",
                            scxmlId, srcpath));
                }

                return scxml;
            } catch (ModelException ex) {
                throw new FacesException(String.format(
                        "can not find scxml definition \"%s\", throw model exception %s.",
                        scxmlId, srcpath));
            }
        } else {
            String srcpath = (String) continerSource;
            try {
                SCXML scxml = handler.getStateMachine(context, srcpath, continerName, scxmlId);
                if (scxml == null) {
                    throw new FacesException(String.format(
                            "can not find scxml definition id=\"%s\", not found"
                            + " in <f:metadata... %s",
                            scxmlId, srcpath));
                }
                return scxml;
            } catch (ModelException ex) {
                throw new FacesException(String.format(
                        "can not find scxml definition \"%s\", throw model exception %s.",
                        scxmlId, srcpath));
            }
        }
    }

    private static String localPath(FacesContext context, String path) {
        String base = context.getExternalContext().getRealPath("/").replace("\\", "/");
        String result = path.replaceFirst(base, "");

        if (result.startsWith("/resources")) {
            result = result.substring(10);
            return result;
        }

        int sep = result.lastIndexOf("/META-INF/resources");
        if (sep > -1) {
            result = result.substring(sep + 19);
            return result;
        }

        return result;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

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
            ExecuteContext viewContext = new ExecuteContext(
                    path, invokeId, executor, executor.getGlobalContext());
            ExecuteContextManager manager = ExecuteContextManager.getManager(context);
            manager.initExecuteContext(context, path, viewContext);

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
            } catch (IOException | ModelException me) {
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

    private void exitExecutor() {
        restorPrevView();
    }

    private void restorPrevView() {
        FacesContext fc = FacesContext.getCurrentInstance();

        UIViewRoot viewRoot = fc.getViewRoot();
        String currentViewId = viewRoot.getViewId();

        if (prevViewId != null && !prevViewId.equals(currentViewId)) {

            if (prevViewState != null) {

                ViewHandler vh = fc.getApplication().getViewHandler();
                fc.getAttributes().put(FACES_VIEW_STATE, prevViewState);
                viewRoot = vh.restoreView(fc, prevViewId);
                fc.setViewRoot(viewRoot);
                fc.setProcessingEvents(true);
                vh.initView(fc);
                fc.setViewRoot(viewRoot);
                fc.renderResponse();
                
                if (!fc.getAttributes().containsKey(VIEW_RESTORED_HINT)) {
                    fc.getAttributes().put(VIEW_RESTORED_HINT, true);
                }

                if (prevViewExecutorId != null) {
                    ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
                    ExecuteContext executeContext = manager.findExecuteContextByPath(fc, prevViewExecutorId);
                    if (executeContext != null) {
                        String executePath = prevRootExecutorId + ":" + prevViewId;
                        ExecuteContext viewContext = new ExecuteContext(
                                executePath, executeContext.getExecutor(), executeContext.getContext());

                        manager.initExecuteContext(fc, executePath, viewContext);
                    }
                }

                if (prevcId != null) {
                    UIComponent cc = viewRoot.findComponent(prevcId);
                    if (cc != null) {
                        cc.pushComponentToEL(fc, cc);
                        cc = UIComponent.getCompositeComponentParent(cc);
                        if (cc != null) {
                            cc.pushComponentToEL(fc, cc);
                        }
                    }
                }

            }

        }

    }

    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[11];

        SCXML stateMachine = executor.getStateMachine();

        values[0] = StateHolderSaver.saveObjectState(context, this);

        values[1] = executor.getId();
        values[2] = stateMachine.getMetadata().get("faces-viewid");
        values[3] = stateMachine.getMetadata().get("faces-chartid");
        values[4] = executor.saveState(context);

        values[5] = path;
        values[6] = prevRootExecutorId;
        values[7] = prevViewExecutorId;
        values[8] = prevViewId;
        values[9] = prevcId;
        values[10] = prevViewState;

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

        path = (String) values[5];
        prevRootExecutorId = (String) values[6];
        prevViewExecutorId = (String) values[7];
        prevViewId = (String) values[8];
        prevcId = (String) values[9];
        prevViewState = (String) values[10];

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
        executor.addListener(stateMachine, new AbstractSCXMLListener() {

            @Override
            public void onExit(EnterableState state) {
                if (state instanceof Final) {
                    exitExecutor();
                }
            }

        });
    }

}
