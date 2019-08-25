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

import java.util.Map;

/**
 * The event controller interface used to send messages containing
 * events or other information directly to another SCXML Interpreter,
 * other external systems using an Event I/O Processor or to raise
 * events in the current SCXML session.
 *
 */
public interface EventDispatcher {

    /**
     * A EventDispatcher keeps track of outstanding (pending) events to be send on behalf of the statemachine
     * it is 'attached' to.
     * To support easy setup and configuration of an invoked child statemachine (see {@link Invoker})
     * the EventDispatcher provides this newInstnace method to allow creating a new instance without sharing its
     * internal state..
     * @return a new EventDispatcher instance for usage in an invoked child statemachine.
     */
    EventDispatcher newInstance();

    /**
     * Cancel the specified send message.
     *
     * @param sendId The ID of the send message to cancel
     */
    void cancel(String sendId);

    /**
     * Send this message to the target.
     *
     * @param sctx send context fot the currend send
     */
    void send(SendContext sctx);

}

