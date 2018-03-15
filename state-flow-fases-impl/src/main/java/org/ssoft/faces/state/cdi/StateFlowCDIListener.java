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
package org.ssoft.faces.state.cdi;

import java.io.Serializable;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.state.events.FlowOnEntryEvent;
import javax.faces.state.events.FlowOnExitEvent;
import javax.faces.state.events.FlowOnTransitionEvent;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;
import org.ssoft.faces.state.utils.Util;
import javax.faces.state.StateFlowListener;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIListener implements StateFlowListener, Serializable {

    public StateFlowCDIListener() {
    }

    @Override
    public void onEntry(TransitionTarget tt) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (Util.isCdiAvailable(fc)) {
            BeanManager bm = Util.getCdiBeanManager(fc);
            bm.fireEvent(new FlowOnEntryEvent(tt));
            
            if(tt instanceof State) {
                State state = (State) tt;
                StateScopeCDIContex.flowStateEntered(state);
            }
            if(tt instanceof Parallel) {
                Parallel parallel = (Parallel) tt;
                ParallelScopeCDIContext.flowParallelEntered(parallel);
            }
        }
    }

    @Override
    public void onTransition(TransitionTarget from, TransitionTarget to, Transition t) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (Util.isCdiAvailable(fc)) {
            BeanManager bm = Util.getCdiBeanManager(fc);
            bm.fireEvent(new FlowOnTransitionEvent(from, to, t));
        }
    }

    @Override
    public void onExit(TransitionTarget tt) {
        FacesContext fc = FacesContext.getCurrentInstance();
        if (Util.isCdiAvailable(fc)) {
            BeanManager bm = Util.getCdiBeanManager(fc);
            bm.fireEvent(new FlowOnExitEvent(tt));
            
            if(tt instanceof State) {
                State state = (State) tt;
                StateScopeCDIContex.flowStateExited(state);
            }
            if(tt instanceof Parallel) {
                Parallel parallel = (Parallel) tt;
                ParallelScopeCDIContext.flowParallelExited(parallel);
            }
        }
    }

}
