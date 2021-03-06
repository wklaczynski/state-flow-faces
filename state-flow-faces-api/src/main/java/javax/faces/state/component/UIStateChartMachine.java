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

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartMachine extends UIComponentBase {

    /**
     *
     */
    public static final String COMPONENT_FAMILY = "javax.faces.state.StateFlow";

    /**
     *
     */
    public static final String COMPONENT_TYPE = "javax.faces.state.UIStateChartMachine";

    enum PropertyKeys {
        stateChart
    }

    /**
     *
     */
    @SuppressWarnings({"OverridableMethodCallInConstructor", "Convert2Lambda"})
    public UIStateChartMachine() {
        super();
        setRendererType(null);
        setTransient(false);
        setRendered(false);
    }

    /**
     *
     * @return
     */
    public SCXML getStateChart() {
        return (SCXML) getStateHelper().eval(PropertyKeys.stateChart);
    }

    /**
     *
     * @param stateChart
     */
    public void setStateChart(SCXML stateChart) {
        getStateHelper().put(PropertyKeys.stateChart, stateChart);
    }

    @Override
    public boolean isTransient() {
        return true;
    }
    
    @Override
    public void restoreState(FacesContext context, Object state) {

    }

    @Override
    public Object saveState(FacesContext context) {
        return null;
    }
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    
}
