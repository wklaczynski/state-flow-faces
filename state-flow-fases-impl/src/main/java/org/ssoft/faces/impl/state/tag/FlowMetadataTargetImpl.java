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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.faces.view.facelets.MetadataTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowMetadataTargetImpl extends MetadataTarget {

    private final Map pdmap;
    private final Class type;
    
    /**
     *
     * @param type
     * @throws IntrospectionException
     */
    public FlowMetadataTargetImpl(Class type) throws IntrospectionException {
        this.type = type;
        this.pdmap = new HashMap();
        BeanInfo info = Introspector.getBeanInfo(type);
        PropertyDescriptor[] pda = info.getPropertyDescriptors();
        for (PropertyDescriptor pda1 : pda) {
            this.pdmap.put(pda1.getName(), pda1);
        }
    }

    @Override
    public PropertyDescriptor getProperty(String name) {
        return (PropertyDescriptor) this.pdmap.get(name);
    }

    @Override
    public boolean isTargetInstanceOf(Class type) {
        return type.isAssignableFrom(this.type);
    }

    @Override
    public Class getTargetClass() {
        return this.type;
    }

    @Override
    public Class getPropertyType(String name) {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null) {
            return pd.getPropertyType();
        }
        return null;
    }

    @Override
    public Method getWriteMethod(String name) {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null) {
            return pd.getWriteMethod();
        }
        return null;
    }

    @Override
    public Method getReadMethod(String name) {
        PropertyDescriptor pd = this.getProperty(name);
        if (pd != null) {
            return pd.getReadMethod();
        }
        return null;
    }

}
