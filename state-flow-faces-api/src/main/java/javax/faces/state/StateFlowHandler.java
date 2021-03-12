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
package javax.faces.state;

import jakarta.el.ELContext;
import jakarta.faces.context.FacesContext;
import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.model.CustomAction;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.SCXML;
import static javax.faces.state.StateFlow.DEFAULT_STATE_MACHINE_NAME;
import static javax.faces.state.StateFlow.STATE_CHART_FACET_NAME;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.task.TimerEventProducer;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class StateFlowHandler {

    /**
     *
     */
    public static final String KEY = "javax.faces.state.StateFlowHandler";

    /**
     *
     * @return
     */
    public final static StateFlowHandler getInstance() {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = (StateFlowHandler) fc.getExternalContext().getApplicationMap().get(KEY);
        return cast(handler);
    }

    private static <T> T cast(Object obj) {
        return (T) obj;
    }

    /**
     *
     * @return
     */
    public abstract List<CustomAction> getCustomActions();

    /**
     *
     * @return
     */
    public abstract TimerEventProducer getTimerEventProducer();

    /**
     *
     * @return
     */
    public abstract Map<String, Class<? extends Invoker>> getCustomInvokers();

    /**
     *
     * @param context
     * @param executorId
     * @return
     */
    public abstract Context getFlowContext(FacesContext context, String executorId);

    /**
     *
     * @param context
     * @return
     */
    public abstract String getFlowId(FacesContext context);

    /**
     *
     * @param context
     * @return
     */
    public abstract String getViewExecutorId(FacesContext context);

//    /**
//     *
//     * @param context
//     * @param executorId
//     */
//    public abstract void setExecutorViewRootId(FacesContext context, String executorId);
    /**
     *
     * @param context
     * @return
     */
    public SCXMLExecutor getRootExecutor(FacesContext context) {
        return getRootExecutor(context, null);
    }

    /**
     *
     * @param context
     * @param executorId
     * @return
     */
    public abstract SCXMLExecutor getExecutor(FacesContext context, String executorId);

    /**
     *
     * @param context
     * @return
     */
    public abstract SCXMLExecutor getViewExecutor(FacesContext context);

    /**
     *
     * @param context
     * @return
     */
    public abstract SCXMLExecutor getViewRootExecutor(FacesContext context);

    /**
     *
     * @param context
     * @param executorId
     * @return
     */
    public abstract SCXMLExecutor getRootExecutor(FacesContext context, String executorId);

    /**
     *
     * @param context
     * @return
     */
    public abstract boolean hasViewRoot(FacesContext context);

    /**
     *
     * @param id
     * @param context
     * @param scxml
     * @return
     * @throws ModelException
     */
    public SCXMLExecutor createRootExecutor(String id, FacesContext context, SCXML scxml) throws ModelException {
        return createRootExecutor(id, context, null, null, scxml);
    }

    /**
     *
     * @param id
     * @param context
     * @param parent
     * @param invokeId
     * @param scxml
     * @return
     * @throws ModelException
     */
    public abstract SCXMLExecutor createRootExecutor(String id, FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException;

    /**
     *
     * @param id
     * @param context
     * @param parent
     * @param invokeId
     * @param scxml
     * @return
     * @throws ModelException
     */
    public abstract SCXMLExecutor createChildExecutor(String id, FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException;

    /**
     *
     * @param context
     * @param executor
     * @param params
     */
    public abstract void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params);

    /**
     *
     * @param context
     * @param evt
     */
    public abstract void broadcastEvent(FacesContext context, TriggerEvent evt);

    /**
     *
     * @param context
     * @return
     */
    public abstract List<String> getControllerClientIds(FacesContext context);

    /**
     *
     * @param context
     * @return
     */
    public abstract SCXMLExecutor getCurrentExecutor(FacesContext context);

    /**
     *
     * @param context
     */
    public abstract void closeAll(FacesContext context);

    /**
     *
     * @param context
     * @param to
     */
    public abstract void close(FacesContext context, SCXMLExecutor to);

    /**
     *
     * @param context
     * @return
     * @throws ModelException
     */
    public SCXML findMainStateMachine(FacesContext context) throws ModelException {
        return getStateMachine(context, (String) null, STATE_CHART_FACET_NAME, DEFAULT_STATE_MACHINE_NAME);
    }

    /**
     *
     * @param context
     * @param id
     * @return
     * @throws ModelException
     */
    public SCXML findStateMachine(FacesContext context, String id) throws ModelException {
        return getStateMachine(context, (String) null, STATE_CHART_FACET_NAME, id);
    }

    /**
     *
     * @param context
     * @param viewId
     * @return
     * @throws ModelException
     */
    public SCXML getStateMachine(FacesContext context, String viewId) throws ModelException {
        return getStateMachine(context, viewId, STATE_CHART_FACET_NAME, DEFAULT_STATE_MACHINE_NAME);
    }

    /**
     *
     * @param context
     * @param viewId
     * @param id
     * @return
     * @throws ModelException
     */
    public SCXML getStateMachine(FacesContext context, String viewId, String id) throws ModelException {
        return getStateMachine(context, viewId, STATE_CHART_FACET_NAME, id);
    }

    /**
     *
     * @param context
     * @param viewId
     * @param continerName
     * @param id
     * @return
     * @throws ModelException
     */
    public abstract SCXML getStateMachine(FacesContext context, String viewId, String continerName, String id) throws ModelException;

    /**
     *
     * @param context
     * @param url
     * @param continerName
     * @param id
     * @return
     * @throws ModelException
     */
    public abstract SCXML getStateMachine(FacesContext context, URL url, String continerName, String id) throws ModelException;

    /**
     *
     * @param context
     * @return
     */
    public abstract ELContext getELContext(FacesContext context);

    /**
     *
     * @param context
     */
    public abstract void writeState(FacesContext context);

    public abstract void executorEntered(SCXMLExecutor executor);

    public abstract void executorExited(SCXMLExecutor executor);

}
