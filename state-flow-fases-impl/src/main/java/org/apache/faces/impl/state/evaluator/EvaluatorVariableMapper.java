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
package org.apache.faces.impl.state.evaluator;

import java.util.HashMap;
import java.util.Map;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class EvaluatorVariableMapper extends VariableMapper {

    private Map vars;

    public EvaluatorVariableMapper() {
        super();
    }

    /**
     * @param name
     * @return 
     * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String name) {
        if (this.vars != null) {
            return (ValueExpression) this.vars.get(name);
        }
        return null;
    }

    /**
     * @param name
     * @param expression
     * @return 
     * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String name, ValueExpression expression) {
        if (this.vars == null) {
            this.vars = new HashMap();
        }
        return (ValueExpression) this.vars.put(name, expression);
    }

}
