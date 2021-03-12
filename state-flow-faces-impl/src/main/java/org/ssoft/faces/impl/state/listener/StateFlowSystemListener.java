/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package org.ssoft.faces.impl.state.listener;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.NamedEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowSystemListener implements SystemEventListener {

    @Override
    public void processEvent(SystemEvent cse) throws AbortProcessingException {
        FacesContext facesContext = cse.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        SCXMLExecutor executor = handler.getRootExecutor(facesContext);
        if (executor != null) {
            Class<? extends SystemEvent> clazz = cse.getClass();
            NamedEvent namedEvent = (NamedEvent) clazz.getAnnotation(NamedEvent.class);
            if (namedEvent != null) {
                EventBuilder eb = new EventBuilder("faces.event." + namedEvent.shortName(), TriggerEvent.CALL_EVENT);
                UIViewRoot viewRoot = facesContext.getViewRoot();
                String sendId = viewRoot.getViewId();

                eb.sendId(sendId);
                eb.data(cse.getSource());

                try {
                    TriggerEvent ev = eb.build();
                    executor.triggerEvent(ev);
                } catch (ModelException ex) {
                    throw new FacesException(ex);
                }

                if (facesContext.getResponseComplete()) {
                    handler.writeState(facesContext);
                }
            }
        }
    }

    @Override
    public boolean isListenerForSource(Object o) {
        return true;
    }
}
