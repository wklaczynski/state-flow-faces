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
package org.ssoft.faces.impl.state;

import java.util.Map;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.env.AbstractContext;

/**
 * Simple Context wrapping a map of variables.
 *
 */
public final class StateFlowContext extends AbstractContext {

    /**
     * Implementation independent log category.
     */

    /**
     * Constructor.
     *
     */
    public StateFlowContext() {
        super(null, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public StateFlowContext(final Context parent) {
        super(parent, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    public StateFlowContext(Context parent, Map<String, Object> initialVars) {
        super(parent, initialVars);
    }
    
}
