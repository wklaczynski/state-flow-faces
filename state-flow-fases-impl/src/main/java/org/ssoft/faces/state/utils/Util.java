/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.utils;

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
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ssoft.faces.state.FlowConstants;
import org.ssoft.faces.state.log.FlowLogger;
import org.ssoft.faces.state.cdi.StateFlowCDIExtension;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Util {

    // Log instance for this class
    private static final Logger LOGGER = FlowLogger.APPLICATION.getLogger();

    /**
     * Stores the logger.
     */
    private final static Log log = LogFactory.getLog(StateFlowCDIExtension.class);

    public static boolean isCdiOneOneOrGreater() {

        // The following try/catch is a hack to discover
        // if CDI 1.1 or greater is available
        boolean result = false;
        try {
            Class.forName("javax.enterprise.context.Initialized");
            result = true;
        } catch (ClassNotFoundException ignored) {
            if (log.isDebugEnabled()) {
                log.debug("Dected CDI 1.0", ignored);
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

        if (facesContext != null && facesContext.getAttributes().containsKey(FlowConstants.CDI_1_1_OR_LATER)) {
            result = (Boolean) facesContext.getAttributes().get(FlowConstants.CDI_1_1_OR_LATER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(FlowConstants.CDI_1_1_OR_LATER)) {
            result = facesContext.getExternalContext().getApplicationMap().containsKey(FlowConstants.CDI_1_1_OR_LATER);
        } else {
            try {
                Class.forName("javax.enterprise.context.Initialized");
                result = true;
            } catch (ClassNotFoundException ignored) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Detected CDI 1.0", ignored);
                }
            }

            if (facesContext != null) {
                facesContext.getAttributes().put(FlowConstants.CDI_1_1_OR_LATER, result);
                facesContext.getExternalContext().getApplicationMap().put(FlowConstants.CDI_1_1_OR_LATER, result);
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

        if (facesContext != null && facesContext.getAttributes().containsKey(FlowConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getAttributes().get(FlowConstants.CDI_BEAN_MANAGER);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(FlowConstants.CDI_BEAN_MANAGER)) {
            result = (BeanManager) facesContext.getExternalContext().getApplicationMap().get(FlowConstants.CDI_BEAN_MANAGER);
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
                facesContext.getAttributes().put(FlowConstants.CDI_BEAN_MANAGER, result);
                facesContext.getExternalContext().getApplicationMap().put(FlowConstants.CDI_BEAN_MANAGER, result);
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

        if (facesContext != null && facesContext.getAttributes().containsKey(FlowConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getAttributes().get(FlowConstants.CDI_AVAILABLE);
        } else if (facesContext != null && facesContext.getExternalContext().getApplicationMap().containsKey(FlowConstants.CDI_AVAILABLE)) {
            result = (Boolean) facesContext.getExternalContext().getApplicationMap().get(FlowConstants.CDI_AVAILABLE);
        } else {
            result = getCdiBeanManager(facesContext) != null;

            if (result && facesContext != null) {
                facesContext.getAttributes().put(FlowConstants.CDI_AVAILABLE, result);
                facesContext.getExternalContext().getApplicationMap().put(FlowConstants.CDI_AVAILABLE, result);
            }
        }

        return result;
    }

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
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.log(Level.FINEST, "Closing stream", e);
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
                    LOGGER.log(Level.SEVERE, "Post Construct Error " + instance.getClass().getName(), ex);
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
                    LOGGER.log(Level.SEVERE, "Pre Destroy Error " + instance.getClass().getName(), ex);
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

        return (ext == null) ? "xhtml": ext;
    }
    
}
