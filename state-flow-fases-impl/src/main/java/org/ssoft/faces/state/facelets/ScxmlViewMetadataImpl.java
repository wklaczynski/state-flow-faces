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
package org.ssoft.faces.state.facelets;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.state.StateFlowHandler;
import static javax.faces.state.StateFlowHandler.DEFAULT_STATECHART_NAME;
import static javax.faces.state.StateFlowHandler.SKIP_START_STATE_MACHINE_HINT;
import javax.faces.state.component.UIStateChartRoot;
import javax.faces.state.model.StateChart;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.state.FlowConstants.STATE_CHART_DEFAULT_PARAM_NAME;
import static org.ssoft.faces.state.FlowConstants.STATE_CHART_REQUEST_PARAM_NAME;
import org.ssoft.faces.state.config.StateWebConfiguration;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ScxmlViewMetadataImpl extends ViewMetadata {

    private final ViewDeclarationLanguage vdl;
    private final String viewId;

    public ScxmlViewMetadataImpl(ViewDeclarationLanguage vdl, String viewId) {
        this.viewId = viewId;
        this.vdl = vdl;
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    protected UIViewRoot createView(FacesContext context) throws IOException {
        UIViewRoot viewRoot = vdl.createView(context, viewId);
        vdl.buildView(context, viewRoot);
        return viewRoot;
    }

    @Override
    public UIViewRoot createMetadataView(FacesContext context) {
        try {
            UIViewRoot viewRoot = createView(context);

            Boolean skip = (Boolean) context.getAttributes().get(SKIP_START_STATE_MACHINE_HINT);
            if (skip != null && skip) {
                return viewRoot;
            }

            UIComponent facet = viewRoot.getFacet(StateChart.STATECHART_FACET_NAME);
            if (facet != null) {
                StateChart stateChart = null;

                
                StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
                
                String pname = wcfg.getOptionValue(STATE_CHART_REQUEST_PARAM_NAME, STATE_CHART_DEFAULT_PARAM_NAME);
                
                String flowId = null;

                if (flowId == null) {
                    flowId = context.getExternalContext().getRequestParameterMap().get(pname);
                }
                if (flowId == null) {
                    flowId = DEFAULT_STATECHART_NAME;
                }

                UIStateChartRoot uichart = (UIStateChartRoot) facet.findComponent(flowId);
                if (uichart != null) {
                    stateChart = uichart.getStateChart();
                }
                
                if (stateChart != null) {
                    viewRoot = startStateMachine(context, viewId, stateChart);
                }
            }

            return viewRoot;
        } catch (IOException e) {
            throw new FacesException(e);
        }
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
