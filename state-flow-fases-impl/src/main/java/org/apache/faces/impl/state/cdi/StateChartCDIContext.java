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

import com.sun.faces.util.Util;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.apache.faces.impl.state.StateFlowConstants;
import org.apache.faces.impl.state.log.FlowLogger;
import org.apache.faces.state.StateFlowHandler;
import org.apache.faces.state.annotation.StateChartScoped;
import org.apache.scxml.SCXMLExecutor;
import org.apache.scxml.SCXMLSystemContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChartCDIContext implements Context, Serializable {

    private static final String FLOW_SCOPE_MAP_KEY = StateFlowConstants.STATE_FLOW_PREFIX + "STATE_CHART_SCOPE_MAP";
    private static final Logger LOGGER = FlowLogger.FLOW.getLogger();

    private static final String PER_SESSION_BEAN_MAP_LIST = StateChartCDIContext.class.getPackage().getName() + ".statechart.PER_SESSION_BEAN_MAP_LIST";
    private static final String PER_SESSION_CREATIONAL_LIST = StateChartCDIContext.class.getPackage().getName() + ".statechart.PER_SESSION_CREATIONAL_LIST";

    @Override
    public Class<? extends Annotation> getScope() {
        return StateChartScoped.class;
    }

    @Override
    @SuppressWarnings("UnusedAssignment")
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = getCurrentExecutor(facesContext);
        StateChartScopeMapHelper mapHelper = new StateChartScopeMapHelper(facesContext, executor);
        
        T result = get(mapHelper, contextual);
        
        if (null == result) {
            Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentExecutor();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getFlowScopedCreationalMapForCurrentExecutor();
            
            String passivationCapableId = ((PassivationCapable)contextual).getId();

            synchronized (flowScopedBeanMap) {
                result = (T) flowScopedBeanMap.get(passivationCapableId);
                if (null == result) {
                    
                    if (null == executor) {
                        return null;
                    }
                    
                    if (!executor.isRunning()) {
                        throw new ContextNotActiveException("Request to activate bean in executor, but that executor is not active.");
                    }

                    
                    result = contextual.create(creational);
                    
                    if (null != result) {
                        flowScopedBeanMap.put(passivationCapableId, result);
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
            throw new IllegalArgumentException("FlowScoped StateChartScoped " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor current = getCurrentExecutor(facesContext);
        StateChartScopeMapHelper mapHelper = new StateChartScopeMapHelper(facesContext, current);

        T result = get(mapHelper, contextual);
        mapHelper = null;

        return result;
    }
    
    private <T> T get(StateChartScopeMapHelper mapHelper, Contextual<T> contextual) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateChartScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
        String passivationCapableId = ((PassivationCapable)contextual).getId();
        return (T) mapHelper.getFlowScopedBeanMapForCurrentExecutor().get(passivationCapableId);
    }
    

    @Override
    public boolean isActive() {
        return null != getCurrentExecutor();
    }

    private static class StateChartScopeMapHelper {

        private transient String flowBeansForClientWindowKey;
        private transient String creationalForClientWindowKey;
        private transient final Map<String, Object> sessionMap;

        private StateChartScopeMapHelper(FacesContext facesContext, SCXMLExecutor executor) {
            ExternalContext extContext = facesContext.getExternalContext();
            this.sessionMap = extContext.getSessionMap();

            generateKeyForCDIBeansBelongToAExecutor(facesContext, executor);
        }

        private void generateKeyForCDIBeansBelongToAExecutor(FacesContext facesContext, SCXMLExecutor executor) {
            if (null != executor) {

                String currentSessionId = (String) executor.getSCInstance().getSystemContext().get(SCXMLSystemContext.SESSIONID_KEY);

                String prefix = "StateChart";
                
                flowBeansForClientWindowKey = prefix + ":" + currentSessionId + "_beans";
                creationalForClientWindowKey = prefix + ":" + currentSessionId + "_creational";

            } else {
                flowBeansForClientWindowKey = creationalForClientWindowKey = null;
            }
        }

        private void createMaps() {
            getFlowScopedBeanMapForCurrentExecutor();
            getFlowScopedCreationalMapForCurrentExecutor();
        }

        private boolean isFlowExists() {
            return (null != flowBeansForClientWindowKey && null != creationalForClientWindowKey);
        }

        public String getCreationalForClientWindowKey() {
            return creationalForClientWindowKey;
        }

        public String getFlowBeansForClientWindowKey() {
            return flowBeansForClientWindowKey;
        }

        private Map<String, Object> getFlowScopedBeanMapForCurrentExecutor() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return Collections.emptyMap();
            }
            Map<String, Object> result;
            result = (Map<String, Object>) sessionMap.get(flowBeansForClientWindowKey);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                sessionMap.put(flowBeansForClientWindowKey, result);
                ensureBeanMapCleanupOnSessionDestroyed(sessionMap, flowBeansForClientWindowKey);
            }
            return result;
        }

        private Map<String, CreationalContext<?>> getFlowScopedCreationalMapForCurrentExecutor() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return Collections.emptyMap();
            }
            Map<String, CreationalContext<?>> result;
            result = (Map<String, CreationalContext<?>>) sessionMap.get(creationalForClientWindowKey);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                sessionMap.put(creationalForClientWindowKey, result);
                ensureCreationalCleanupOnSessionDestroyed(sessionMap, creationalForClientWindowKey);
            }
            return result;
        }

        private void updateSession() {
            if (null == flowBeansForClientWindowKey && null == creationalForClientWindowKey) {
                return;
            }
            sessionMap.put(flowBeansForClientWindowKey, getFlowScopedBeanMapForCurrentExecutor());
            sessionMap.put(creationalForClientWindowKey, getFlowScopedCreationalMapForCurrentExecutor());
            Object obj = sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
            if (null != obj) {
                sessionMap.put(PER_SESSION_BEAN_MAP_LIST, obj);
            }
            obj = sessionMap.get(PER_SESSION_CREATIONAL_LIST);
            if (null != obj) {
                sessionMap.put(PER_SESSION_CREATIONAL_LIST, obj);
            }
        }
    }

    
    private static void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String flowBeansForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, beanMapList);
        }
        beanMapList.add(flowBeansForClientWindow);
    }

    private static void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, beanMapList);
        }
        beanMapList.add(creationalForClientWindow);
    }

    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        
        List<String> beanMapList = (List<String>) session.getAttribute(PER_SESSION_BEAN_MAP_LIST);
        if (null != beanMapList) {
            for (String cur : beanMapList) {
                Map<Contextual<?>, Object> beanMap = 
                        (Map<Contextual<?>, Object>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_BEAN_MAP_LIST);
            beanMapList.clear();
        }
        
        List<String> creationalList = (List<String>) session.getAttribute(PER_SESSION_CREATIONAL_LIST);
        if (null != creationalList) {
            for (String cur : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> beanMap = 
                        (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_CREATIONAL_LIST);
            creationalList.clear();
        }
        
        
    }
    
    private static Map<Object, Object> getCurrentFlowScopeAndUpdateSession(StateChartScopeMapHelper mapHelper) {
        Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentExecutor();
        Map<Object, Object> result = null;
        if (mapHelper.isFlowExists()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(FLOW_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                flowScopedBeanMap.put(FLOW_SCOPE_MAP_KEY, result);
            }
        }
        mapHelper.updateSession();
        return result; 
    }
    
    
    
    static void executorExited(SCXMLExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        StateChartScopeMapHelper mapHelper = new StateChartScopeMapHelper(facesContext, executor);
        Map<String, Object> flowScopedBeanMap = mapHelper.getFlowScopedBeanMapForCurrentExecutor();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getFlowScopedCreationalMapForCurrentExecutor();
        assert(!flowScopedBeanMap.isEmpty());
        assert(!creationalMap.isEmpty());
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
        
        for (Map.Entry<String, Object> entry : flowScopedBeanMap.entrySet()) {
            String passivationCapableId = entry.getKey();
            if (FLOW_SCOPE_MAP_KEY.equals(passivationCapableId)) {
                continue;
            }
            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(passivationCapableId);
            
            owner.destroy(bean, creational);
        }
        
        flowScopedBeanMap.clear();
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
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper = 
                            (StateFlowCDIEventFireHelper)  beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireExecutorDestroyedEvent(executor);
                }
            }
        }
    }
    
    static void executorEntered(SCXMLExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        StateChartScopeMapHelper mapHelper = new StateChartScopeMapHelper(facesContext, executor);

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
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper = 
                            (StateFlowCDIEventFireHelper)  beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireExecutorInitializedEvent(executor);
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

    private SCXMLExecutor getCurrentExecutor() {
        FacesContext context = FacesContext.getCurrentInstance();
        SCXMLExecutor result = getCurrentExecutor(context);
        return result;
    }

    private static SCXMLExecutor getCurrentExecutor(FacesContext context) {

        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        if (null == flowHandler) {
            return null;
        }

        SCXMLExecutor result = flowHandler.getCurrentExecutor(context);

        return result;

    }

}
