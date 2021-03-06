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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
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
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.ssoft.faces.impl.state.StateFlowImplConstants;
import org.ssoft.faces.impl.state.log.FlowLogger;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.Parallel;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.state.scxml.model.TransitionTarget;
import javax.faces.state.annotation.StateScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateCDIContext implements Context, Serializable {

    private static final String TARGET_SCOPE_KEY = "targetscope";
    private static final Logger LOGGER = FlowLogger.CDI.getLogger();
    private static final String TARGET_SCOPE_MAP_KEY = StateFlowImplConstants.STATE_FLOW_PREFIX + "STATE_TARGET_SCOPE_MAP";

    private final Map<Contextual<?>, TargetBeanInfo> targetIds;

    static class TargetBeanInfo {

        String id;
        Type baseType;

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + Objects.hashCode(this.id);
            hash = 83 * hash + Objects.hashCode(this.baseType);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TargetBeanInfo other = (TargetBeanInfo) obj;
            if (!Objects.equals(this.id, other.id)) {
                return false;
            }
            return Objects.equals(this.baseType, other.baseType);
        }


        @Override
        public String toString() {
            return "TargetBeanInfo{" + id + '}';
        }

    }

    StateCDIContext(Map<Contextual<?>, TargetBeanInfo> flowIds) {
        this.targetIds = new ConcurrentHashMap<>(flowIds);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return StateScoped.class;
    }

    @Override
    public <T> T get(Contextual<T> contextual, CreationalContext<T> creational) {
        assertNotReleased();

        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateTargetScoped " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = getExecutor(facesContext);

        TargetBeanInfo tbi = targetIds.get(contextual);
        TransitionTarget state = findState(facesContext, executor, tbi.id);

        StateScopeMapHelper mapHelper = StateScopeMapHelper.state(facesContext, executor, TARGET_SCOPE_KEY);

        T result = get(mapHelper, contextual, executor, state);

        if (null == result && creational != null) {
            javax.faces.state.scxml.Context scopedBeanMap = mapHelper.getScopeBeanContext();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMap();

            String passivationCapableId = ((PassivationCapable) contextual).getId();
            String beanId = state.getId() + ":" + passivationCapableId;

            synchronized (scopedBeanMap) {
                result = (T) scopedBeanMap.get(beanId);
                if (null == result) {

                    if (null == executor) {
                        throw new ContextNotActiveException("Request to activate bean in state, but that executor is not active.");
                    }

                    if (!executor.isRunning()) {
                        LOGGER.warning("Request to activate bean in state, but that executor is not running.");
                    }

                    if (!executor.getStatus().isInState(state.getId())) {
                        throw new ContextNotActiveException("Request to activate bean in state '" + tbi + "', but that state is not active.");
                    }

                    result = contextual.create(creational);

                    if (null != result) {
                        scopedBeanMap.setLocal(beanId, result);
                        creationalMap.put(beanId, creational);
                        mapHelper.updateSession();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public <T> T get(Contextual<T> contextual) {
        assertNotReleased();

        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateTargetScoped " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = getExecutor(facesContext);

        if (!executor.isRunning()) {
            LOGGER.warning("Request to bean in state, but that executor is not running.");
        }

        TargetBeanInfo tbi = targetIds.get(contextual);
        TransitionTarget state = findState(facesContext, executor, tbi.id);

        if (state == null) {
            throw new ContextNotActiveException("@StateScoped(\""
                    + tbi.id 
                    + "\") annotated \""
                    + tbi.baseType.getTypeName()
                    + "\", can be open only in state\"" + tbi.id 
                    +"\", but that state is not active.");
        }

        StateScopeMapHelper mapHelper = StateScopeMapHelper.state(facesContext, executor, TARGET_SCOPE_KEY);
        T result = get(mapHelper, contextual, executor, state);

        return result;
    }

    private <T> T get(StateScopeMapHelper mapHelper, Contextual<T> contextual, SCXMLExecutor executor, TransitionTarget state) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateChartScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        if (!executor.isRunning()) {
            LOGGER.warning("Request to bean in state, but that executor is not running.");
        }

        if (!executor.getStatus().isInState(state.getId())) {
            return null;
        }

        String passivationCapableId = ((PassivationCapable) contextual).getId();

        String beanId = state.getId() + ":" + passivationCapableId;
        javax.faces.state.scxml.Context map = mapHelper.getScopeBeanContext();
        return (T) map.get(beanId);
    }

    @Override
    public boolean isActive() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = getExecutor(facesContext);
        StateScopeMapHelper mapHelper = StateScopeMapHelper.state(facesContext, executor, TARGET_SCOPE_KEY);
        return mapHelper.isActive();
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
        javax.faces.state.scxml.Context flowScopedBeanMap = mapHelper.getScopeBeanContext();
        Map<Object, Object> result = null;
        if (mapHelper.isActive()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(TARGET_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                flowScopedBeanMap.setLocal(TARGET_SCOPE_MAP_KEY, result);
            }
        }
        mapHelper.updateSession();
        return result;
    }

    static void executorExited(SCXMLExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        StateScopeMapHelper mapHelper = StateScopeMapHelper.state(facesContext, executor, TARGET_SCOPE_KEY);
        javax.faces.state.scxml.Context flowScopedBeanMap = mapHelper.getScopeBeanContext();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMap();
        assert (!flowScopedBeanMap.getVars().isEmpty());
        assert (!creationalMap.isEmpty());
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);

        for (Map.Entry<String, Object> entry : flowScopedBeanMap.getVars().entrySet()) {
            String beanId = entry.getKey();
            if (TARGET_SCOPE_MAP_KEY.equals(beanId)) {
                continue;
            }

            String passivationCapableId = beanId;
            int sep = passivationCapableId.indexOf(":");
            if (sep > -1) {
                passivationCapableId = passivationCapableId.substring(
                        sep + 1, passivationCapableId.length());
            }

            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(beanId);

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
                    eventHelper.fireTargetExecutorDestroyedEvent(executor);
                }
            }
        }
    }

    static void executorEntered(SCXMLExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        StateScopeMapHelper mapHelper = StateScopeMapHelper.state(facesContext, executor, TARGET_SCOPE_KEY);

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
                    eventHelper.fireTargetExecutorInitializedEvent(executor);
                }
            }
        }
    }

