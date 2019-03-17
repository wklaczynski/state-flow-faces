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
package org.apache.common.faces.demo.prime;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.state.events.OnEntryEvent;
import javax.faces.state.events.OnExitEvent;
import javax.faces.state.events.OnFinishEvent;
import javax.faces.state.events.OnTransitionEvent;
import org.primefaces.PrimeFaces;

/**
 *
 * @author Waldemar Kłaczyński
 */
@RequestScoped
public class LayoutService {

    static final Logger log = Logger.getLogger(LayoutService.class.getName());

    public void flowFinish(@Observes OnFinishEvent event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc != null && !fc.getResponseComplete()) {
            goToMainPage();
        }
    }

    public void stateChartOnEntryEvent(@Observes OnEntryEvent event) {

    }

    public void stateChartOnExitEvent(@Observes OnExitEvent event) {

    }

    public void stateChartOnTransitionEvent(@Observes OnTransitionEvent event) {

    }

    public void goToMainPage() {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            String actionURL = fc.getApplication().
                    getViewHandler().getBookmarkableURL(fc, "/index.xhtml", new HashMap<>(), false);

            String redirectPath = actionURL;
            ec.redirect(redirectPath);
            fc.renderResponse();
            if (!PrimeFaces.current().isAjaxRequest()) {
                fc.responseComplete();
            }
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

}
