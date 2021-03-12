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

import jakarta.el.ValueExpression;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.Metadata;
import jakarta.faces.view.facelets.MetadataTarget;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class BeanPropertyTagRule extends MetaRule {

    final static class PropertyMetadata extends Metadata {

        private final Method method;

        private final TagAttribute attribute;

        private final Class type;

        public PropertyMetadata(Method method, TagAttribute attribute) {
            this.method = method;
            this.type = method.getParameterTypes()[0];
            this.attribute = attribute;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            try {
                if (ValueExpression.class.isAssignableFrom(method.getParameterTypes()[0])) {
                    this.method.invoke(instance, new Object[]{this.attribute.getValueExpression(ctx, Object.class)});
                } else {
                    this.method.invoke(instance, new Object[]{this.attribute.getValue()});
                }
            } catch (InvocationTargetException e) {
                throw new TagAttributeException(this.attribute, e.getCause());
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new TagAttributeException(this.attribute, e);
            }
        }
    }

    /**
     *
     */
    public final static BeanPropertyTagRule Instance = new BeanPropertyTagRule();

    @Override
    public Metadata applyRule(String name, TagAttribute attribute, MetadataTarget meta) {
        Method m = meta.getWriteMethod(name);
        return new PropertyMetadata(m, attribute);
    }

}
