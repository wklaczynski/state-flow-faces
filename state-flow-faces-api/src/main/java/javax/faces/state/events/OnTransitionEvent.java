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
package javax.faces.state.events;

import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.model.Transition;
import javax.faces.state.scxml.model.TransitionTarget;

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

    /**
     *
     * @param executor
     * @param from
     * @param to
     * @param transition
     * @param event
     */
    public OnTransitionEvent(SCXMLExecutor executor, TransitionTarget from, TransitionTarget to, Transition transition, String event) {
        this.executor = executor;
        this.from = from;
        this.to = to;
        this.transition = transition;
        this.event = event;
    }

    /**
     *
     * @return
     */
    public SCXMLExecutor getExecutor() {
        return executor;
    }

    /**
     *
     * @return
     */
    public TransitionTarget getFrom() {
        return from;
    }

    /**
     *
     * @return
     */
    public TransitionTarget getTo() {
        return to;
    }

    /**
     *
     * @return
     */
    public Transition getTransition() {
        return transition;
    }

    /**
     *
     * @return
     */
    public String getEvent() {
        return event;
    }

    
}
