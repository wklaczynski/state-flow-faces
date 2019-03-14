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

import javax.faces.component.UIComponentBase;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostRestoreStateEvent;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartDefinition extends UIComponentBase {

    /**
     *
     */
    public static final String COMPONENT_FAMILY = "org.apache.common.faces.StateFlow";
    
    /**
     *
     */
    public static final String COMPONENT_TYPE = "org.apache.common.faces.UIStateChartDefinition";

    enum PropertyKeys {
        stateChart
    }

    /**
     *
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartDefinition() {
        super();
        setRendererType(null);
        setTransient(false);
        setRendered(false);

        addFacesListener((ComponentSystemEventListener) (ComponentSystemEvent event) -> {
            if (event instanceof PostRestoreStateEvent) {
                postRestoreState();
            }
        });
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

    private void postRestoreState() {
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    
}
