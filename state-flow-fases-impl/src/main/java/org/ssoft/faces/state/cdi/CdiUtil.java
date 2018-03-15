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
package org.ssoft.faces.state.cdi;

import com.sun.faces.util.Util;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CdiUtil {

    public static <T> T injectFields(BeanManager beanManager, T instance) {
        if (instance == null) {
            return null;
        }

        CreationalContext<T> creationalContext = beanManager.createCreationalContext(null);

        AnnotatedType<T> annotatedType = beanManager.createAnnotatedType((Class<T>) instance.getClass());
        InjectionTarget<T> injectionTarget = beanManager.createInjectionTarget(annotatedType);
        injectionTarget.inject(instance, creationalContext);
        return instance;
    }

    public static Bean createHelperBean(BeanManager beanManager, Class beanClass) {
        CdiBeanWrapper result;

        AnnotatedType annotatedType = beanManager.createAnnotatedType(beanClass);

        InjectionTargetFactory factory = beanManager.getInjectionTargetFactory(annotatedType);

        result = new CdiBeanWrapper(beanClass);
        //use this to create the class and inject dependencies
        final InjectionTarget injectionTarget = factory.createInjectionTarget(result);
        result.setInjectionTarget(injectionTarget);

        return result;
    }

    public static <T> T getBeanReference(Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(Util.getCdiBeanManager(FacesContext.getCurrentInstance()), type, qualifiers));
    }

    /**
     * @param <T>
     * @param beanManager the bean manager
     * @param type the required bean type the reference must have
     * @param qualifiers
     * @return a bean reference adhering to the required type and qualifiers
     */
    public static <T> T getBeanReference(BeanManager beanManager, Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(beanManager, type, qualifiers));
    }

    public static Object getBeanReferenceByType(BeanManager beanManager, Type type, Annotation... qualifiers) {

        Object beanReference = null;

        Bean<?> bean = beanManager.resolve(beanManager.getBeans(type, qualifiers));
        if (bean != null) {
            beanReference = beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
        }

        return beanReference;
    }

    /**
     * Returns concrete (non-proxied) bean instance of given class in current
     * context.
     *
     * @param <T>
     * @param type the required bean type the instance must have
     * @param create whether to auto-create bean if not exist
     * @return a bean instance adhering to the required type
     */
    public static <T> T getBeanInstance(Class<T> type, boolean create) {
        BeanManager beanManager = Util.getCdiBeanManager(FacesContext.getCurrentInstance());
        @SuppressWarnings("unchecked")
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type));

        if (bean != null) {
            Context context = beanManager.getContext(bean.getScope());

            if (create) {
                return context.get(bean, beanManager.createCreationalContext(bean));
            } else {
                return context.get(bean);
            }
        } else {
            return null;
        }
    }

}
