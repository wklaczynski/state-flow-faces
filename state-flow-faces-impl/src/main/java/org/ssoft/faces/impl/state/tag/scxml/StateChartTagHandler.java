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
package org.ssoft.faces.impl.state.tag.scxml;

import com.sun.faces.el.ELContextImpl;
import jakarta.el.ELContext;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.state.scxml.PathResolver;
import javax.faces.state.component.UIStateChartMachine;
import javax.faces.state.scxml.model.SCXML;
import org.ssoft.faces.impl.state.StateFlowURLResolver;
import org.ssoft.faces.impl.state.el.VariableMapperWrapper;
import org.ssoft.faces.impl.state.log.FlowLogger;
import static org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler.CURRENT_FLOW_OBJECT;
import static org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler.TAG_MAP;
import static org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler.getElement;
import static org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler.popElement;
import static org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler.pushElement;
import org.ssoft.faces.impl.state.tag.ModelUpdater;
import static javax.faces.state.StateFlow.BUILD_STATE_CONTINER_HINT;
import static javax.faces.state.StateFlow.BUILD_STATE_MACHINE_HINT;
import static javax.faces.state.StateFlow.CUSTOM_ACTIONS_HINT;
import static javax.faces.state.StateFlow.CUSTOM_INVOKERS_HINT;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.model.CustomAction;
import static javax.faces.state.StateFlow.DEFAULT_STATE_MACHINE_NAME;
import static javax.faces.state.StateFlow.DISABLE_EXPRESSION_MAP;
import static javax.faces.state.StateFlow.STATE_CHART_FACET_NAME;

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
            stateContinerName = STATE_CHART_FACET_NAME;
        }

        UIComponent facetComponent = null;
        if (root.getFacetCount() > 0) {
            facetComponent = parent.getFacets().get(stateContinerName);
        }

        UIStateChartMachine uichart = null;

        String chartId = DEFAULT_STATE_MACHINE_NAME;
        if (id != null) {
            chartId = id.getValue();
        }

        String buildId = (String) ctx.getFacesContext().getAttributes().get(BUILD_STATE_MACHINE_HINT);
        if (buildId != null && !buildId.equals(chartId)) {
            return;
        }

        if (facetComponent != null) {
            uichart = (UIStateChartMachine) facetComponent.findComponent(chartId);
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
            uichart = (UIStateChartMachine) app.createComponent(UIStateChartMachine.COMPONENT_TYPE);
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

        String path = getPath(ctx, root, parent);

        PathResolver resolver = baseResolver.getResolver(path);
        chart.setPathResolver(resolver);

        FunctionMapper forig = ctx.getFunctionMapper();

        VariableMapper corig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(corig));

        StateFlowHandler handler = StateFlowHandler.getInstance();
        List<CustomAction> customActions = handler.getCustomActions();
        Map<String, Class<? extends Invoker>> customInvokers = handler.getCustomInvokers();

        Map<Object, Tag> tags = new HashMap<>();
        fc.getAttributes().put(DISABLE_EXPRESSION_MAP, true);
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
            fc.getAttributes().remove(DISABLE_EXPRESSION_MAP);
        }

        chart.getMetadata().put("faces-tag-info", new HashMap<>(tags));

        ModelUpdater updater = new ModelUpdater(tags);
        updater.updateSCXML(chart);

        restored = chart;

    }

    public String getPath(FaceletContext ctx, UIViewRoot root, UIComponent parent) {
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
