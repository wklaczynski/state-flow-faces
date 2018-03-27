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
package org.apache.common.faces.state.events;

import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.model.Transition;
import org.apache.common.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnTransitionEvent {

    private final SCXMLExecutor executor;
    private final TransitionTarget from;
    private final TransitionTarget to;
    private final Transition transition;
    private final String event;

    public OnTransitionEvent(SCXMLExecutor executor, TransitionTarget from, TransitionTarget to, Transition transition, String event) {
        this.executor = executor;
        this.from = from;
        this.to = to;
        this.transition = transition;
        this.event = event;
    }

    public SCXMLExecutor getExecutor() {
        return executor;
    }

    public TransitionTarget getFrom() {
        return from;
    }

    public TransitionTarget getTo() {
        return to;
    }

    public Transition getTransition() {
        return transition;
    }

    public String getEvent() {
        return event;
    }

    
}