//    static void stateExited(SCXMLExecutor executor, EnterableState state) {
//        FacesContext facesContext = FacesContext.getCurrentInstance();
//
//        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);
//        Map<String, Object> scopedBeanMap = mapHelper.getContextForCurrentExecutor();
//        Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMap();
//        assert (!scopedBeanMap.isEmpty());
//        assert (!creationalMap.isEmpty());
//
//        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
//
//        String prefix = state.getId() + ":";
//
//        Set<String> toRemove = new HashSet<>();
//
//        for (Map.Entry<String, Object> entry : scopedBeanMap.entrySet()) {
//            String beanId = entry.getKey();
//            if (TARGET_SCOPE_MAP_KEY.equals(beanId)) {
//                continue;
//            }
//            if (!prefix.startsWith(beanId)) {
//                continue;
//            }
//            toRemove.add(beanId);
//            String passivationCapableId = beanId.substring(prefix.length());
//
//            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
//            Object bean = entry.getValue();
//            CreationalContext creational = creationalMap.flow(beanId);
//
//            owner.destroy(bean, creational);
//        }
//
//        for (String key : toRemove) {
//            scopedBeanMap.remove(key);
//            creationalMap.remove(key);
//        }
//
//        mapHelper.updateSession();
//
//        if (CdiUtil.isCdiOneOneOrLater(facesContext)) {
//            Class flowCDIEventFireHelperImplClass = null;
//            try {
//                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
//            } catch (ClassNotFoundException ex) {
//                if (LOGGER.isLoggable(Level.SEVERE)) {
//                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
//                }
//            }
//
//            if (null != flowCDIEventFireHelperImplClass) {
//                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
//                if (null != availableBeans && !availableBeans.isEmpty()) {
//                    Bean<?> bean = beanManager.resolve(availableBeans);
//                    CreationalContext<?> creationalContext
//                            = beanManager.createCreationalContext(null);
//                    StateFlowCDIEventFireHelper eventHelper
//                            = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
//                                    creationalContext);
//                    eventHelper.fireExecutorDestroyedEvent(executor);
//                }
//            }
//        }
//    }
//
//    static void stateEntered(SCXMLExecutor executor, EnterableState state) {
//        FacesContext facesContext = FacesContext.getCurrentInstance();
//        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);
//
//        mapHelper.createMaps();
//        getCurrentFlowScopeAndUpdateSession(mapHelper);
//
//        if (CdiUtil.isCdiOneOneOrLater(facesContext)) {
//            Class flowCDIEventFireHelperImplClass = null;
//            try {
//                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
//            } catch (ClassNotFoundException ex) {
//                if (LOGGER.isLoggable(Level.SEVERE)) {
//                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
//                }
//            }
//            if (null != flowCDIEventFireHelperImplClass) {
//                BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
//                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
//                if (null != availableBeans && !availableBeans.isEmpty()) {
//                    Bean<?> bean = beanManager.resolve(availableBeans);
//                    CreationalContext<?> creationalContext
//                            = beanManager.createCreationalContext(null);
//                    StateFlowCDIEventFireHelper eventHelper
//                            = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
//                                    creationalContext);
//                    eventHelper.fireExecutorInitializedEvent(executor);
//                }
//            }
//        }
//    }
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

        SCXMLExecutor result = flowHandler.getCurrentExecutor(context);
        return result;
    }

    @SuppressWarnings("ConvertToStringSwitch")
    private static TransitionTarget findState(FacesContext context, SCXMLExecutor executor, String selector) {
        if (executor == null) {
            return null;
        }
        TransitionTarget result = null;

        if (selector.equals("@common")) {
            Set<EnterableState> states = executor.getStatus().getStates();
            result = selectCommonState(states);
        } else if (selector.equals("@top")) {
            Set<EnterableState> states = executor.getStatus().getStates();
            result = selectTopState(states);
        } else if (selector.equals("@composite")) {
            Set<EnterableState> states = executor.getStatus().getStates();
            result = selectFirstCompositeState(states);
        } else {
            SCXML stateMachine = executor.getStateMachine();
            TransitionTarget tt = stateMachine.getTargets().get(selector);
            if (tt instanceof EnterableState) {
                result = tt;
            }
        }

        return result;

    }

    private static EnterableState selectCommonState(Set<EnterableState> states) {
        if (states.isEmpty()) {
            return null;
        }
        EnterableState result = states.iterator().next();
        if (states.size() > 1) {
            for (int i = 0; i < result.getNumberOfAncestors(); i++) {
                EnterableState ancestor = result.getAncestor(i);
                if (ancestor instanceof Parallel) {
                    result = ancestor;
                    break;
                }
            }
        }
        return result;
    }

    private static EnterableState selectFirstCompositeState(Set<EnterableState> states) {
        EnterableState result = selectCommonState(states);
        while (result.getParent() != null && result.isAtomicState()) {
            result = result.getParent();
        }
        return result;
    }

    private static EnterableState selectTopState(Set<EnterableState> states) {
        if (!states.isEmpty()) {
            return null;
        }
        EnterableState result = states.iterator().next();
        if (result.getNumberOfAncestors() > 0) {
            result = result.getAncestor(0);
        }
        return result;
    }

}
