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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.Context;
import javax.servlet.http.HttpSession;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.env.SimpleContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateScopeMapHelper {

    private static final String PER_SESSION_BEAN_MAP_LIST = StateScopeMapHelper.class.getPackage().getName() + ".PER_SESSION_BEAN_MAP_LIST";
    private static final String PER_SESSION_CREATIONAL_LIST = StateScopeMapHelper.class.getPackage().getName() + ".PER_SESSION_CREATIONAL_LIST";

    private transient Map<String, Object> sessionMap;
    private Context context;
    private final String prefix;
    private String sessionId;

    public static StateScopeMapHelper flow(FacesContext facesContext, String prefix) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String sessionId = handler.getExecutorViewRootId(facesContext);
        Context context = handler.getFlowContext(facesContext, null);
        return new StateScopeMapHelper(facesContext, context, prefix, sessionId);
    }

    public static StateScopeMapHelper dialog(FacesContext facesContext, SCXMLExecutor executor, String prefix) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        Context context = handler.getFlowContext(facesContext, null);
        String sessionId = null;
        if (executor != null) {
            sessionId = executor.getId();
        } else {
            sessionId = handler.getExecutorViewRootId(facesContext);
        }
        return new StateScopeMapHelper(facesContext, context, prefix, sessionId);
    }

    public static StateScopeMapHelper chart(FacesContext facesContext, SCXMLExecutor executor, String prefix) {
        return get(facesContext, executor, prefix, true);
    }

    public static StateScopeMapHelper state(FacesContext facesContext, SCXMLExecutor executor, String prefix) {
        return get(facesContext, executor, prefix, true);
    }

    private static StateScopeMapHelper get(FacesContext facesContext, SCXMLExecutor executor, String prefix, boolean root) {
        Context context = null;
        String sessionId = null;
        if (executor != null) {
            sessionId = executor.getId();
            if (root) {
                context = executor.getRootContext();
            } else {
                context = executor.getGlobalContext();
            }
        }
        return new StateScopeMapHelper(facesContext, context, prefix, sessionId);
    }

    /**
     *
     * @param facesContext
     * @param context
     * @param prefix
     * @param sessionId
     */
    public StateScopeMapHelper(FacesContext facesContext, Context context, String prefix, String sessionId) {
        this(prefix);
        ExternalContext extContext = facesContext.getExternalContext();
        this.sessionMap = extContext.getSessionMap();
        this.context = context;
        this.sessionId = sessionId;
    }

    /**
     *
     * @param prefix
     */
    public StateScopeMapHelper(String prefix) {
        this.prefix = prefix;
    }

    private String generateKeyForCDIBeansBelong(String currentSessionId, String sufix) {
        return prefix + ":" + currentSessionId + sufix;
    }

    /**
     *
     */
    public void createMaps() {
        getContextForCurrentExecutor();
        getScopedCreationalMap();
    }

    public Context getContext() {
        return context;
    }

    /**
     *
     * @return
     */
    public boolean isActive() {
        return context != null;
    }

    /**
     *
     * @return
     */
    private String getCreationalForExecutorKey() {
        if (!isActive()) {
            return null;
        }
        return generateKeyForCDIBeansBelong(sessionId, "_creational");
    }

    /**
     *
     * @return
     */
    private String getBeansForExecutorKey() {
        return generateKeyForCDIBeansBelong(sessionId, "_beans");
    }

    /**
     *
     * @return
     */
    public Context getContextForCurrentExecutor() {
        if (!isActive()) {
            return new SimpleContext();
        }
        ScopedBeanContext result;
        String beansForExecutorKey = getBeansForExecutorKey();

        result = (ScopedBeanContext) sessionMap.get(beansForExecutorKey);
        if (null == result) {
            result = new ScopedBeanContext();
            sessionMap.put(beansForExecutorKey, result);
            ensureBeanMapCleanupOnSessionDestroyed(sessionMap, beansForExecutorKey);
        }
        return result;
    }

    /**
     *
     * @return
     */
    public Context getContextForRootExecutor() {
        if (!isActive()) {
            return new SimpleContext();
        }

        ScopedBeanContext result;
        String beansForExecutorKey = getBeansForExecutorKey();
        result = (ScopedBeanContext) sessionMap.get(beansForExecutorKey);
        if (null == result) {
            result = new ScopedBeanContext();
            sessionMap.put(beansForExecutorKey, result);
            ensureBeanMapCleanupOnSessionDestroyed(sessionMap, beansForExecutorKey);
        }
        return result;
    }

    /**
     *
     * @return
     */
    public Map<String, CreationalContext<?>> getScopedCreationalMap() {
        if (!isActive()) {
            return Collections.emptyMap();
        }

        Map<String, CreationalContext<?>> result;
        String creationalForExecutorKey = getCreationalForExecutorKey();
        result = (Map<String, CreationalContext<?>>) sessionMap.get(creationalForExecutorKey);
        if (null == result) {
            result = new ConcurrentHashMap<>();
            sessionMap.put(creationalForExecutorKey, result);
            ensureCreationalCleanupOnSessionDestroyed(sessionMap, creationalForExecutorKey);
        }
        return result;
    }

    /**
     *
     */
    public void updateSession() {
        if (!isActive()) {
            return;
        }

        String beansForExecutorKey = getBeansForExecutorKey();
        String creationalForExecutorKey = getCreationalForExecutorKey();

        sessionMap.put(beansForExecutorKey, getContextForCurrentExecutor());
        sessionMap.put(creationalForExecutorKey, getScopedCreationalMap());
        Object obj = sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (null != obj) {
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, obj);
        }
        obj = sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (null != obj) {
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, obj);
        }
    }

    private static void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String beansForExecutor) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_BEAN_MAP_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_BEAN_MAP_LIST, beanMapList);
        }
        beanMapList.add(beansForExecutor);
    }

    private static void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForExecutor) {
        List<String> beanMapList = (List<String>) sessionMap.get(PER_SESSION_CREATIONAL_LIST);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(PER_SESSION_CREATIONAL_LIST, beanMapList);
        }
        beanMapList.add(creationalForExecutor);
    }

    /**
     *
     * @param session
     */
    public static void sessionDestroyed(HttpSession session) {
        List<String> beanMapList = (List<String>) session.getAttribute(PER_SESSION_BEAN_MAP_LIST);
        if (null != beanMapList) {
            for (String cur : beanMapList) {
                ScopedBeanContext beanMap
                        = (ScopedBeanContext) session.getAttribute(cur);
                beanMap.getVars().clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_BEAN_MAP_LIST);
            beanMapList.clear();
        }

        List<String> creationalList = (List<String>) session.getAttribute(PER_SESSION_CREATIONAL_LIST);
        if (null != creationalList) {
            for (String cur : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> beanMap
                        = (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(PER_SESSION_CREATIONAL_LIST);
            creationalList.clear();
        }

    }

}
