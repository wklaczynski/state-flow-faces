/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.scxml.model;

/**
 * Interface for elements of the SCXML object model whose progress
 * can be observed using the {@link org.apache.commons.scxml2.NotificationRegistry}. These include
 * individual {@link TransitionTarget}s, {@link Transition}s or the entire state
 * machine {@link SCXML}.
 *
 * <p>Note: it is assumed there will be no more than Integer.MAX_VALUE of such elements in a single SCXML document</p>
 */
public interface Observable {

    /**
     * @return Returns the id for this Observable which is unique within the SCXML state machine
     */
    Integer getObservableId();
}

