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
package org.apache.common.faces.impl.state.cdi;

import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface StateFlowCDIEventFireHelper {

    /**
     *
     * @param executor
     */
    void fireExecutorInitializedEvent(SCXMLExecutor executor);

    /**
     *
     * @param executor
     */
    void fireExecutorDestroyedEvent(SCXMLExecutor executor);

    /**
     *
     * @param executor
     */
    void fireRootExecutorInitializedEvent(SCXMLExecutor executor);

    /**
     *
     * @param executor
     */
    void fireRootExecutorDestroyedEvent(SCXMLExecutor executor);
    
    /**
     *
     * @param target
     */
    void fireTargetInitializedEvent(TransitionTarget target);

    /**
     *
     * @param target
     */
    void fireTargetDestroyedEvent(TransitionTarget target);

    /**
     *
     * @param executor
     */
    void fireTargetExecutorInitializedEvent(SCXMLExecutor executor);

    /**
     *
     * @param executor
     */
    void fireTargetExecutorDestroyedEvent(SCXMLExecutor executor);
    
}
