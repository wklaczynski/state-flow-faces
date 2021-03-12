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

import javax.faces.state.utils.ComponentUtils;
import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreValidateEvent;
import javax.faces.view.Location;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import static javax.faces.state.StateFlow.RENDER_EXECUTOR_FACET;
import javax.faces.state.StateFlowHandler;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartFacetRender extends UIPanel {

//    private transient SCXMLExecutor _executor;
    private transient String _executorId;
    private transient String _path;

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

    @Override
    public void processDecodes(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, null);

        try {
            UIComponent renderComponent = getCurentEncodeFacet(context);
            if (renderComponent != null) {
                renderComponent.processDecodes(context);
            }
        } finally {
            popComponentFromEL(context);
        }
    }

    @Override
    public void processValidators(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (!isRendered()) {
            return;
        }

        pushComponentToEL(context, null);

        try {
            Application app = context.getApplication();
            app.publishEvent(context, PreValidateEvent.class, this);
            UIComponent renderComponent = getCurentEncodeFacet(context);
            if (renderComponent != null) {
                renderComponent.processValidators(context);
            }
            app.publishEvent(context, PostValidateEvent.class, this);
        } finally {
            popComponentFromEL(context);
        }
    }

    @Override
    public void decode(FacesContext context) {

    }

    public String getExecutorId() {
        return _executorId;
    }

    public void setExecutorId(String _executorId) {
        this._executorId = _executorId;
    }

    public String getExecutePath(FacesContext context) {
        if (_path == null && _executorId != null) {
            _path = _executorId + ":" + getSlot();
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
        UIComponent renderComponent = getCurentEncodeFacet(context);
        if (renderComponent != null) {
            renderComponent.encodeAll(context);
        }
        super.encodeEnd(context);
    }

    private UIComponent getCurentEncodeFacet(FacesContext context) {
        UIComponent facet = null;
        SCXMLExecutor executor = null;
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String executorId = getExecutorId();
        if (executorId != null) {
            executor = handler.getExecutor(context, executorId);
        }

        if (executor != null) {
            Context sctx = executor.getRootContext();
            String slot = getSlot();
            String source = (String) sctx.get(RENDER_EXECUTOR_FACET.get(slot));
            if (source != null) {
                UIStateChartExecutor execute = ComponentUtils.assigned(UIStateChartExecutor.class, this);
                if (source.startsWith("@renderer:")) {
                    String name = source.substring(10);
                    facet = getFacet(name);
                    if (facet == null) {
                        throwRequiredRendererFacetException(context, name);
                    }
                } else if (source.startsWith("@executor:")) {
                    if (execute == null) {
                        throwNoInController(context, "@executor");
                    }

                    String name = source.substring(10);

                    facet = execute.getFacet(name);

                    if (facet == null) {
                        throwRequiredExecutorFacetException(context, name);
                    }
                } else if (source.startsWith("@viewroot:")) {
                    if (execute != null) {
                        throwInControler(context, "@viewroot");
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
