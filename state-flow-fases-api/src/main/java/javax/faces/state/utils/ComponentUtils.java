/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package javax.faces.state.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import javax.faces.application.Application;
import javax.faces.application.Resource;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.state.StateFlow;
import javax.faces.view.Location;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ComponentUtils {

    public static <T> T closest(Class<T> type, UIComponent base) {
        UIComponent parent = base.getParent();

        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }

    public static <T> T assigned(Class<T> type, UIComponent base) {
        if (type.isAssignableFrom(base.getClass())) {
            return (T) base;
        }

        UIComponent parent = base.getParent();

        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) {
                return (T) parent;
            }

            parent = parent.getParent();
        }

        return null;
    }

    public static <T> T lokated(Class<T> type, UIComponent base) {
        if (type.isAssignableFrom(base.getClass())) {
            return (T) base;
        }
        FacesContext ctx = FacesContext.getCurrentInstance();

        String path = null;
        Location location = (Location) base.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
        if (location != null) {
            path = location.getPath();
        }

        UIComponent parent = base.getParent();

        while (parent != null) {
            if (type.isAssignableFrom(parent.getClass())) {
                if (location == null) {
                    return (T) parent;
                }
                UIComponent cc = findLocatedParentCompositeComponent(ctx, parent);
                if (cc == null) {
                    return (T) parent;
                }
                Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
                if (path == null || r == null || path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
                    return (T) parent;
                }
            }
            parent = parent.getParent();
        }

        return null;
    }

    public static <T> ArrayList<T> children(Class<T> type, UIComponent base) {

        ArrayList<T> result = new ArrayList<>();

        Iterator<UIComponent> kids = base.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = (UIComponent) kids.next();
            if (type.isAssignableFrom(kid.getClass())) {
                result.add((T) kid);
            }
        }

        return result;
    }

    public static boolean isInOrEqual(UIComponent top, UIComponent base) {
        UIComponent parent = base;
        while (parent != null) {
            if (Objects.deepEquals(parent, top)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    public static ArrayDeque<UIComponent> getComponentStack(String keyName, Map<Object, Object> contextAttributes) {
        ArrayDeque<UIComponent> stack = (ArrayDeque<UIComponent>) contextAttributes.computeIfAbsent(keyName, (t) -> {
            return new ArrayDeque<>();
        });

        return stack;
    }

    public static void addComponentResource(FacesContext context, String name, String library, String target) {
        Application application = context.getApplication();

        UIComponent componentResource = application.createComponent(UIOutput.COMPONENT_TYPE);
        componentResource.setRendererType(application.getResourceHandler().getRendererTypeForResourceName(name));
        componentResource.getAttributes().put("name", name);
        componentResource.getAttributes().put("library", library);
        componentResource.getAttributes().put("target", target);

        context.getViewRoot().addComponentResource(context, componentResource, target);
    }

    public static void loadResorces(FacesContext context, UIViewRoot view, Object instance, String target) {
        ResourceDependencies aresorces = instance.getClass().getAnnotation(ResourceDependencies.class);
        if (aresorces != null) {
            for (ResourceDependency resource : aresorces.value()) {
                String name = resource.name();
                addComponentResource(context, name, resource.library(), target);
            }
        }
    }

    public static UIComponent findCompositeComponentUsingLocation(FacesContext ctx, Location location) {

        String path = location.getPath();
        UIComponent cc = UIComponent.getCurrentCompositeComponent(ctx);
        while (cc != null) {
            Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
            if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
                return cc;
            }
            cc = UIComponent.getCompositeComponentParent(cc);
        }
        return UIComponent.getCurrentCompositeComponent(ctx);
    }

    public static UIComponent findLocatedParentCompositeComponent(FacesContext ctx, UIComponent component) {
        if (component == null) {
            component = UIComponent.getCurrentComponent(ctx);
        }
        Location location = (Location) component.getAttributes().get(UIComponent.VIEW_LOCATION_KEY);
        if (location == null) {
            return UIComponent.getCompositeComponentParent(component);
        }
        
        String path = location.getPath();
        UIComponent cc = UIComponent.getCompositeComponentParent(component);
        while (cc != null) {
            Resource r = (Resource) cc.getAttributes().get(Resource.COMPONENT_RESOURCE_KEY);
            if (path.endsWith('/' + r.getResourceName()) && path.contains(r.getLibraryName())) {
                return cc;
            }
            cc = UIComponent.getCompositeComponentParent(cc);
        }
        return null;
    }

    public static UIComponent findExecuteCompositeComponent(FacesContext ctx, UIComponent cc) {

        if (!UIComponent.isCompositeComponent(cc)) {
            cc = UIComponent.getCompositeComponentParent(cc);
        }

        while (cc != null) {
            if (cc.getAttributes().containsKey(StateFlow.EXECUTOR_CONTROLLER_KEY)) {
                return cc;
            }
            cc = UIComponent.getCompositeComponentParent(cc);
        }
        return null;
    }

    public static UIComponent findExecuteCompositeComponent(FacesContext ctx) {

        UIComponent cc = UIComponent.getCurrentCompositeComponent(ctx);
        while (cc != null) {
            if (cc.getAttributes().containsKey(StateFlow.EXECUTOR_CONTROLLER_KEY)) {
                return cc;
            }
            cc = UIComponent.getCompositeComponentParent(cc);
        }
        return null;
    }

}
