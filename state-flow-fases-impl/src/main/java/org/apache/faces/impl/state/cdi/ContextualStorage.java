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
package org.apache.faces.impl.state.cdi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.FacesContext;
import org.apache.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ContextualStorage implements Serializable {

    private Map<Object, ContextualInstanceInfo<?>> contextualInstances;

    private transient BeanManager beanManager;

    private boolean concurrent;

    private boolean passivationCapable;

    /**
     * @param beanManager is needed for serialisation
     * @param concurrent whether the ContextualStorage might get accessed
     * concurrently by different threads
     * @param passivationCapable whether the storage is for passivation capable
     * Scopes
     */
    public ContextualStorage(BeanManager beanManager, boolean concurrent, boolean passivationCapable) {
        this.beanManager = beanManager;
        this.concurrent = concurrent;
        this.passivationCapable = passivationCapable;
        if (concurrent) {
            contextualInstances = new ConcurrentHashMap<>();
        } else {
            contextualInstances = new HashMap<>();
        }
    }

    /**
     * @return the underlying storage map.
     */
    public Map<Object, ContextualInstanceInfo<?>> getStorage() {
        return contextualInstances;
    }

    /**
     * @return whether the ContextualStorage might get accessed concurrently by
     * different threads.
     */
    public boolean isConcurrent() {
        return concurrent;
    }

    /**
     *
     * @param bean
     * @param creationalContext
     * @param <T>
     * @return
     */
    public <T> T createContextualInstance(Contextual<T> bean, CreationalContext<T> creationalContext) {
        Object beanKey = getBeanKey(bean);
        if (isConcurrent()) {
            // locked approach
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<>();

            ConcurrentMap<Object, ContextualInstanceInfo<?>> concurrentMap
                    = (ConcurrentHashMap<Object, ContextualInstanceInfo<?>>) getStorage();

            ContextualInstanceInfo<T> oldInstanceInfo
                    = (ContextualInstanceInfo<T>) concurrentMap.putIfAbsent(beanKey, instanceInfo);

            if (oldInstanceInfo != null) {
                instanceInfo = oldInstanceInfo;
            }
            synchronized (instanceInfo) {
                T instance = instanceInfo.getContextualInstance();
                if (instance == null) {
                    instance = bean.create(creationalContext);
                    instanceInfo.setContextualInstance(instance);
                    instanceInfo.setCreationalContext(creationalContext);
                }

                return instance;
            }

        } else {
            // simply create the contextual instance
            ContextualInstanceInfo<T> instanceInfo = new ContextualInstanceInfo<>();
            instanceInfo.setCreationalContext(creationalContext);
            instanceInfo.setContextualInstance(bean.create(creationalContext));

            getStorage().put(beanKey, instanceInfo);

            return instanceInfo.getContextualInstance();
        }
    }

    /**
     * If the context is a passivating scope then we return the passivationId of
     * the Bean. Otherwise we use the Bean directly.
     *
     * @param <T>
     * @param bean
     * @return the key to use in the context map
     */
    public <T> Object getBeanKey(Contextual<T> bean) {
        if (passivationCapable) {
            // if the
            return ((PassivationCapable) bean).getId();
        }

        return bean;
    }

    /**
     * Restores the Bean from its beanKey.
     *
     * @param beanKey
     * @return
     * @see #getBeanKey(javax.enterprise.context.spi.Contextual)
     */
    public Contextual<?> getBean(Object beanKey) {
        if (passivationCapable) {
            return beanManager.getPassivationCapableBean((String) beanKey);
        } else {
            return (Contextual<?>) beanKey;
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(concurrent);
        out.writeObject(passivationCapable);
        out.writeObject(contextualInstances);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        //noinspection unchecked
        concurrent = (boolean) in.readObject();
        passivationCapable = (boolean) in.readObject();
        contextualInstances = (Map<Object, ContextualInstanceInfo<?>>) in.readObject();

        beanManager = CdiUtil.getCdiBeanManager(FacesContext.getCurrentInstance());
    }

}
