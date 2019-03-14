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

import org.apache.common.faces.state.scxml.ActionExecutionContext;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.Evaluator;
import org.apache.common.faces.state.scxml.SCXMLExpressionException;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;script&gt; SCXML element.
 */
public class Script extends Action {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private boolean globalScript;

    private String script;

    private String src;

    /**
     * Constructor.
     */
    public Script() {
        super();
    }

    /**
     *
     * @return
     */
    public boolean isGlobalScript() {
        return globalScript;
    }

    /**
     *
     * @param globalScript
     */
    public void setGlobalScript(final boolean globalScript) {
        this.globalScript = globalScript;
    }

    /**
     *
     * @return
     */
    public String getSrc() {
        return src;
    }

    /**
     *
     * @param src
     */
    public void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the script to execute.
     *
     * @return The script to execute.
     */
    public String getScript() {
        return script;
    }

    /**
     *
     * @param script
     */
    public void setScript(final String script) {
        this.script = script;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = isGlobalScript() ? exctx.getGlobalContext() : exctx.getContext(getParentEnterableState());
        Evaluator eval = exctx.getEvaluator();
        eval.evalScript(ctx, getScript());
    }
}

