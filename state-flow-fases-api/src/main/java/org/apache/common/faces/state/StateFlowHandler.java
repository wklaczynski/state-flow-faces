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

import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import static org.apache.common.faces.state.StateFlow.DEFAULT_STATECHART_NAME;
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

    public static final String KEY = "javax.faces.state.StateFlowHandler";

    public final static StateFlowHandler getInstance() {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = (StateFlowHandler) fc.getExternalContext().getApplicationMap().get(KEY);
        return handler;
    }

    public abstract List<CustomAction> getCustomActions();

    public abstract Map<String, Class<? extends Invoker>> getCustomInvokers();

    public abstract SCXMLExecutor getRootExecutor(FacesContext context);

    public abstract boolean isActive(FacesContext context);

    public abstract SCXMLExecutor createRootExecutor(FacesContext context, SCXML scxml) throws ModelException;
    
    public abstract SCXMLExecutor createChildExecutor(FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException;
    
    public abstract void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params);

    public abstract SCXMLExecutor getCurrentExecutor(FacesContext context);

    public void close(FacesContext context) {
        close(context, null);
    }

    public abstract void close(FacesContext context, SCXMLExecutor to);

    public SCXML createStateMachine(FacesContext context, String path) throws ModelException {
        return createStateMachine(context, path, DEFAULT_STATECHART_NAME);
    }

    public abstract SCXML createStateMachine(FacesContext context, String path, String id) throws ModelException;

    public abstract void writeState(FacesContext context);

}
