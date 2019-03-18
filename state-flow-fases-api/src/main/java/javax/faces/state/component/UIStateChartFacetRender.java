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
import java.util.ArrayDeque;
import java.util.Map;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.Location;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import static javax.faces.state.StateFlow.RENDER_EXECUTOR_FACET;
import javax.faces.state.utils.ComponentUtils;
import static javax.faces.state.utils.ComponentUtils.getComponentStack;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartFacetRender extends UIPanel {

    private static final String _CURRENT_RENDERER_STACK_KEY
                                = "javax.faces.state.component.CURRENT_RENDERER_STACK_KEY";

    private transient SCXMLExecutor _executor;
    private transient String _path;

    private int _isPushedAsCurrentRefCount = 0;

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

    public SCXMLExecutor getExecutor() {
        return _executor;
    }

    public void setExecutor(SCXMLExecutor executor) {
        this._executor = executor;
    }

    public String getInvokePath(FacesContext context) {
        if (_path == null && _executor != null) {
            _path = _executor.getId() + ":" + getSlot();
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

    @Override
    public void pushComponentToEL(FacesContext context, UIComponent component) {
        super.pushComponentToEL(context, component);
        pushRendererToEl(context, (UIStateChartFacetRender) component);
    }

    @Override
    public void popComponentFromEL(FacesContext context) {
        super.popComponentFromEL(context);
        popRendererFromEl(context);
    }

    private void pushRendererToEl(FacesContext context, UIStateChartFacetRender component) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (null == component) {
            component = this;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_RENDERER_STACK_KEY,
                contextAttributes);

        componentStack.push(component);
        component._isPushedAsCurrentRefCount++;
    }

    private void popRendererFromEl(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (_isPushedAsCurrentRefCount < 1) {
            return;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_RENDERER_STACK_KEY,
                contextAttributes);

        for (UIComponent topComponent = componentStack.peek();
                topComponent != this;
                topComponent = componentStack.peek()) {
            if (topComponent instanceof UIStateChartFacetRender) {
                ((UIStateChartFacetRender) topComponent).popRendererFromEl(context);

            } else {
                componentStack.pop();
            }
        }

        componentStack.pop();
        _isPushedAsCurrentRefCount--;

    }

    public static UIStateChartFacetRender getCurrentRenderer(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_RENDERER_STACK_KEY,
                contextAttributes);

        return (UIStateChartFacetRender) componentStack.peek();
    }

    private UIComponent getCurentEncodeFacet(FacesContext context) {
        UIComponent facet = null;

        SCXMLExecutor executor = getExecutor();
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
