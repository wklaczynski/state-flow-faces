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
 * The class in this SCXML object model that corresponds to the
 * &lt;finalize&gt; SCXML element.
 *
 */
public class Finalize extends Executable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Default no-args constructor.
     */
    public Finalize() {
        super();
    }

    /**
     * @return Returns the TransitionalState parent
     */
    @Override
    public TransitionalState getParent() {
        return (TransitionalState)super.getParent();
    }

    /**
     * Set the TransitionalState parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final TransitionalState parent) {
        super.setParent(parent);
    }    
}

