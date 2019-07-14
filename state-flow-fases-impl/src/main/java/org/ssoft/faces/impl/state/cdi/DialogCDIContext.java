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

import com.sun.faces.util.Util;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.ssoft.faces.impl.state.StateFlowImplConstants;
import org.ssoft.faces.impl.state.log.FlowLogger;
import javax.faces.state.StateChartExecuteContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.annotation.DialogScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DialogCDIContext implements Context, Serializable {

    private static final String DIALOG_SCOPE_KEY = "dialogscope";
    private static final String DIALOG_SCOPE_MAP_KEY = StateFlowImplConstants.STATE_FLOW_PREFIX + "STATE_DIALOG_SCOPE_MAP";
    private static final Logger LOGGER = FlowLogger.CDI.getLogger();

    @Override
    public Class<? extends Annotation> getScope() {
        return DialogScoped.class;
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();

        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = getExecutor(facesContext);
        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, DIALOG_SCOPE_KEY);

        T result = get(mapHelper, contextual);

        if (null == result) {
            ScopedBeanContext flowScopedBeanMap = mapHelper.getScopedBeanContextForCurrentExecutor();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMapForCurrentExecutor();

            String passivationCapableId = ((PassivationCapable) contextual).getId();

            synchronized (flowScopedBeanMap) {
                result = (T) flowScopedBeanMap.get(passivationCapableId);
                if (null == result) {

                    if (null == executor) {
                        return null;
                    }

                    if (!executor.isRunning()) {
                        LOGGER.warning("Request to activate bean in executor, but that executor is not active.");
                    }

                    result = contextual.create(creational);

                    if (null != result) {
                        flowScopedBeanMap.setLocal(passivationCapableId, result);
                        creationalMap.put(passivationCapableId, creational);
                        mapHelper.updateSession();
                    }
                }
            }
        }
        mapHelper = null;
        return result;
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("FlowScoped StateDialogScoped " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor current = getExecutor(facesContext);
        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, current, DIALOG_SCOPE_KEY);

        T result = get(mapHelper, contextual);
        mapHelper = null;

        return result;
    }

    private <T> T get(StateScopeMapHelper mapHelper, Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateDialogScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        String passivationCapableId = ((PassivationCapable) contextual).getId();
        return (T) mapHelper.getScopedBeanContextForCurrentExecutor().get(passivationCapableId);
    }

    @Override
    public boolean isActive() {
        return null != getExecutor();
    }

    /**
     *
     * @param hse
     */
    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        StateScopeMapHelper.sessionDestroyed(session);
    }

    private static Map<Object, Object> getCurrentFlowScopeAndUpdateSession(StateScopeMapHelper mapHelper) {
        ScopedBeanContext flowScopedBeanMap = mapHelper.getScopedBeanContextForCurrentExecutor();
        Map<Object, Object> result = null;
        if (mapHelper.isExecutorExists()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(DIALOG_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                flowScopedBeanMap.setLocal(DIALOG_SCOPE_MAP_KEY, result);
            }
        }
        mapHelper.updateSession();
        return result;
    }

    static void executorExited(SCXMLExecutor executor) {
        if(executor.getParentSCXMLIOProcessor() != null) {
            return;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, DIALOG_SCOPE_KEY);
        ScopedBeanContext flowScopedBeanMap = mapHelper.getScopedBeanContextForCurrentExecutor();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMapForCurrentExecutor();
        assert (!flowScopedBeanMap.getVars().isEmpty());
        assert (!creationalMap.isEmpty());
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);

        for (Map.Entry<String, Object> entry : flowScopedBeanMap.getVars().entrySet()) {
            String passivationCapableId = entry.getKey();
            if (DIALOG_SCOPE_MAP_KEY.equals(passivationCapableId)) {
                continue;
            }
            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(passivationCapableId);

            owner.destroy(bean, creational);
        }

        flowScopedBeanMap.getVars().clear();
        creationalMap.clear();

        mapHelper.updateSession();

        if (CdiUtil.isCdiOneOneOrLater(facesContext)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }

            if (null != flowCDIEventFireHelperImplClass) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext
                            = beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper
                            = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
                                    creationalContext);
                    eventHelper.fireRootExecutorDestroyedEvent(executor);
                }
            }
        }
    }

    static void executorEntered(SCXMLExecutor executor) {
        if(executor.getParentSCXMLIOProcessor() != null) {
            return;
        }
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, DIALOG_SCOPE_KEY);

        mapHelper.createMaps();

        getCurrentFlowScopeAndUpdateSession(mapHelper);

        if (CdiUtil.isCdiOneOneOrLater(facesContext)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }
            if (null != flowCDIEventFireHelperImplClass) {
                BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext
                            = beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper
                            = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
                                    creationalContext);
                    eventHelper.fireRootExecutorInitializedEvent(executor);
                }
            }
        }
    }

    @SuppressWarnings({"FinalPrivateMethod"})
    private final void assertNotReleased() {
        if (!isActive()) {
            throw new IllegalStateException();
        }
    }

    private SCXMLExecutor getExecutor() {
        FacesContext context = FacesContext.getCurrentInstance();
        SCXMLExecutor result = getExecutor(context);
        return result;
    }

    private static SCXMLExecutor getExecutor(FacesContext context) {
        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        if (null == flowHandler) {
            return null;
        }

        SCXMLExecutor result = null;
        StateChartExecuteContext ec = flowHandler.getCurrentExecuteContext(context);
        if (ec != null) {
            result = ec.getExecutor();
        }        
        return result;

    }

}
