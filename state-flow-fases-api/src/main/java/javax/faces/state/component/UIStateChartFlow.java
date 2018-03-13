/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.component;

import javax.faces.component.UIComponentBase;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostRestoreStateEvent;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartFlow extends UIComponentBase {

    // ------------------------------------------------------ Manifest Constants
    /**
     * <p>
     * The standard component type for this component.</p>
     */
    public static final String COMPONENT_TYPE = "javax.faces.UIStateChartFlow";

    /**
     * <p>
     * The standard component family for this component.</p>
     */
    public static final String COMPONENT_FAMILY = "javax.faces.UIStateChartFlow";

    enum PropertyKeys {
        stateChart
    }

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartFlow() {
        super();
        setRendererType(null);
        setRendered(false);
        setTransient(false);

        addFacesListener((ComponentSystemEventListener) (ComponentSystemEvent event) -> {
            if (event instanceof PostRestoreStateEvent) {
                postRestoreState();
            }
        });
    }

    @Override
    public String getFamily() {
        return (COMPONENT_FAMILY);
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
