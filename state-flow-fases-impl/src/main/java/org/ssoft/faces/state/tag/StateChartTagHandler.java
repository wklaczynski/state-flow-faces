/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.state.PathResolver;
import javax.faces.state.component.UIStateChartRoot;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandler;
import org.ssoft.faces.state.impl.FacesURLResolver;
import org.ssoft.faces.state.impl.VariableMapperWrapper;
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

        StateChart chart = getElement(parent, StateChart.class);
        if (chart != null) {
            throw new TagException(this.tag, "can not instance new chart in other chart!");
        }

        UIComponent facetComponent = null;
        if (root.getFacetCount() > 0) {
            facetComponent = root.getFacets().get(StateChart.STATECHART_FACET_NAME);
        }

        UIStateChartRoot uichart = null;

        String chartId = "main";
        if (id != null) {
            chartId = id.getValue();
        }

        if (facetComponent != null) {
            uichart = (UIStateChartRoot) facetComponent.findComponent(chartId);
        }

        if (uichart != null && uichart.getStateChart() != null) {
            return;
        }

        chart = new StateChart(chartId, root.getViewId());
        if (initial != null) {
            chart.setInitial(initial.getValue(ctx));
        }

        FunctionMapper functionMapper = ctx.getFunctionMapper();
        chart.setFunctionMapper(functionMapper);

        Application app = ctx.getFacesContext().getApplication();
        if (facetComponent == null && !(facetComponent instanceof UIPanel)) {
            UIComponent panelGroup = app.createComponent(UIPanel.COMPONENT_TYPE);
            if (facetComponent != null) {
                panelGroup.getChildren().add(facetComponent);
            }
            root.getFacets().put(StateChart.STATECHART_FACET_NAME, panelGroup);
            facetComponent = panelGroup;
        }
        if (null != facetComponent) {
            facetComponent.setId(StateChart.STATECHART_FACET_NAME);
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

        PathResolver resolver = baseResolver.getResolver(fc, root.getViewId());

        Map<Object, Tag> tags = new HashMap<>();
        VariableMapper orig = ctx.getVariableMapper();
        ctx.setVariableMapper(new VariableMapperWrapper(orig));
        try {
            build(ctx, uichart, chart, tags, resolver);
        } finally {
            ctx.setVariableMapper(orig);
        }

        ModelUpdater updater = new ModelUpdater(tags);
        updater.updateSCXML(chart);

    }

    protected void build(FaceletContext ctx, UIComponent parent, StateChart chart, Map<Object, Tag> tags, PathResolver resolver) throws IOException {
        pushElement(parent, TAG_MAP, tags);
        pushElement(parent, StateChart.class, chart);
        pushElement(parent, PathResolver.class, resolver);
        pushElement(parent, CURRENT_FLOW_OBJECT, chart);
        try {
            this.nextHandler.apply(ctx, parent);
        } finally {
            popElement(parent, CURRENT_FLOW_OBJECT);
            popElement(parent, PathResolver.class);
            popElement(parent, StateChart.class);
            popElement(parent, TAG_MAP);
        }
    }

}
