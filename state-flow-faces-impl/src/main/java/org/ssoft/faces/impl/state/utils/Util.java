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
package org.ssoft.faces.impl.state.utils;

import com.sun.faces.util.LRUMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Field;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.FacesException;
import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletContext;
import org.ssoft.faces.impl.state.StateFlowImplConstants;
import org.ssoft.faces.impl.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Util {

    /**
     * Stores the logger.
     */
    public static final Logger log = FlowLogger.APPLICATION.getLogger();

    /**
     *
     * @param fallbackClass
     * @return
     */
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

    /**
     *
     * @param url
     * @return
     */
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

    private static final String FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY = Util.class.getName() + "_FACES_CONTEXT_ATTRS_XMLDECL_KEY";

    /**
     *
     * @param XMLDECL
     */
    public static void saveXMLDECLToFacesContextAttributes(String XMLDECL) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (null == context) {
            return;
        }
        Map<Object, Object> attrs = context.getAttributes();
        attrs.put(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY, XMLDECL);

    }

    /**
     *
     * @param context
     * @return
     */
    public static String getXMLDECLFromFacesContextAttributes(FacesContext context) {
        if (null == context) {
            return null;
        }
        Map<Object, Object> attrs = context.getAttributes();
        return (String) attrs.get(FACES_CONTEXT_ATTRIBUTES_XMLDECL_KEY);
    }

    private static final Pattern EXTENSION_PATTERN = Pattern.compile("\\.[^/]+$");

    /**
     *
     * @param alias
     * @return
     */
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

    /**
     *
     * @param appMap
     * @param toSplit
     * @param regex
     * @return
     */
    public synchronized static String[] split(Map<String, Object> appMap, String toSplit, String regex) {
        Map<String, Pattern> patternCache = getPatternCache(appMap);
        Pattern pattern = patternCache.get(regex);
        if (pattern == null) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }
        return pattern.split(toSplit, 0);
    }

    private static final String patternCacheKey = StateFlowImplConstants.STATE_FLOW_PREFIX + "patternCache";
    
    private static Map<String, Pattern> getPatternCache(Map<String, Object> appMap) {
        @SuppressWarnings("unchecked")
        Map<String, Pattern> result = (Map<String, Pattern>) appMap.get(patternCacheKey);
        if (result == null) {
            result = new LRUMap<>(15);
            appMap.put(patternCacheKey, result);
        }

        return result;
    }
    
    /**
     *
     * @param context
     * @return
     * @throws FacesException
     */
    public static StateManager getStateManager(FacesContext context) throws FacesException {
        return (context.getApplication().getStateManager());
    }
    
    /**
     *
     * @param th
     * @return
     */
    public static String getErrorMessage(Throwable th) {
        String result = th.getMessage();
        
        while(th.getCause() != null) {
            th = th.getCause();
            result = th.getMessage();
        }
        return result;
    }

    /**
     *
     * @param varname
     * @param var
     */
    public static void notNull(String varname, Object var) {

        if (var == null) {
            throw new NullPointerException(String.format("valuue %s can not be null.", varname));
        }
        
    }
    
    /**
     *
     * @param resourceURL
     * @return
     * @throws IOException
     */
    public static String readResource(final URL resourceURL) throws IOException {
        try (InputStream in = resourceURL.openStream()) {
            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            StringBuilder out = new StringBuilder();
            Reader reader = new InputStreamReader(in, "UTF-8");
            for (;;) {
                int rsz = reader.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.append(buffer, 0, rsz);
            }
            String content = out.toString();
            return content;
        }
    }
    
    /**
     *
     * @param content
     * @return
     */
    public static String trimContent(final String content) {
        if (content != null) {
            int start = 0;
            int length = content.length();
            while (start < length && isWhiteSpace(content.charAt(start))) {
                start++;
            }
            while (length > start && isWhiteSpace(content.charAt(length - 1))) {
                length--;
            }
            if (start == length) {
                return "";
            }
            return content.substring(start, length);
        }
        return null;
    }
    
    /**
     *
     * @param c
     * @return
     */
    public static boolean isWhiteSpace(final char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }
    
    public static String toViewId(FacesContext context, String path) {
        String base = context.getExternalContext().getRealPath("/").replace("\\", "/");
        String result = path.replaceFirst(base, "");

        if (result.startsWith("/resources")) {
            result = result.substring(10);
            return result;
        }

        int sep = result.lastIndexOf("/META-INF/resources");
        if (sep > -1) {
            result = result.substring(sep + 19);
            return result;
        }

        return result;
    }
    
    private static String localPath(FacesContext context, String path) {
        String base = context.getExternalContext().getRealPath("/").replace("\\","/");
        String result = path.replaceFirst(base, "");
        
//        if(result.startsWith("/resources")) {
//            result = result.substring(10);
//            return result;
//        }
//        
//        int sep = result.lastIndexOf("/META-INF/resources");
//        if(sep > -1) {
//            result = result.substring(sep+19);
//            return result;
//        }
//        
        return result;
    }
    
    public static URL getCompositeURL(FaceletContext ctx) {

        URL url = null;

        try {
            Field ffield = ctx.getClass().getDeclaredField("facelet");
            boolean faccessible = ffield.isAccessible();
            try {
                ffield.setAccessible(true);
                Facelet facelet = (Facelet) ffield.get(ctx);
                Field sfield = facelet.getClass().getDeclaredField("src");
                boolean saccessible = sfield.isAccessible();
                try {
                    sfield.setAccessible(true);
                    url = (URL) sfield.get(facelet);
                } finally {
                    sfield.setAccessible(saccessible);
                }

            } finally {
                ffield.setAccessible(faccessible);
            }

        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
        }

        return url;
    }
    
    public static SCXML findStateMachine(FacesContext fc, String continerName, String scxmlId, Object continerSource) {
        StateFlowHandler handler = StateFlowHandler.getInstance();

        if (continerSource instanceof URL) {
            URL url = (URL) continerSource;

            if (continerName == null) {
                throw new IllegalStateException(String.format(
                        "Can not find scxml definition \"%s\", "
                        + "view location not found in composite component.",
                        scxmlId));
            }

            try {
                SCXML scxml = handler.getStateMachine(fc, url, continerName, scxmlId);
                if (scxml == null) {
                    throw new IllegalStateException(String.format(
                            "Can not find scxml definition id=\"%s\", not found"
                            + " in composite <f:metadata...",
                            scxmlId));
                }

                return scxml;
            } catch (ModelException ex) {
                throw new IllegalStateException(String.format(
                        "can not find scxml definition \"%s\", throw model exception.",
                        scxmlId), ex);
            }
        } else {
            try {
                SCXML scxml = handler.findStateMachine(fc, scxmlId);
                if (scxml == null) {
                    throw new IllegalStateException(String.format(
                            "can not find scxml definition id=\"%s\", not found"
                            + " in composite <f:metadata...",
                            scxmlId));
                }
                return scxml;
            } catch (ModelException ex) {
                throw new IllegalStateException(String.format(
                        "can not find scxml definition \"%s\", throw model exception.",
                        scxmlId), ex);
            }
        }
    }
    
    public static Map<String, List<String>> getUIParams(UIComponent component) {
        Map<String, List<String>> params = null;

        for (int i = 0; i < component.getChildCount(); i++) {
            UIComponent child = component.getChildren().get(i);
            if (child.isRendered() && (child instanceof UIParameter)) {
                UIParameter uiParam = (UIParameter) child;

                if (!uiParam.isDisable()) {
                    if (params == null) {
                        params = new LinkedHashMap<>();
                    }

                    List<String> paramValues = params.get(uiParam.getName());
                    if (paramValues == null) {
                        paramValues = new ArrayList<>();
                        params.put(uiParam.getName(), paramValues);
                    }

                    paramValues.add(String.valueOf(uiParam.getValue()));
                }
            }
        }

        return params;
    }
    
    
}
