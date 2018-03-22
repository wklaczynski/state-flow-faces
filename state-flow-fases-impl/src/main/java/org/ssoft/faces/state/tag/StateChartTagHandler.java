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
package org.ssoft.faces.state.tag;

import com.sun.faces.el.ELContextImpl;
import java.io.IOException;
import java.util.HashMap;
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
import javax.scxml.PathResolver;
import javax.faces.state.component.UIStateChartRoot;
import static javax.faces.state.faces.StateFlowHandler.BUILD_STATE_MACHINE_HINT;
import static javax.faces.state.faces.StateFlowHandler.DEFAULT_STATECHART_NAME;
import static javax.faces.state.faces.StateFlowHandler.STATECHART_FACET_NAME;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;
import javax.scxml.model.SCXML;
import org.ssoft.faces.state.FacesURLResolver;
import org.ssoft.faces.state.el.VariableMapperWrapper;
import org.ssoft.faces.state.log.FlowLogger;
import static org.ssoft.faces.state.tag.AbstractFlowTagHandler.CURRENT_FLOW_OBJECT;
import static org.ssoft.faces.state.tag.AbstractFlowTagHandler.TAG_MAP;
import static org.ssoft.faces.state.tag.AbstractFlowTagHandler.getElement;
import static org.ssoft.faces.state.tag.AbstractFlowTagHandler.popElement;
import static org.ssoft.faces.state.tag.AbstractFlowTagHandler.pushElement;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChartTagHandler extends TagHandler {

    public static final String KEY = "facelets.STATECHART_FACET_NAME";
    public static final String BASE_PATH_RESOLVER = "facelets.stateflow.BASE_PATH_RESOLVER";

    private static final Logger LOGGER = FlowLogger.TAGLIB.getLogger();

    protected final TagAttribute id;
    protected final TagAttribute initial;

    public StateChartTagHandler(TagConfig config) {
        super(config);
        this.id = this.getAttribute("id");
        this.initial = this.getRequiredAttribute("initial");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        UIViewRoot root;
        if (parent instanceof UIViewRoot) {
            root = (UIViewRoot) parent;
        } else {
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

        UIComponent facetComponent = null;
        if (root.getFacetCount() > 0) {
            facetComponent = root.getFacets().get(STATECHART_FACET_NAME);
        }

        UIStateChartRoot uichart = null;

        String chartId = DEFAULT_STATECHART_NAME;
        if (id != null) {
            chartId = id.getValue();
        }

        String buildId = (String) ctx.getFacesContext().getAttributes().get(BUILD_STATE_MACHINE_HINT);
        if (buildId != null && !buildId.equals(chartId)) {
            return;
        }

        if (facetComponent != null) {
            uichart = (UIStateChartRoot) facetComponent.findComponent(chartId);
        }

        if (uichart != null && uichart.getStateChart() != null) {
            return;
        }

        chart = new SCXML();
        if (initial != null) {
            chart.setInitial(initial.getValue(ctx));
        }
        chart.setDatamodelName(tag.getNamespace());

        Application app = ctx.getFacesContext().getApplication();
        if (facetComponent == null && !(facetComponent instanceof UIPanel)) {
            UIComponent panelGroup = app.createComponent(UIPanel.COMPONENT_TYPE);
            if (facetComponent != null) {
                panelGroup.getChildren().add(facetComponent);
            }
            root.getFacets().put(STATECHART_FACET_NAME, panelGroup);
            facetComponent = panelGroup;
        }
        if (null != facetComponent) {
            facetComponent.setId(STATECHART_FACET_NAME);
        }

        if (uichart == null) {
            uichart = (UIStateChartRoot) app.createComponent(UIStateChartRoot.COMPONENT_TYPE);
            uichart.setId(chartId);
            facetComponent.getChildren().add(uichart);
        }
        uichart.setStateChart(chart);

        FacesContext fc = ctx.getFacesContext();
        PathResolver baseResolver = (PathResolver) fc.getExternalContext().getApplicationMap().get(BASE_PATH_RESOLVER);
        if (baseResolver == null) {
            baseResolver = new FacesURLResolver("/");
            fc.getExternalContext().getApplicationMap().put(BASE_PATH_RESOLVER, baseResolver);
        }

        PathResolver resolver = baseResolver.getResolver(root.getViewId());
        chart.setPathResolver(resolver);

        FunctionMapper forig = ctx.getFunctionMapper();

        VariableMapper corig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(corig));

        Map<Object, Tag> tags = new HashMap<>();
        try {
            build(ctx, uichart, chart, tags, resolver);
        } finally {
            ctx.setVariableMapper(corig);
            ctx.setFunctionMapper(forig);
        }

        chart.setTags(new HashMap<>(tags));

        ModelUpdater updater = new ModelUpdater(tags);
        updater.updateSCXML(chart);

    }

    protected void build(FaceletContext ctx, UIComponent parent, SCXML chart, Map<Object, Tag> tags, PathResolver resolver) throws IOException {
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
        }
    }

    private void pushMapper(FacesContext ctx, FunctionMapper mapper) {
        ELContext elContext = ctx.getELContext();
        if (elContext instanceof ELContextImpl) {
            ((ELContextImpl) elContext).setFunctionMapper(mapper);
        }
    }

}
