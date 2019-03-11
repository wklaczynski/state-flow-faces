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

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import static org.apache.common.faces.state.StateFlow.DEFAULT_STATECHART_NAME;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.model.CustomAction;
import org.apache.common.scxml.model.ModelException;
import org.apache.common.scxml.model.SCXML;

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
        return handler;
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
    public abstract Map<String, Class<? extends Invoker>> getCustomInvokers();

    
    /**
     *
     * @param context
     * @return
     */
    public abstract Context getFlowContext(FacesContext context);
            
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
    public abstract SCXMLExecutor getRootExecutor(FacesContext context, String executorId);

    /**
     *
     * @param context
     * @param executor
     */
    public void pushRootExecutor(FacesContext context, SCXMLExecutor executor) {
        pushRootExecutor(context, executor, null);
    }

    /**
     *
     * @param context
     * @param executor
     * @param viewId
     */
    public abstract void pushRootExecutor(FacesContext context, SCXMLExecutor executor, String viewId);

    /**
     *
     * @param context
     * @param executor
     */
    public void popRootExecutor(FacesContext context, SCXMLExecutor executor) {
        pushRootExecutor(context, executor, null);
    }

    /**
     *
     * @param context
     * @param executor
     * @param viewId
     */
    public abstract void popRootExecutor(FacesContext context, SCXMLExecutor executor, String viewId);

    
    /**
     *
     * @param context
     * @return
     */
    public abstract boolean isActive(FacesContext context);

    /**
     *
     * @param context
     * @return
     */
    public abstract boolean isFinal(FacesContext context);

    /**
     *
     * @param context
     * @return
     */
    public abstract boolean isInWindow(FacesContext context);

    /**
     *
     * @param id
     * @param context
     * @param scxml
     * @return
     * @throws ModelException
     */
    public abstract SCXMLExecutor createRootExecutor(String id, FacesContext context, SCXML scxml) throws ModelException;

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
    public void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params) {
        execute(context, executor, params, false);
    }

    /**
     *
     * @param context
     * @param executor
     * @param params
     * @param inline
     */
    public abstract void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params, boolean inline);

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
    public void close(FacesContext context) {
        close(context, null);
    }

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
        return getStateMachine(context, (String)null, STATECHART_FACET_NAME,  DEFAULT_STATECHART_NAME);
    }
    
    
    /**
     *
     * @param context
     * @param id
     * @return
     * @throws ModelException
     */
    public SCXML findStateMachine(FacesContext context, String id) throws ModelException {
        return getStateMachine(context, (String)null, STATECHART_FACET_NAME,  id);
    }
    
    
    /**
     *
     * @param context
     * @param viewId
     * @return
     * @throws ModelException
     */
    public SCXML getStateMachine(FacesContext context, String viewId) throws ModelException {
        return getStateMachine(context, viewId, STATECHART_FACET_NAME,  DEFAULT_STATECHART_NAME);
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
        return getStateMachine(context, viewId, STATECHART_FACET_NAME,  id);
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
     */
    public abstract void writeState(FacesContext context);

    public abstract void executorEntered(SCXMLExecutor executor);

    public abstract void executorExited(SCXMLExecutor executor);

}
