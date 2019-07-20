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
package javax.faces.state.scxml;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParentSCXMLIOProcessor implements SCXMLIOProcessor {

    private SCXMLExecutor executor;
    private final String invokeId;

    /**
     *
     * @param executor
     * @param invokeId
     */
    public ParentSCXMLIOProcessor(final SCXMLExecutor executor, final String invokeId) {
        this.executor = executor;
        this.invokeId = invokeId;
    }

    @Override
    public synchronized void addEvent(final TriggerEvent event) {
        if (executor != null) {
            executor.addEvent(event);
        }
    }

    /**
     * Get the id.
     *
     * @return String An identifier.
     */
    @Override
    public final String getId() {
        return executor.getId();
    }

    /**
     * Get the id.
     *
     * @return String An identifier.
     */
    @Override
    public final String getRootId() {
        return executor.getRootId();
    }

    /**
     * Get the id.
     *
     * @return String An identifier.
     */
    @Override
    public final String getClientId() {
        return executor.getClientId();
    }

    /**
     *
     * @return
     */
    public String getInvokeId() {
        return invokeId;
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
     */
    public synchronized void close() {
        executor = null;
    }

    /**
     *
     * @return
     */
    public synchronized boolean isClosed() {
        return executor == null;
    }
    
}