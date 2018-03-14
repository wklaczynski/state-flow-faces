/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.jsf;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartRoot;
import javax.faces.state.model.StateChart;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.state.FlowConstants.SKIP_START_STATE_MACHINE_HINT;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class BasicViewMetadataImpl extends ViewMetadata {

    private final ViewMetadata wrapped;
    private final String viewId;

    public BasicViewMetadataImpl(ViewMetadata wrapped) {
        this.wrapped = wrapped;
        this.viewId = wrapped.getViewId();
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    @Override
    public UIViewRoot createMetadataView(FacesContext context) {
        UIViewRoot viewRoot = wrapped.createMetadataView(context);

        Boolean skip = (Boolean) context.getAttributes().get(SKIP_START_STATE_MACHINE_HINT);
        if (skip != null && skip) {
            return viewRoot;
        }

        StateChart stateChart = null;

        UIComponent facet = viewRoot.getFacet(StateChart.STATECHART_FACET_NAME);
        if (facet != null) {
            UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent("main");
            if (uichart != null) {
                stateChart = uichart.getStateChart();
            }
        }

        if (stateChart != null) {
            viewRoot = startStateMachine(context, viewId, stateChart);
        }

        return viewRoot;
    }

    public UIViewRoot startStateMachine(FacesContext context, String viewId, StateChart stateFlow) {
        UIViewRoot result = null;
        UIViewRoot currentViewRoot = context.getViewRoot();
        Map<String, Object> currentViewMapShallowCopy = Collections.emptyMap();

        try {
            context.setProcessingEvents(false);

            StateFlowHandler flowHandler = StateFlowHandler.getInstance();

            Flash flash = context.getExternalContext().getFlash();
            Map<String, Object> params = new LinkedHashMap<>();
            Set<String> keySet = flash.keySet();
            for (String key : keySet) {
                params.put(key, flash.get(key));
            }
            Map<String, String> pmap = context.getExternalContext().getRequestParameterMap();
            for (String key : pmap.keySet()) {
                params.put(key, pmap.get(key));
            }

            UIViewRoot scxmlRoot = new UIViewRoot();
            scxmlRoot.setViewId(viewId);
            UIViewRoot oldRoot = context.getViewRoot();
            try {
                if (context.getViewRoot() == null) {
                    context.setViewRoot(scxmlRoot);
                }

                flowHandler.startExecutor(context, stateFlow, params, false);
            } finally {
                if (oldRoot != null) {
                    context.setViewRoot(oldRoot);
                }
            }
            result = context.getViewRoot();

            if (null != currentViewRoot) {
                Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                if (null != currentViewMap && !currentViewMap.isEmpty()) {
                    currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                    Map<String, Object> resultViewMap = result.getViewMap(true);
                    resultViewMap.putAll(currentViewMapShallowCopy);
                }
            }

            // Only replace the current context's UIViewRoot if there is 
            // one to replace.
            if (null != currentViewRoot) {
                // This clear's the ViewMap of the current UIViewRoot before
                // setting the argument as the new UIViewRoot.
                context.setViewRoot(result);
            }

        } finally {
            context.setProcessingEvents(true);
            if (null != currentViewRoot) {
                context.setViewRoot(currentViewRoot);
                if (!currentViewMapShallowCopy.isEmpty()) {
                    currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                    currentViewMapShallowCopy.clear();
                }
            }

        }

        return result;
    }

}
