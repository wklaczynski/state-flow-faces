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
package org.apache.common.faces.impl.state.el;

import java.util.HashMap;
import java.util.Map;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class VariableMapperWrapper extends VariableMapper {

    private final VariableMapper target;

    private Map vars;

    /**
     *
     * @param orig
     */
    public VariableMapperWrapper(VariableMapper orig) {
        super();
        this.target = orig;
    }

    @Override
    public ValueExpression resolveVariable(String variable) {
        ValueExpression ve = null;
        try {
            if (this.vars != null) {
                ve = (ValueExpression) this.vars.get(variable);
            }
            if (ve == null) {
                return this.target.resolveVariable(variable);
            }
            return ve;
        } catch (StackOverflowError e) {
            throw new ELException("Could not Resolve Variable [Overflow]: " + variable, e);
        }
    }

    @Override
    public ValueExpression setVariable(String variable,
            ValueExpression expression) {
        if (this.vars == null) {
            this.vars = new HashMap();
        }
        return (ValueExpression) this.vars.put(variable, expression);
    }
}
