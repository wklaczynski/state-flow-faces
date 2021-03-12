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
package org.ssoft.faces.impl.state.tag;

import jakarta.el.MethodExpression;
import jakarta.faces.FacesException;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.faces.state.scxml.model.Param;
import javax.faces.state.scxml.model.ParamsContainer;
import org.ssoft.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class MethodPropertyTagRule extends MetaRule {

    private static final Map<String, String> classmap = new ConcurrentHashMap<>();

    static {
        classmap.put("string", String.class.getName());
        classmap.put("boolean", Boolean.class.getName());
        classmap.put("int", Integer.class.getName());
        classmap.put("long", Long.class.getName());
        classmap.put("float", Float.class.getName());
        classmap.put("double", Double.class.getName());
        classmap.put("currency", Currency.class.getName());
        classmap.put("date", Date.class.getName());
    }

    private final String methodName;
    private final Class returnTypeClass;

    public MethodPropertyTagRule(String methodName, Class returnTypeClass) {
        this.methodName = methodName;
        this.returnTypeClass = returnTypeClass;
    }

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {
        if (false == name.equals(this.methodName)) {
            return null;
        }

        Class<?> type = meta.getPropertyType(name);

        if (MethodExpression.class.equals(type)) {
            Method method = meta.getWriteMethod(name);
            if (method != null) {
                return new MethodExpressionMetadata(method, attribute, this.returnTypeClass);
            }
        } else if (type != null && "javax.faces.el.MethodBinding".equals(type.getName())) {
            throw new FacesException("javax.faces.el.MethodBinding should not be used anymore!");
        }

        return null;
    }

    private static class MethodExpressionMetadata extends Metadata {

        private final Method method;
        private final TagAttribute attribute;
        private final Class returnType;

        public MethodExpressionMetadata(Method method, TagAttribute attribute, Class returnType) {
            this.method = method;
            this.attribute = attribute;
            this.returnType = returnType;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            try {
                List<Param> paramsList = new ArrayList<>();
                if (instance instanceof ParamsContainer) {
                    paramsList.addAll(((ParamsContainer) instance).getParams());
                }
                ClassLoader loader = Util.getCurrentLoader(this);
                Class[] classes = new Class[paramsList.size()];
                for (int i = 0; i < paramsList.size(); i++) {
                    Param param = paramsList.get(i);
                    String className = param.getLocation();
                    if (classmap.containsKey(className)) {
                        className = classmap.get(className);
                    }
                    classes[i] = loader.loadClass(className);
                }

                MethodExpression expr = this.attribute.getMethodExpression(ctx, this.returnType, classes);

                this.method.invoke(instance, new Object[]{expr});
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(this.attribute, e.getCause());
            } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException e) {
                throw new TagAttributeException(this.attribute, e);
            }
        }
    }

}
