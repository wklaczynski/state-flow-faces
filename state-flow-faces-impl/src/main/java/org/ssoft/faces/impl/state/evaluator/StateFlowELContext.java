/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package org.ssoft.faces.impl.state.evaluator;

import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowELContext extends ELContext implements Serializable {

    private final HashMap<Class<?>, Object> map = new HashMap<>();
    private final ELContext ctx;
    private final VariableMapper varMapper;
    private final EvalueatorFunctionMapper fnMapper;
    private final CompositeELResolver elResolver;
    private final StateFlowEvaluator evaluator;

    public StateFlowELContext(FacesContext facesContext, StateFlowEvaluator evaluator) {
        super();
        this.evaluator = evaluator;

        this.ctx = evaluator.getELContext();

        this.varMapper = new EvaluatorVariableMapper(evaluator);
        this.fnMapper = new EvalueatorFunctionMapper(evaluator);
        
        this.elResolver = new CompositeELResolver();
        this.elResolver.add(new EvaluatorELResolver(evaluator, varMapper));
        this.elResolver.add(ctx.getELResolver());

    }

    public StateFlowEvaluator getEvaluator() {
        return evaluator;
    }
    
    @Override
    public FunctionMapper getFunctionMapper() {
        return this.fnMapper;
    }

    public void addFunctionMaper(FunctionMapper functionMapper) {
        this.fnMapper.add(functionMapper);
    }

    @Override
    public VariableMapper getVariableMapper() {
        return this.varMapper;
    }

    @Override
    public ELResolver getELResolver() {
        return elResolver;
    }

    public void reset() {
        this.map.clear();
        fnMapper.reset();
    }

    @Override
    public void putContext(Class key, Object contextObject) {
        if ((key == null)) {
            throw new NullPointerException();
        }
        if (contextObject == null) {
            map.remove(key);
        } else {
            map.put(key, contextObject);
        }
    }

    @Override
    public Object getContext(Class key) {
        if (key == null) {
            throw new NullPointerException();
        }
        Object result = map.get(key);
        if (result == null) {
            result = super.getContext(key);
        }
        return result;
    }

}
