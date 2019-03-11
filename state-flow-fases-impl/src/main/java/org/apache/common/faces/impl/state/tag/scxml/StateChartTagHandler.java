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
package org.apache.common.faces.impl.state.tag.scxml;

import com.sun.faces.el.ELContextImpl;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.Facelet;
import org.apache.common.scxml.PathResolver;
import org.apache.common.faces.state.component.UIStateChartDefinition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.faces.impl.state.StateFlowURLResolver;
import org.apache.common.faces.impl.state.el.VariableMapperWrapper;
import org.apache.common.faces.impl.state.log.FlowLogger;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.CURRENT_FLOW_OBJECT;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.TAG_MAP;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.getElement;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.popElement;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.pushElement;
import org.apache.common.faces.impl.state.tag.ModelUpdater;
import static org.apache.common.faces.state.StateFlow.BUILD_STATE_CONTINER_HINT;
import static org.apache.common.faces.state.StateFlow.BUILD_STATE_MACHINE_HINT;
import static org.apache.common.faces.state.StateFlow.CUSTOM_ACTIONS_HINT;
import static org.apache.common.faces.state.StateFlow.CUSTOM_INVOKERS_HINT;
import static org.apache.common.faces.state.StateFlow.DEFAULT_STATECHART_NAME;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.model.CustomAction;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChartTagHandler extends TagHandler {

    /**
     *
     */
    public static final String KEY = "facelets.STATECHART_FACET_NAME";

    /**
     *
     */
    public static final String BASE_PATH_RESOLVER = "facelets.stateflow.BASE_PATH_RESOLVER";

    private static final Logger LOGGER = FlowLogger.TAGLIB.getLogger();

    /**
     *
     */
    protected final TagAttribute id;

    /**
     *
     */
    protected final TagAttribute initial;

    private static SCXML restored;

    /**
     *
     * @param config
     */
    public StateChartTagHandler(TagConfig config) {
        super(config);
        this.id = this.getAttribute("id");
        this.initial = this.getRequiredAttribute("initial");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        boolean inline = false;
        UIViewRoot root;
        if (parent instanceof UIViewRoot) {
            root = (UIViewRoot) parent;
        } else {
            inline = true;
            root = ctx.getFacesContext().getViewRoot();
        }
        if (root == null) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "jsf.statechart.uiviewroot.unavailable");
            }
            return;
        }

        SCXML chart = getElement(parent, SCXML.class);
        if (chart != null) {
            throw new TagException(this.tag, "can not instance new chart in other chart!");
        }

        String stateContinerName = (String) ctx.getFacesContext().getAttributes().get(BUILD_STATE_CONTINER_HINT);
        if (stateContinerName == null) {
            stateContinerName = STATECHART_FACET_NAME;
        }
        
        UIComponent facetComponent = null;
        if (root.getFacetCount() > 0) {
            facetComponent = parent.getFacets().get(stateContinerName);
        }

        UIStateChartDefinition uichart = null;

        String chartId = DEFAULT_STATECHART_NAME;
        if (id != null) {
            chartId = id.getValue();
        }

        String buildId = (String) ctx.getFacesContext().getAttributes().get(BUILD_STATE_MACHINE_HINT);
        if (buildId != null && !buildId.equals(chartId)) {
            return;
        }

        if (facetComponent != null) {
            uichart = (UIStateChartDefinition) facetComponent.findComponent(chartId);
        }

        Application app = ctx.getFacesContext().getApplication();

        if (uichart != null && uichart.getStateChart() != null) {
            return;
        }

        chart = new SCXML();
        if (initial != null) {
            chart.setInitial(initial.getValue(ctx));
        }
        chart.setDatamodelName(tag.getNamespace());

        String viewId = root.getViewId();
        chart.setViewId(viewId);

        String qname = tag.getQName();
        String typens = "";

        int sep = qname.indexOf(":");
        if (sep > 0) {
            typens = qname.substring(0, sep);
        }

        chart.setNamespaces(new LinkedHashMap<>());
        chart.getNamespaces().put(typens, tag.getNamespace());

        chart.getMetadata().put("faces-viewid", root.getViewId());
        chart.getMetadata().put("faces-chartid", chartId);

        if (facetComponent == null && !(facetComponent instanceof UIPanel)) {
            UIComponent panelGroup = app.createComponent(UIPanel.COMPONENT_TYPE);
            if (facetComponent != null) {
                panelGroup.getChildren().add(facetComponent);
            }
            parent.getFacets().put(stateContinerName, panelGroup);
            facetComponent = panelGroup;
            
            if (null != facetComponent) {
                facetComponent.setId(stateContinerName);
            }
        }

        if (uichart == null) {
            uichart = (UIStateChartDefinition) app.createComponent(UIStateChartDefinition.COMPONENT_TYPE);
            uichart.setId(chartId);
            facetComponent.getChildren().add(uichart);
        }

        FacesContext fc = ctx.getFacesContext();
        PathResolver baseResolver = (PathResolver) fc.getExternalContext().getApplicationMap().get(BASE_PATH_RESOLVER);
        if (baseResolver == null) {
            baseResolver = new StateFlowURLResolver("/");
            fc.getExternalContext().getApplicationMap().put(BASE_PATH_RESOLVER, baseResolver);
        }

        uichart.setStateChart(chart);

        String path = getPath(ctx, root);

        PathResolver resolver = baseResolver.getResolver(path);
        chart.setPathResolver(resolver);

        FunctionMapper forig = ctx.getFunctionMapper();

        VariableMapper corig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(corig));

        StateFlowHandler handler = StateFlowHandler.getInstance();
        List<CustomAction> customActions = handler.getCustomActions();
        Map<String, Class<? extends Invoker>> customInvokers = handler.getCustomInvokers();

        Map<Object, Tag> tags = new HashMap<>();
        pushElement(parent, CUSTOM_ACTIONS_HINT, customActions);
        pushElement(parent, CUSTOM_INVOKERS_HINT, customInvokers);
        pushElement(parent, TAG_MAP, tags);
        pushElement(parent, SCXML.class, chart);
        pushElement(parent, PathResolver.class, resolver);
        pushElement(parent, CURRENT_FLOW_OBJECT, chart);
        try {
            this.nextHandler.apply(ctx, parent);
        } finally {
            popElement(parent, CURRENT_FLOW_OBJECT);
            popElement(parent, PathResolver.class);
            popElement(parent, SCXML.class);
            popElement(parent, TAG_MAP);
            popElement(parent, CUSTOM_INVOKERS_HINT);
            popElement(parent, CUSTOM_ACTIONS_HINT);
            ctx.setVariableMapper(corig);
            ctx.setFunctionMapper(forig);
        }

        chart.getMetadata().put("faces-tag-info", new HashMap<>(tags));

        ModelUpdater updater = new ModelUpdater(tags);
        updater.updateSCXML(chart);

        restored = chart;

    }

    public String getPath(FaceletContext ctx, UIViewRoot root) {

        String path = root.getViewId();

        try {
            Field ffield = ctx.getClass().getDeclaredField("facelet");
            boolean faccessible = ffield.isAccessible();
            try {
                ffield.setAccessible(true);
                Facelet facelet = (Facelet) ffield.get(ctx);
                Field sfield = facelet.getClass().getDeclaredField("src");
                boolean saccessible = sfield.isAccessible();
                try {
                    sfield.setAccessible(true);
                    URL url = (URL) sfield.get(facelet);
                    path = "/" + url.getPath();
                } finally {
                    sfield.setAccessible(saccessible);
                }

            } finally {
                ffield.setAccessible(faccessible);
            }

        } catch (Throwable ex) {
        }

        return path;
    }

    private void pushMapper(FacesContext ctx, FunctionMapper mapper) {
        ELContext elContext = ctx.getELContext();
        if (elContext instanceof ELContextImpl) {
            ((ELContextImpl) elContext).setFunctionMapper(mapper);
        }
    }

}
