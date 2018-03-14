/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.component;

import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartRoot extends UIStateFlow {

    public static final String COMPONENT_TYPE = "javax.faces.UIStateChartRoot";

    enum PropertyKeys {
        stateChart
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartRoot() {
        super();
        setRendererType(null);
        setRendered(false);
        setTransient(true);

        addFacesListener((ComponentSystemEventListener) (ComponentSystemEvent event) -> {
            if (event instanceof PostRestoreStateEvent) {
                postRestoreState();
            }
        });
    }

    public StateChart getStateChart() {
        return (StateChart) getStateHelper().eval(PropertyKeys.stateChart);
    }

    public void setStateChart(StateChart stateChart) {
        getStateHelper().put(PropertyKeys.stateChart, stateChart);
    }

    private void postRestoreState() {



    }
    
}
