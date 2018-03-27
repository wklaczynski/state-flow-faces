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

import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostRestoreStateEvent;
import org.apache.common.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartRoot extends UIFlowBase {

    public static final String COMPONENT_TYPE = "org.apache.common.faces.UIStateChartRoot";

    enum PropertyKeys {
        stateChart
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartRoot() {
        super();
        setRendered(false);
        setTransient(true);

        addFacesListener((ComponentSystemEventListener) (ComponentSystemEvent event) -> {
            if (event instanceof PostRestoreStateEvent) {
                postRestoreState();
            }
        });
    }

    public SCXML getStateChart() {
        return (SCXML) getStateHelper().eval(PropertyKeys.stateChart);
    }

    public void setStateChart(SCXML stateChart) {
        getStateHelper().put(PropertyKeys.stateChart, stateChart);
    }

    private void postRestoreState() {



    }
    
}
