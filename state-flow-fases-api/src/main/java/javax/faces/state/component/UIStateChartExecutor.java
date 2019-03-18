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

import java.util.ArrayDeque;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.SCXMLExecutor;
import static javax.faces.state.utils.ComponentUtils.getComponentStack;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateChartExecutor extends UIPanel {

    private static final String _CURRENT_EXECUTOR_STACK_KEY
                                = "javax.faces.state.component.CURRENT_EXECUTOR_STACK_KEY";

    private transient SCXMLExecutor _executor;

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
    public static final String COMPONENT_TYPE = "javax.faces.state.UIStateChartExecutor";

    enum PropertyKeys {
        name,
        required,
    }

    /**
     *
     */
    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateChartExecutor() {
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


    public String getName() {
        return (java.lang.String) getStateHelper().eval(PropertyKeys.name, null);
    }

    public void setName(java.lang.String _name) {
        getStateHelper().put(PropertyKeys.name, _name);
    }

    public boolean isRequired() {
        return (java.lang.Boolean) getStateHelper().eval(PropertyKeys.required, true);
    }

    public void setRequired(boolean _required) {
        getStateHelper().put(PropertyKeys.required, _required);
    }

    public String getPath(FacesContext context) {
        String path = context.getViewRoot().getViewId() + "!" + getName();
        return path;
    }

//    @Override
//    public void pushComponentToEL(FacesContext context, UIComponent component) {
//        super.pushComponentToEL(context, component);
//        pushExecutorToEl(context, (UIStateChartExecutor) component);
//    }
//
//    @Override
//    public void popComponentFromEL(FacesContext context) {
//        super.popComponentFromEL(context);
//        popExecutorFromEl(context);
//    }

    public void pushExecutorToEl(FacesContext context, UIStateChartExecutor component) {
        if (context == null) {
            throw new NullPointerException();
        }
        
        if (null == component) {
            component = this;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        componentStack.push(component);
        component._isPushedAsCurrentRefCount++;
    }

    public void popExecutorFromEl(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (_isPushedAsCurrentRefCount < 1) {
            return;
        }

        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        for (UIComponent topComponent = componentStack.peek();
                topComponent != this;
                topComponent = componentStack.peek()) {
            if (topComponent instanceof UIStateChartFacetRender) {
                ((UIStateChartExecutor) topComponent).popExecutorFromEl(context);

            } else {
                componentStack.pop();
            }
        }

        componentStack.pop();
        _isPushedAsCurrentRefCount--;

    }

    public static UIStateChartExecutor getCurrentExecutor(FacesContext context) {
        Map<Object, Object> contextAttributes = context.getAttributes();
        ArrayDeque<UIComponent> componentStack = getComponentStack(_CURRENT_EXECUTOR_STACK_KEY,
                contextAttributes);

        return (UIStateChartExecutor) componentStack.peek();
    }

}
