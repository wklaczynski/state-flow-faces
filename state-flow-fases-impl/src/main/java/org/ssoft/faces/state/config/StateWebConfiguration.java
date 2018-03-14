/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.config;

import java.util.HashMap;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import org.ssoft.faces.state.utils.Util;

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

    public static StateWebConfiguration getInstance() {
        return getInstance(FacesContext.getCurrentInstance().getExternalContext());
    }

    public static StateWebConfiguration getInstance(ExternalContext extContext) {

        StateWebConfiguration config = (StateWebConfiguration) extContext.getApplicationMap().get(WEB_CONFIG_KEY);
        if (config == null) {
            return getInstance((ServletContext) extContext.getContext());
        } else {
            return config;
        }
    }

    public static StateWebConfiguration getInstance(ServletContext servletContext) {

        StateWebConfiguration webConfig = (StateWebConfiguration) servletContext.getAttribute(WEB_CONFIG_KEY);

        if (webConfig == null) {
            webConfig = new StateWebConfiguration(servletContext);
            servletContext.setAttribute(WEB_CONFIG_KEY, webConfig);
        }

        return webConfig;
    }

    public String getServletContextName() {

        if (servletContext.getMajorVersion() == 2 && servletContext.getMinorVersion() <= 4) {
            return servletContext.getServletContextName();
        }

        return servletContext.getContextPath();
    }

    public String getOptionValue(String param) {
        return getOptionValue(param, null);
    }
    
    
    public String getOptionValue(String param, String def) {
        String[] values = getOptionValues(param, def, " ");
        if(values.length == 0) {
            return def;
        } else {
            return values[0];
        }
    }

    public String[] getOptionValues(String param, String sep) {
        return getOptionValues(param, null, sep);
    }
    
    
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
