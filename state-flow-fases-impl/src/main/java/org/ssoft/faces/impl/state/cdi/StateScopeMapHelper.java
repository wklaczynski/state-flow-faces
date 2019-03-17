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
import javax.servlet.http.HttpSession;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLSystemContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateScopeMapHelper {

    private static final String PER_SESSION_BEAN_MAP_LIST = StateScopeMapHelper.class.getPackage().getName() + ".PER_SESSION_BEAN_MAP_LIST";
    private static final String PER_SESSION_CREATIONAL_LIST = StateScopeMapHelper.class.getPackage().getName() + ".PER_SESSION_CREATIONAL_LIST";
    
    private transient String beansForExecutorKey;
    private transient String creationalForExecutorKey;
    private transient Map<String, Object> sessionMap;
    private final String prefix;

    /**
     *
     * @param facesContext
     * @param executor
     * @param prefix
     */
    public StateScopeMapHelper(FacesContext facesContext, SCXMLExecutor executor, String prefix) {
        this(prefix);
        ExternalContext extContext = facesContext.getExternalContext();
        this.sessionMap = extContext.getSessionMap();

        generateKeyForCDIBeansBelongToAExecutor(facesContext, executor);
    }

    /**
     *
     * @param prefix
     */
    public StateScopeMapHelper(String prefix) {
        this.prefix = prefix;
    }
    
    private void generateKeyForCDIBeansBelongToAExecutor(FacesContext facesContext, SCXMLExecutor executor) {
        if (null != executor) {

            String currentSessionId = (String) executor.getSCInstance().getSystemContext().get(SCXMLSystemContext.SESSIONID_KEY);

            beansForExecutorKey = prefix + ":" + currentSessionId + "_beans";
            creationalForExecutorKey = prefix + ":" + currentSessionId + "_creational";

        } else {
            beansForExecutorKey = creationalForExecutorKey = null;
        }
    }

    /**
     *
     */
    public void createMaps() {
        getScopedBeanMapForCurrentExecutor();
        getScopedCreationalMapForCurrentExecutor();
    }

    /**
     *
     * @return
     */
    public boolean isExecutorExists() {
        return (null != beansForExecutorKey && null != creationalForExecutorKey);
    }

    /**
     *
     * @return
     */
    public String getCreationalForExecutorKey() {
        return creationalForExecutorKey;
    }

    /**
     *
     * @return
     */
    public String getBeansForExecutorKey() {
        return beansForExecutorKey;
    }

    /**
     *
     * @return
     */
    public Map<String, Object> getScopedBeanMapForCurrentExecutor() {
        if (null == beansForExecutorKey && null == creationalForExecutorKey) {
            return Collections.emptyMap();
        }
        Map<String, Object> result;
        result = (Map<String, Object>) sessionMap.get(beansForExecutorKey);
        if (null == result) {
            result = new ConcurrentHashMap<>();
            sessionMap.put(beansForExecutorKey, result);
            ensureBeanMapCleanupOnSessionDestroyed(sessionMap, beansForExecutorKey);
        }
        return result;
    }

    /**
     *
     * @return
     */
    public Map<String, CreationalContext<?>> getScopedCreationalMapForCurrentExecutor() {
        if (null == beansForExecutorKey && null == creationalForExecutorKey) {
            return Collections.emptyMap();
        }
        Map<String, CreationalContext<?>> result;
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
        if (null == beansForExecutorKey && null == creationalForExecutorKey) {
            return;
        }

        sessionMap.put(beansForExecutorKey, getScopedBeanMapForCurrentExecutor());
        sessionMap.put(creationalForExecutorKey, getScopedCreationalMapForCurrentExecutor());
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
                Map<Contextual<?>, Object> beanMap
                        = (Map<Contextual<?>, Object>) session.getAttribute(cur);
                beanMap.clear();
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
