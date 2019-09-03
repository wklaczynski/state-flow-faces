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
package javax.faces.state.component;

import javax.faces.component.UIPanel;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartExecutor extends UIPanel {

    private transient String _executorId;

    public static final String SCXML_URL = "javax.faces.state.component.SCXML_URL";

    public static final String SCXML_NAME = "javax.faces.state.component.SCXML_NAME";
    
    
    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_FAMILY = "javax.faces.state.StateFlow";

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_TYPE = "javax.faces.state.UIStateChartExecutor";

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

    public void setExecutorId(String _executorId) {
        this._executorId = _executorId;
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
    
}
