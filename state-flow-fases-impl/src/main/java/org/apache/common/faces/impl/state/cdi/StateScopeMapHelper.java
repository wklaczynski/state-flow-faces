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
import javax.servlet.http.HttpSessionEvent;
import static org.apache.common.faces.impl.state.StateFlowConstants.STATE_FLOW_PREFIX;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLSystemContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateScopeMapHelper {

    private transient String sessionBeanMapListKey;
    private transient String sessionCreationalMapListKey;
    
    private transient String beansForExecutorKey;
    private transient String creationalForExecutorKey;
    private transient Map<String, Object> sessionMap;
    private final String prefix;

    public StateScopeMapHelper(FacesContext facesContext, SCXMLExecutor executor, String prefix) {
        this(prefix);
        ExternalContext extContext = facesContext.getExternalContext();
        this.sessionMap = extContext.getSessionMap();

        generateKeyForCDIBeansBelongToAExecutor(facesContext, executor);
    }

    public StateScopeMapHelper(String prefix) {
        this.prefix = prefix;
        sessionBeanMapListKey = STATE_FLOW_PREFIX + "." + prefix + ".PER_SESSION_BEAN_MAP_LIST";
        sessionCreationalMapListKey = STATE_FLOW_PREFIX + "." + prefix + ".PER_SESSION_CREATIONAL_LIST";
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

    public void createMaps() {
        getScopedBeanMapForCurrentExecutor();
        getScopedCreationalMapForCurrentExecutor();
    }

    public boolean isExecutorExists() {
        return (null != beansForExecutorKey && null != creationalForExecutorKey);
    }

    public String getCreationalForExecutorKey() {
        return creationalForExecutorKey;
    }

    public String getBeansForExecutorKey() {
        return beansForExecutorKey;
    }

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

    public void updateSession() {
        if (null == beansForExecutorKey && null == creationalForExecutorKey) {
            return;
        }

        sessionMap.put(beansForExecutorKey, getScopedBeanMapForCurrentExecutor());
        sessionMap.put(creationalForExecutorKey, getScopedCreationalMapForCurrentExecutor());
        Object obj = sessionMap.get(sessionBeanMapListKey);
        if (null != obj) {
            sessionMap.put(sessionBeanMapListKey, obj);
        }
        obj = sessionMap.get(sessionCreationalMapListKey);
        if (null != obj) {
            sessionMap.put(sessionCreationalMapListKey, obj);
        }
    }

    private void ensureBeanMapCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String flowBeansForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(sessionBeanMapListKey);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(sessionBeanMapListKey, beanMapList);
        }
        beanMapList.add(flowBeansForClientWindow);
    }

    private void ensureCreationalCleanupOnSessionDestroyed(Map<String, Object> sessionMap, String creationalForClientWindow) {
        List<String> beanMapList = (List<String>) sessionMap.get(sessionCreationalMapListKey);
        if (null == beanMapList) {
            beanMapList = new ArrayList<>();
            sessionMap.put(sessionCreationalMapListKey, beanMapList);
        }
        beanMapList.add(creationalForClientWindow);
    }

    public void sessionDestroyed(HttpSession session) {
        List<String> beanMapList = (List<String>) session.getAttribute(sessionBeanMapListKey);
        if (null != beanMapList) {
            for (String cur : beanMapList) {
                Map<Contextual<?>, Object> beanMap
                        = (Map<Contextual<?>, Object>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(sessionBeanMapListKey);
            beanMapList.clear();
        }

        List<String> creationalList = (List<String>) session.getAttribute(sessionCreationalMapListKey);
        if (null != creationalList) {
            for (String cur : creationalList) {
                Map<Contextual<?>, CreationalContext<?>> beanMap
                        = (Map<Contextual<?>, CreationalContext<?>>) session.getAttribute(cur);
                beanMap.clear();
                session.removeAttribute(cur);
            }
            session.removeAttribute(sessionCreationalMapListKey);
            creationalList.clear();
        }

    }

}
