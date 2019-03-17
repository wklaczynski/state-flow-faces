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
package javax.faces.state.component;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.Location;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import static javax.faces.state.StateFlow.RENDER_EXECUTOR_FACET;
import javax.faces.state.utils.ComponentUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartFacetRender extends UIPanel {

    private String _executorId;
    private String _path;

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_FAMILY = "javax.faces.state.StateFlow";

    /**
     *
     */
    @SuppressWarnings("FieldNameHidesFieldInSuperclass")
    public static final String COMPONENT_TYPE = "javax.faces.state.UIStateChartFacetRender";

    enum PropertyKeys {
        slot
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

    public String getInvokePath(FacesContext context) {
        if(_path == null) {
            String viewId = context.getViewRoot().getViewId();
            _path = viewId + "!" + _executorId + ":" + getSlot();
        }
        return _path;
    }

    public String getSlot() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.slot, "content");
    }

    public void setSlot(java.lang.String _slot) {
        getStateHelper().put(PropertyKeys.slot, _slot);
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
            String slot = getSlot();
            String source = (String) sctx.get(RENDER_EXECUTOR_FACET.get(slot));
            if (source != null) {
                UIStateChartExecutor controller = ComponentUtils.assigned(UIStateChartExecutor.class, this);
                if (source.startsWith("@renderer:")) {
                    String name = source.substring(10);
                    facet = getFacet(name);
                    if (facet == null) {
                        throwRequiredRendererFacetException(context, name);
                    }
                } else if (source.startsWith("@executor:")) {
                    if (controller == null) {
                        throwNoInController(context, "@executor");
                    }

                    String name = source.substring(10);

                    facet = controller.getFacet(name);
                    
                    if (facet == null) {
                        throwRequiredExecutorFacetException(context, name);
                    }
                } else if (source.startsWith("@viewroot:")) {
                    if (controller != null) {
                        throwInControler(context, "@executor");
                    }

                    String name = source.substring(10);
                    UIViewRoot view = context.getViewRoot();
                    if (view != null) {
                        facet = view.getFacet(name);
                    }

                    if (facet == null) {
                        throwRequiredViewFacetException(context, name);
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

    private void throwRequiredRendererFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in renderer component "
                + " with id \"" + getClientId(ctx) + "\"");

    }

    private void throwRequiredExecutorFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in executor component "
                + " with id \"" + getClientId(ctx) + "\"");

    }

    private void throwRequiredViewFacetException(FacesContext ctx, String name) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to find facet named \"" + name + "\" in view root "
                + " with id \"" + getClientId(ctx) + "\"");

    }

    private void throwNoInController(FacesContext ctx, String type) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to render facet slot type \"" + type + "\" in view root executor "
                + " with id \"" + getClientId(ctx) + "\"");
        

    }

    private void throwInControler(FacesContext ctx, String type) {

        Location location = (Location) getAttributes().get(UIComponent.VIEW_LOCATION_KEY);

        throw new FacesException(
                location + " "
                + "unable to render facet slot type \"" + type + "\" in controller executor "
                + " with id \"" + getClientId(ctx) + "\"");

    }

    
}
