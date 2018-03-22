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

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.scxml.Context;
import javax.scxml.SCXMLExecutor;
import javax.faces.state.component.UIStateChartRoot;
import javax.faces.state.faces.StateFlowHandler;
import static javax.faces.state.faces.StateFlowHandler.DEFAULT_STATECHART_NAME;
import static javax.faces.state.faces.StateFlowHandler.SKIP_START_STATE_MACHINE_HINT;
import static javax.faces.state.faces.StateFlowHandler.STATECHART_FACET_NAME;
import javax.scxml.model.EnterableState;
import javax.scxml.model.SCXML;
import javax.scxml.model.TransitionalState;
import static org.ssoft.faces.state.FacesFlowConstants.STATE_CHART_DEFAULT_PARAM_NAME;
import static org.ssoft.faces.state.FacesFlowConstants.STATE_CHART_REQUEST_PARAM_NAME;
import org.ssoft.faces.state.config.StateWebConfiguration;
import org.ssoft.faces.state.impl.CompositeContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowPhaseListener implements PhaseListener {

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            restoreStateFlow(context);
            
            StateFlowHandler fh = StateFlowHandler.getInstance();
            SCXMLExecutor executor = fh.getExecutor(context);
            if (executor != null) {
                Context stateContext = getStateContext(context, executor);
                context.getELContext().putContext(Context.class, stateContext);
                context.getELContext().putContext(SCXMLExecutor.class, executor);
            }
        }

    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        if (event.getPhaseId() != PhaseId.RESTORE_VIEW) {
        }
        if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            StateFlowHandler.getInstance().writeState(context);
        }
    }

    private static Context getStateContext(
            final FacesContext fc,
            final SCXMLExecutor executor) {

        CompositeContext result = new CompositeContext(executor.getRootContext());
        
        Iterator<EnterableState> iterator = executor.getStatus().getActiveStates().iterator();
        while (iterator.hasNext()) {
            EnterableState enterable = iterator.next();
            if(enterable instanceof TransitionalState) {
                Context context = executor.getSCInstance().getContext(enterable);
                result.add(context);
            }
        }
        return result;
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private void restoreStateFlow(FacesContext context) {
        if (context.isPostback()) {
            return;
        }

        Boolean skip = (Boolean) context.getAttributes().get(SKIP_START_STATE_MACHINE_HINT);
        if (skip != null && skip) {
            return;
        }

        UIViewRoot viewRoot = context.getViewRoot();
        if (viewRoot == null) {
            return;
        }

        UIComponent facet = viewRoot.getFacet(STATECHART_FACET_NAME);
        if (facet != null) {
            SCXML stateChart = null;

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
                startStateMachine(context, stateChart);
            }
        }
    }

    public void startStateMachine(FacesContext context, SCXML stateFlow) {
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

            flowHandler.execute(stateFlow, params);
            UIViewRoot result = context.getViewRoot();

            if (null != currentViewRoot) {
                Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                if (null != currentViewMap && !currentViewMap.isEmpty()) {
                    currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                    Map<String, Object> resultViewMap = result.getViewMap(true);
                    resultViewMap.putAll(currentViewMapShallowCopy);
                }
            }
        } finally {
            context.setProcessingEvents(true);
            if (!currentViewMapShallowCopy.isEmpty()) {
                currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                currentViewMapShallowCopy.clear();
            }
        }

    }

}
