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
package javax.scxml;

public class EventBuilder {

    private final String name;
    private final int type;
    private String sendId;
    private String origin;
    private String originType;
    private String invokeId;
    private Object data;

    public EventBuilder(final String name, final int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public String getSendId() {
        return sendId;
    }

    public EventBuilder sendId(final String sendId) {
        this.sendId = sendId;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public EventBuilder origin(final String origin) {
        this.origin = origin;
        return this;
    }

    public String getOriginType() {
        return originType;
    }

    public EventBuilder originType(final String originType) {
        this.originType = originType;
        return this;
    }

    public String getInvokeId() {
        return invokeId;
    }

    public EventBuilder invokeId(final String invokeId) {
        this.invokeId = invokeId;
        return this;
    }

    public Object getData() {
        return data;
    }

    public EventBuilder data(final Object data) {
        this.data = data;
        return this;
    }

    public TriggerEvent build() {
        return new TriggerEvent(name, type, sendId, origin, originType, invokeId, data);
    }
}
