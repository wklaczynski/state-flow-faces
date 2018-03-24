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
package org.apache.faces.demo.basic;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Observes;
import javax.faces.context.FacesContext;
import org.apache.faces.state.events.OnEntryEvent;
import org.apache.faces.state.events.OnExitEvent;
import org.apache.faces.state.events.OnFinishEvent;
import org.apache.faces.state.events.OnTransitionEvent;

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
            String redirectPath = "/index.xhtml";
            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().redirect(redirectPath);
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

}
