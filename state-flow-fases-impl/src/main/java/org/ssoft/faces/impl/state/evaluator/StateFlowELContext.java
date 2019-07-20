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

import java.io.Serializable;
import java.util.HashMap;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.VariableMapper;
import javax.faces.context.FacesContext;
import org.ssoft.faces.impl.state.el.CompositeFunctionMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowELContext extends ELContext implements Serializable {

    private final HashMap<Class<?>, Object> map = new HashMap<>();
    private final ELContext ctx;
    private VariableMapper varMapper;
    private FunctionMapper fnMapper;
    private final FunctionMapper defFnMapper;
    private final CompositeELResolver elResolver;

    public StateFlowELContext(FacesContext facesContext) {
        super();
        this.ctx = facesContext.getELContext();

        this.varMapper = ctx.getVariableMapper();
        if (varMapper == null) {
            this.varMapper = new EvaluatorVariableMapper();
        }

        this.defFnMapper = this.fnMapper = ctx.getFunctionMapper();

        this.elResolver = new CompositeELResolver();
        this.elResolver.add(new EvaluatorELResolver());
        this.elResolver.add(ctx.getELResolver());

    }

    @Override
    public FunctionMapper getFunctionMapper() {
        return this.fnMapper;
    }

    public void addFunctionMaper(FunctionMapper functionMapper) {
        this.fnMapper = new CompositeFunctionMapper(functionMapper, this.fnMapper);
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
        this.fnMapper = defFnMapper;
        this.map.clear();
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
