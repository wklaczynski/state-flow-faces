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

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.view.Location;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_FACET;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartFacetRender extends UIPanel {

    private String _executorId;

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_FAMILY = "org.apache.common.faces.StateFlow";

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_TYPE = "org.apache.common.faces.UIStateChartFacetRender";

    enum PropertyKeys {
        slot,
        required,
    }

    /**
     *
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartFacetRender() {
        super();
        setRendererType(null);
        setTransient(false);
        setRendered(true);
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public String getExecutorId() {
        return _executorId;
    }

    public void setExecutorId(String executorId) {
        this._executorId = executorId;
    }

    public String getSlot() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.slot, "content");
    }

    public void setSlot(java.lang.String _slot) {
        getStateHelper().put(PropertyKeys.slot, _slot);
    }

    public boolean isRequired() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.required, true);
    }

    public void setRequired(boolean _required) {
        getStateHelper().put(PropertyKeys.required, _required);
    }

    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        UIComponent renderComponent = getCurentRenderComponent(context);
        if (renderComponent != null) {
            renderComponent.encodeAll(context);
        }
        super.encodeEnd(context);
    }

    public UIComponent getCurentRenderComponent(FacesContext context) {
        UIComponent facet = null;

        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = getExecutorId();
        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor != null) {
            Context sctx = executor.getRootContext();
            String source = (String) sctx.get(FACES_CHART_FACET);
            if (source != null) {
                if (source.startsWith("@renderer:")) {
                    String name = source.substring(10);
                    facet = getFacet(name);
                    if (facet == null) {
                        throwRequiredFacetException(context, name);
                    }
                } else if (source.startsWith("@cc:")) {
                    String name = source.substring(4);
                    facet = getFacet(name);
                    if (facet == null) {
                        throwRequiredFacetException(context, name);
                    }
                }
            }
        }
        return facet;
    }

    public UIComponent getRenderNamingContainer(FacesContext context) {
        UIComponent namingContainer = getCurentRenderComponent(context);
        while (namingContainer != null) {
            if (namingContainer instanceof NamingContainer) {
                return namingContainer;
            }
            namingContainer = namingContainer.getParent();
        }
        return null;
    }

    private void throwRequiredFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in controller component "
                + " with id \"" + getClientId(ctx) + "\"");

    }
    
}
