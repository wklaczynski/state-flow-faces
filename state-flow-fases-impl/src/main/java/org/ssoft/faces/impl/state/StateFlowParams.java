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
package org.ssoft.faces.impl.state;

import javax.faces.context.FacesContext;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_AJAX_REDIRECT_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_DEFAULT_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_REQUEST_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_VIEW_REDIRECT_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_USE_FLASH_REDIRECT_PARAM_NAME;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowParams {

    /**
     *
     * @return
     */
    public static boolean isDefaultViewRedirect() {
        FacesContext context = FacesContext.getCurrentInstance();
        Boolean redirect = (Boolean) context.getExternalContext()
                .getApplicationMap().get(STATE_CHART_VIEW_REDIRECT_PARAM_NAME);
        if (redirect == null) {
            StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
            String pname = wcfg.getOptionValue(STATE_CHART_VIEW_REDIRECT_PARAM_NAME, "false");
            redirect = Boolean.parseBoolean(pname);
            context.getExternalContext()
                    .getApplicationMap().put(STATE_CHART_VIEW_REDIRECT_PARAM_NAME, redirect);
        }
        return redirect;
    }

    /**
     *
     * @return
     */
    public static boolean isDefaultAjaxRedirect() {
        FacesContext context = FacesContext.getCurrentInstance();
        Boolean redirect = (Boolean) context.getExternalContext()
                .getApplicationMap().get(STATE_CHART_AJAX_REDIRECT_PARAM_NAME);
        if (redirect == null) {
            StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
            String pname = wcfg.getOptionValue(STATE_CHART_AJAX_REDIRECT_PARAM_NAME, "true");
            redirect = Boolean.parseBoolean(pname);
            context.getExternalContext()
                    .getApplicationMap().put(STATE_CHART_AJAX_REDIRECT_PARAM_NAME, redirect);
        }
        return redirect;
    }

    
    /**
     *
     * @return
     */
    public static boolean isDefaultUseFlashInRedirect() {
        FacesContext context = FacesContext.getCurrentInstance();
        Boolean redirect = (Boolean) context.getExternalContext()
                .getApplicationMap().get(STATE_USE_FLASH_REDIRECT_PARAM_NAME);
        if (redirect == null) {
            StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
            String pname = wcfg.getOptionValue(STATE_USE_FLASH_REDIRECT_PARAM_NAME, "false");
            redirect = Boolean.parseBoolean(pname);
            context.getExternalContext()
                    .getApplicationMap().put(STATE_USE_FLASH_REDIRECT_PARAM_NAME, redirect);
        }
        return redirect;
    }

    /**
     *
     * @return
     */
    public static String getRequestParamatrChartId() {
        FacesContext context = FacesContext.getCurrentInstance();
        String result = (String) context.getExternalContext()
                .getApplicationMap().get(STATE_CHART_REQUEST_PARAM_NAME);
        if (result == null) {
            StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
            result = wcfg.getOptionValue(STATE_CHART_REQUEST_PARAM_NAME, STATE_CHART_DEFAULT_PARAM_NAME);
            context.getExternalContext()
                    .getApplicationMap().put(STATE_CHART_REQUEST_PARAM_NAME, result);
        }
        return result;
    }

}
