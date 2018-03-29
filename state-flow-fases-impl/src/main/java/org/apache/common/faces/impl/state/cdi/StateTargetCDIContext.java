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
package org.apache.common.faces.impl.state.cdi;

import com.sun.faces.util.Util;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.HashSet;
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
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.apache.common.faces.impl.state.StateFlowConstants;
import org.apache.common.faces.impl.state.log.FlowLogger;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.annotation.StateTargetScoped;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.model.EnterableState;
import org.apache.common.scxml.model.Parallel;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateTargetCDIContext implements Context, Serializable {

    private static final String TARGET_SCOPE_KEY = "targetscope";
    private static final Logger LOGGER = FlowLogger.FLOW.getLogger();
    private static final String TARGET_SCOPE_MAP_KEY = StateFlowConstants.STATE_FLOW_PREFIX + "STATE_TARGET_SCOPE_MAP";

    private final Map<Contextual<?>, TargetBeanInfo> targetIds;

    static class TargetBeanInfo {

        String id;

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final TargetBeanInfo other = (TargetBeanInfo) obj;
            return !((this.id == null) ? (other.id != null) : !this.id.equals(other.id));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "TargetBeanInfo{" + id + '}';
        }

    }

    StateTargetCDIContext(Map<Contextual<?>, TargetBeanInfo> flowIds) {
        this.targetIds = new ConcurrentHashMap<>(flowIds);
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return StateTargetScoped.class;
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

        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);

        T result = get(mapHelper, contextual, executor, state);

        if (null == result && creational != null) {
            Map<String, Object> scopedBeanMap = mapHelper.getScopedBeanMapForCurrentExecutor();
            Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMapForCurrentExecutor();

            String passivationCapableId = ((PassivationCapable) contextual).getId();
            String beanId = state.getId() + ":" + passivationCapableId;

            synchronized (scopedBeanMap) {
                result = (T) scopedBeanMap.get(beanId);
                if (null == result) {

                    if (null == executor) {
                        throw new ContextNotActiveException("Request to activate bean in state, but that executor is not active.");
                    }

                    if (!executor.isRunning()) {
                        throw new ContextNotActiveException("Request to activate bean in state, but that executor is not running.");
                    }

                    if (!executor.getStatus().isInState(state.getId())) {
                        throw new ContextNotActiveException("Request to activate bean in state '" + tbi + "', but that state is not active.");
                    }

                    result = contextual.create(creational);

                    if (null != result) {
                        scopedBeanMap.put(beanId, result);
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
            throw new ContextNotActiveException("Request to bean in state, but that executor is not running.");
        }

        TargetBeanInfo tbi = targetIds.get(contextual);
        TransitionTarget state = findState(facesContext, executor, tbi.id);

        if (state == null) {
            throw new ContextNotActiveException("StateTargetScoped '" + tbi + "', must be defined state, but that state is not active.");
        }

        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);
        T result = get(mapHelper, contextual, executor, state);

        return result;
    }

    private <T> T get(StateScopeMapHelper mapHelper, Contextual<T> contextual, SCXMLExecutor executor, TransitionTarget state) {
        assertNotReleased();
        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("StateChartScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        if (!executor.isRunning()) {
            return null;
        }

        if (!executor.getStatus().isInState(state.getId())) {
            return null;
        }

        String passivationCapableId = ((PassivationCapable) contextual).getId();

        String beanId = state.getId() + ":" + passivationCapableId;
        Map<String, Object> map = mapHelper.getScopedBeanMapForCurrentExecutor();
        return (T) map.get(beanId);
    }

    @Override
    public boolean isActive() {
        SCXMLExecutor executor = getExecutor();
        return null != executor && executor.isRunning();
    }

    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        StateScopeMapHelper.sessionDestroyed(session);
    }

    private static Map<Object, Object> getCurrentFlowScopeAndUpdateSession(StateScopeMapHelper mapHelper) {
        Map<String, Object> flowScopedBeanMap = mapHelper.getScopedBeanMapForCurrentExecutor();
        Map<Object, Object> result = null;
        if (mapHelper.isExecutorExists()) {
            result = (Map<Object, Object>) flowScopedBeanMap.get(TARGET_SCOPE_MAP_KEY);
            if (null == result) {
                result = new ConcurrentHashMap<>();
                flowScopedBeanMap.put(TARGET_SCOPE_MAP_KEY, result);
            }
        }
        mapHelper.updateSession();
        return result;
    }

    static void executorExited(SCXMLExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);
        Map<String, Object> flowScopedBeanMap = mapHelper.getScopedBeanMapForCurrentExecutor();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMapForCurrentExecutor();
        assert (!flowScopedBeanMap.isEmpty());
        assert (!creationalMap.isEmpty());
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);

        for (Map.Entry<String, Object> entry : flowScopedBeanMap.entrySet()) {
            String passivationCapableId = entry.getKey();
            if (TARGET_SCOPE_MAP_KEY.equals(passivationCapableId)) {
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

        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);

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

    static void stateExited(SCXMLExecutor executor, EnterableState state) {
        FacesContext facesContext = FacesContext.getCurrentInstance();

        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);
        Map<String, Object> scopedBeanMap = mapHelper.getScopedBeanMapForCurrentExecutor();
        Map<String, CreationalContext<?>> creationalMap = mapHelper.getScopedCreationalMapForCurrentExecutor();
        assert (!scopedBeanMap.isEmpty());
        assert (!creationalMap.isEmpty());

        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);

        String prefix = state.getId() + ":";

        Set<String> toRemove = new HashSet<>();

        for (Map.Entry<String, Object> entry : scopedBeanMap.entrySet()) {
            String beanId = entry.getKey();
            if (TARGET_SCOPE_MAP_KEY.equals(beanId)) {
                continue;
            }
            if (!prefix.startsWith(beanId)) {
                continue;
            }
            toRemove.add(beanId);
            String passivationCapableId = beanId.substring(prefix.length());

            Contextual owner = beanManager.getPassivationCapableBean(passivationCapableId);
            Object bean = entry.getValue();
            CreationalContext creational = creationalMap.get(beanId);

            owner.destroy(bean, creational);
        }

        for (String key : toRemove) {
            scopedBeanMap.remove(key);
            creationalMap.remove(key);
        }

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
                    eventHelper.fireExecutorDestroyedEvent(executor);
                }
            }
        }
    }

    static void stateEntered(SCXMLExecutor executor, EnterableState state) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        StateScopeMapHelper mapHelper = new StateScopeMapHelper(facesContext, executor, TARGET_SCOPE_KEY);

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
