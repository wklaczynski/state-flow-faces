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
import org.apache.common.scxml.model.EnterableState;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnExitEvent {

    private final SCXMLExecutor executor;
    
    private final EnterableState target;

    /**
     *
     * @param executor
     * @param target
     */
    public OnExitEvent(SCXMLExecutor executor, EnterableState target) {
        this.executor = executor;
        this.target = target;
    }

    /**
     *
     * @return
     */
    public EnterableState getTarget() {
        return target;
    }

    /**
     *
     * @return
     */
    public SCXMLExecutor getExecutor() {
        return executor;
    }
    
}
