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
package org.apache.common.faces.state.scxml.model;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;content&gt; SCXML element.
 *
 */
public class Content extends SCComponent implements ParsedValueContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The param expression, may be null.
     */
    private String expr;

    /**
     * The content body, may be null.
     */
    private ParsedValue contentBody;

    /**
     * Get the expression for this content.
     *
     * @return String The expression for this content.
     */
    public final String getExpr() {
        return expr;
    }

    /**
     * Set the expression for this content.
     *
     * @param expr The expression for this content.
     */
    public final void setExpr(final String expr) {
        this.expr = expr;
    }

    /**
     * Get the content element body
     *
     * @return The content element body.
     */
    @Override
    public final ParsedValue getParsedValue() {
        return contentBody;
    }

    /**
     * Set the content element body
     *
     * @param contentBody The content element body
     */
    @Override
    public final void setParsedValue(final ParsedValue contentBody) {
        this.contentBody = contentBody;
    }
}
