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
import javax.faces.component.UIComponent;

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

}
