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

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import javax.faces.state.events.OnEntryEvent;
import javax.faces.state.events.OnExitEvent;
import javax.faces.state.events.OnTransitionEvent;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLListener;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.Parallel;
import javax.faces.state.scxml.model.State;
import javax.faces.state.scxml.model.Transition;
import javax.faces.state.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIListener implements SCXMLListener, Serializable {

    private final SCXMLExecutor executor;

    /**
     *
     * @param executor
     */
    public StateFlowCDIListener(SCXMLExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void onEntry(EnterableState tt) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (CdiUtil.isCdiAvailable(fc)) {
            StateFlowCDIHelper.stateEntered(executor, tt);
            
            BeanManager bm = CdiUtil.getCdiBeanManager(fc);
            bm.fireEvent(new OnEntryEvent(executor, tt));
            
            if(tt instanceof State) {
                State state = (State) tt;

            }
            if(tt instanceof Parallel) {
                Parallel parallel = (Parallel) tt;
            }
        }
    }

    @Override
    public void onTransition(TransitionTarget from, TransitionTarget to, Transition t, String event) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (CdiUtil.isCdiAvailable(fc)) {
            BeanManager bm = CdiUtil.getCdiBeanManager(fc);
            bm.fireEvent(new OnTransitionEvent(executor, from, to, t, event));
        }
    }

    @Override
    public void onExit(EnterableState tt) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (CdiUtil.isCdiAvailable(fc)) {
            StateFlowCDIHelper.stateExited(executor, tt);
            
            BeanManager bm = CdiUtil.getCdiBeanManager(fc);
            bm.fireEvent(new OnExitEvent(executor, tt));
            
            if(tt instanceof State) {
                State state = (State) tt;

            }
            if(tt instanceof Parallel) {
                Parallel parallel = (Parallel) tt;
            }
        }
    }

}
