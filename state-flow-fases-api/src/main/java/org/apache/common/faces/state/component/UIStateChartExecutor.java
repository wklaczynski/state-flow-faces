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
package org.apache.common.faces.state.component;

import javax.faces.FacesException;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.MethodBinding;
import javax.faces.el.MethodNotFoundException;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.scxml.EventBuilder;
import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.TriggerEvent;
import org.apache.common.faces.state.scxml.model.ModelException;
import org.apache.common.faces.state.utils.ComponentUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartExecutor extends UIPanel {

    public static final String CONTROLLER_FACET_NAME = "javax.faces.component.CONTROLLER_FACET_NAME";

    private String _executorId;

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_FAMILY = "org.apache.common.faces.StateFlow";

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_TYPE = "org.apache.common.faces.UIStateChartExecutor";

    enum PropertyKeys {
        name,
        required,
    }

    /**
     *
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartExecutor() {
        super();
        setRendererType(null);
        setTransient(false);
        setRendered(true);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getExecutorId() {
        return _executorId;
    }

    public void setExecutorId(String executorId) {
        this._executorId = executorId;
    }

    public String getName() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.name, null);
    }

    public void setName(java.lang.String _name) {
        getStateHelper().put(PropertyKeys.name, _name);
    }

    public boolean isRequired() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.required, true);
    }

    public void setRequired(boolean _required) {
        getStateHelper().put(PropertyKeys.required, _required);
    }

    public String getPath(FacesContext context) {
        String path = context.getViewRoot().getViewId() + "!" + getName();
        return path;
    }

    public SCXMLExecutor getRootExecutor(FacesContext context) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        return executor;
    }

}