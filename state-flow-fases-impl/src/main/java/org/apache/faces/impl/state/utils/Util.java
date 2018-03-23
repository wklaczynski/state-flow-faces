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
package org.apache.faces.impl.state.utils;

import com.sun.faces.util.LRUMap;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.context.FacesContext;
import org.apache.faces.impl.state.StateFlowConstants;
import org.apache.faces.impl.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Util {

    /**
     * Stores the logger.
     */
    public static final Logger log = FlowLogger.APPLICATION.getLogger();

    public static ClassLoader getCurrentLoader(Object fallbackClass) {
        ClassLoader loader = getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return loader;
    }

    @SuppressWarnings("Convert2Lambda")
    private static ClassLoader getContextClassLoader() {
        if (System.getSecurityManager() == null) {
            return Thread.currentThread().getContextClassLoader();
        } else {
            return (ClassLoader) java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction() {
                @Override
                public java.lang.Object run() {
                    return Thread.currentThread().getContextClassLoader();
                }
            });
        }
    }

    public static long getLastModified(URL url) {
        long lastModified;
        URLConnection conn;
        InputStream is = null;

        try {
            conn = url.openConnection();

            if (conn instanceof JarURLConnection) {
                /*
                 * Note this is a work around for JarURLConnection since the
                 * getLastModified method is buggy. See JAVASERVERFACES-2725
                 * and JAVASERVERFACES-2734.
                 */
                JarURLConnection jarUrlConnection = (JarURLConnection) conn;
                URL jarFileUrl = jarUrlConnection.getJarFileURL();
                URLConnection jarFileConnection = jarFileUrl.openConnection();
                lastModified = jarFileConnection.getLastModified();
                jarFileConnection.getInputStream().close();
            } else {
                is = conn.getInputStream();
                lastModified = conn.getLastModified();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Error Checking Last Modified for " + url, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    if (log.isLoggable(Level.FINEST)) {
                        log.log(Level.FINEST, "Closing stream", e);
                    }
                }
            }
        }
        return lastModified;
    }

    public static void postConstruct(Object instance) {
        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();
            Method postConstruct = null;
            for (Method method : methods) {
                if (method.isAnnotationPresent(PostConstruct.class)) {
                    if ((postConstruct != null) || (method.getParameterTypes().length != 0) || (Modifier.isStatic(method.getModifiers())) || (method.getExceptionTypes().length > 0) || (!method.getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PostConstruct annotation");
                    }
                    postConstruct = method;
                }
            }

            if (postConstruct != null) {
                try {
                    boolean accessibility = postConstruct.isAccessible();
                    postConstruct.setAccessible(true);
                    postConstruct.invoke(instance);
                    postConstruct.setAccessible(accessibility);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.log(Level.SEVERE, "Post Construct Error " + instance.getClass().getName(), ex);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    public static void preDestroy(Object instance) {
        Class<?> clazz = instance.getClass();

        while (clazz != null) {
            Method[] methods = clazz.getDeclaredMethods();

            Method preDestroy = null;
            for (Method method : methods) {
                if (method.isAnnotationPresent(PreDestroy.class)) {
                    if ((preDestroy != null) || (method.getParameterTypes().length != 0) || (Modifier.isStatic(method.getModifiers())) || (method.getExceptionTypes().length > 0) || (!method.getReturnType().getName().equals("void"))) {
                        throw new IllegalArgumentException("Invalid PreDestroy annotation");
                    }
                    preDestroy = method;
                }
            }

            if (preDestroy != null) {
                try {
                    boolean accessibility = preDestroy.isAccessible();
                    preDestroy.setAccessible(true);
                    preDestroy.invoke(instance);
                    preDestroy.setAccessible(accessibility);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    log.log(Level.SEVERE, "Pre Destroy Error " + instance.getClass().getName(), ex);
                }
            }

            clazz = clazz.getSuperclass();
        }
    }

    private static final String FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY = Util.class.getName() + "_FACES_CONTEXT_ATTRS_XMLDECL_KEY";

    public static void saveXMLDECLToFacesContextAttributes(String XMLDECL) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (null == context) {
            return;
        }
        Map<Object, Object> attrs = context.getAttributes();
        attrs.put(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY, XMLDECL);

    }

    public static String getXMLDECLFromFacesContextAttributes(FacesContext context) {
        if (null == context) {
            return null;
        }
        Map<Object, Object> attrs = context.getAttributes();
        return (String) attrs.get(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY);
    }

    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[^/]+$");

    public static String getExtension(String alias) {
        String ext = null;

        if (alias != null) {
            Matcher matcher = EXTENSION_PATTERN.matcher(alias);
            if (matcher.find()) {
                ext = alias.substring(matcher.start(), matcher.end());
            }
        }

        return (ext == null) ? "xhtml" : ext;
    }

    public synchronized static String[] split(Map<String, Object> appMap, String toSplit, String regex) {
        Map<String, Pattern> patternCache = getPatternCache(appMap);
        Pattern pattern = patternCache.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }
        return pattern.split(toSplit, 0);
    }

    private static final String patternCacheKey = StateFlowConstants.STATE_FLOW_PREFIX + "patternCache";
    
    private static Map<String, Pattern> getPatternCache(Map<String, Object> appMap) {
        @SuppressWarnings("unchecked")
        Map<String, Pattern> result = (Map<String, Pattern>) appMap.get(patternCacheKey);
        if (result == null) {
            result = new LRUMap<>(15);
            appMap.put(patternCacheKey, result);
        }

        return result;
    }
    
    public static StateManager getStateManager(FacesContext context) throws FacesException {
        return (context.getApplication().getStateManager());
    }
    
    public static String getErrorMessage(Throwable th) {
        String result = th.getMessage();
        
        while(th.getCause() != null) {
            th = th.getCause();
            result = th.getMessage();
        }
        return result;
    }

}
