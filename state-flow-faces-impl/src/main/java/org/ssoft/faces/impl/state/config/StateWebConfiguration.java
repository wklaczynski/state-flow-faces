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
package org.ssoft.faces.impl.state.config;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import org.ssoft.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateWebConfiguration {

    private static final String WEB_CONFIG_KEY = "com.sun.faces.flow.config.WebConfiguration";

    private final Map<String, String[]> cachedListParams;
    private final ServletContext servletContext;

    private StateWebConfiguration(ServletContext servletContext) {

        this.servletContext = servletContext;
        
        String contextName = getServletContextName();

        // build the cache of list type params
        cachedListParams = new HashMap<>(3);
    }

    /**
     *
     * @return
     */
    public static StateWebConfiguration getInstance() {
        return getInstance(FacesContext.getCurrentInstance().getExternalContext());
    }

    /**
     *
     * @param extContext
     * @return
     */
    public static StateWebConfiguration getInstance(ExternalContext extContext) {

        StateWebConfiguration config = (StateWebConfiguration) extContext.getApplicationMap().get(WEB_CONFIG_KEY);
        if (config == null) {
            return getInstance((ServletContext) extContext.getContext());
        } else {
            return config;
        }
    }

    /**
     *
     * @param servletContext
     * @return
     */
    public static StateWebConfiguration getInstance(ServletContext servletContext) {

        StateWebConfiguration webConfig = (StateWebConfiguration) servletContext.getAttribute(WEB_CONFIG_KEY);

        if (webConfig == null) {
            webConfig = new StateWebConfiguration(servletContext);
            servletContext.setAttribute(WEB_CONFIG_KEY, webConfig);
        }

        return webConfig;
    }

    /**
     *
     * @return
     */
    public String getServletContextName() {

        if (servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() <= 4) {
            return servletContext.getServletContextName();
        }

        return servletContext.getContextPath();
    }

    /**
     *
     * @param param
     * @return
     */
    public String getOptionValue(String param) {
        return getOptionValue(param, null);
    }
    
    /**
     *
     * @param param
     * @param def
     * @return
     */
    public String getOptionValue(String param, String def) {
        String[] values = getOptionValues(param, def, " ");
        if(values.length == 0) {
            return def;
        } else {
            return values[0];
        }
    }

    /**
     *
     * @param param
     * @param sep
     * @return
     */
    public String[] getOptionValues(String param, String sep) {
        return getOptionValues(param, null, sep);
    }
    
    /**
     *
     * @param param
     * @param def
     * @param sep
     * @return
     */
    public String[] getOptionValues(String param, String def, String sep) {
        String[] result;

        assert (cachedListParams != null);

        if ((result = cachedListParams.get(param)) == null) {
            String value = FacesContext.getCurrentInstance().getExternalContext().getInitParameter(param);
            if (value == null) {
                value = def;
            }
            
            if (value == null) {
                result = new String[0];
            } else {
                Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
                if (sep == null) {
                    result = new String[]{value};
                } else {
                    result = Util.split(appMap, value, sep);
                }
            }
            cachedListParams.put(param, result);
        }

        return result;
    }

    
    
}
