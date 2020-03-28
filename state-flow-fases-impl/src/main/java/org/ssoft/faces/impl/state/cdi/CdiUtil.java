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
package org.ssoft.faces.impl.state.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.logging.Level;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.InjectionTargetFactory;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.ssoft.faces.impl.state.StateFlowImplConstants;
import static org.ssoft.faces.impl.state.utils.Util.log;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CdiUtil {

    /**
     *
     * @param <T>
     * @param beanManager
     * @param instance
     * @return
     */
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

    /**
     *
     * @param beanManager
     * @param beanClass
     * @return
     */
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

    /**
     *
     * @param <T>
     * @param type
     * @param qualifiers
     * @return
     */
    public static <T> T getBeanReference(Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(getCdiBeanManager(FacesContext.getCurrentInstance()), type, qualifiers));
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

    /**
     *
     * @param beanManager
     * @param type
     * @param qualifiers
     * @return
     */
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
        BeanManager beanManager = getCdiBeanManager(FacesContext.getCurrentInstance());
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

    /**
     *
     * @return
     */
    public static boolean isCdiOneOneOrGreater() {

        // The following try/catch is a hack to discover
        // if CDI 1.1 or greater is available
        boolean result = false;
        try {
            Class.forName("javax.enterprise.context.Initialized");
            result = true;
        } catch (ClassNotFoundException ignored) {
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "Dected CDI 1.0", ignored);
            }
        }
        return result;
    }

    /**
     * Is CDI 1.1 or later
     *
     * @param facesContext the Faces context.
     * @return true if CDI 1.1 or later, false otherwise.
     */
    public static boolean isCdiOneOneOrLater(FacesContext facesContext) {
        boolean result = false;

        if (facesContext != null && facesContext.getAttributes().containsKey(StateFlowImplConstants.CDI_1_1_OR_LATER)) {
            result = (Boolean) facesContext.getAttributes().get(StateFlowImplConstants.CDI_1_1_OR_LATER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(StateFlowImplConstants.CDI_1_1_OR_LATER)) {
            result = facesContext.getExternalContext().getApplicationMap().containsKey(StateFlowImplConstants.CDI_1_1_OR_LATER);
        } else {
            try {
                Class.forName("javax.enterprise.context.Initialized");
                result = true;
            } catch (ClassNotFoundException ignored) {
                if (log.isLoggable(Level.FINEST)) {
                    log.log(Level.FINEST, "Detected CDI 1.0", ignored);
                }
            }

            if (facesContext != null) {
                facesContext.getAttributes().put(StateFlowImplConstants.CDI_1_1_OR_LATER, result);
                facesContext.getExternalContext().getApplicationMap().put(StateFlowImplConstants.CDI_1_1_OR_LATER, result);
            }
        }

        return result;
    }

    /**
     * Get the CDI bean manager.
     *
     * @param facesContext the Faces context to consult
     * @return the CDI bean manager.
     */
    public static BeanManager getCdiBeanManager(FacesContext facesContext) {
        BeanManager result = null;

        if (facesContext != null && facesContext.getAttributes().containsKey(StateFlowImplConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getAttributes().get(StateFlowImplConstants.CDI_BEAN_MANAGER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(StateFlowImplConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getExternalContext().getApplicationMap().get(StateFlowImplConstants.CDI_BEAN_MANAGER);
        } else {
            try {
                InitialContext initialContext = new InitialContext();
                result = (BeanManager) initialContext.lookup("java:comp/BeanManager");
            } catch (NamingException ne) {
                try {
                    InitialContext initialContext = new InitialContext();
                    result = (BeanManager) initialContext.lookup("java:comp/env/BeanManager");
                } catch (NamingException ne2) {
                }
            }

            if (result == null && facesContext != null) {
                Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
                result = (BeanManager) applicationMap.get("org.jboss.weld.environment.servlet.javax.enterprise.inject.spi.BeanManager");
            }

            if (result != null && facesContext != null) {
                facesContext.getAttributes().put(StateFlowImplConstants.CDI_BEAN_MANAGER, result);
                facesContext.getExternalContext().getApplicationMap().put(StateFlowImplConstants.CDI_BEAN_MANAGER, result);
            }
        }

        return result;
    }

    /**
     * Is CDI available.
     *
     * @param facesContext the Faces context to consult.
     * @return true if available, false otherwise.
     */
    public static boolean isCdiAvailable(FacesContext facesContext) {
        boolean result;

        if (facesContext != null && facesContext.getAttributes().containsKey(StateFlowImplConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getAttributes().get(StateFlowImplConstants.CDI_AVAILABLE);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(StateFlowImplConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getExternalContext().getApplicationMap().get(StateFlowImplConstants.CDI_AVAILABLE);
        } else {
            result = getCdiBeanManager(facesContext) != null;

            if (result && facesContext != null) {
                facesContext.getAttributes().put(StateFlowImplConstants.CDI_AVAILABLE, result);
                facesContext.getExternalContext().getApplicationMap().put(StateFlowImplConstants.CDI_AVAILABLE, result);
            }
        }

        return result;
    }
    
    
}
