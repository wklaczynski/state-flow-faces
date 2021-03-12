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
package org.ssoft.faces.impl.state.evaluator;

import jakarta.el.FunctionMapper;
import java.lang.reflect.Method;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class EvalueatorFunctionMapper extends FunctionMapper {

    private FunctionMapper[] mappers;
    private int size;
    private StateFlowEvaluator evaluator;

    /**
     *
     * @param context
     * @param evaluator
     */
    public EvalueatorFunctionMapper(StateFlowEvaluator evaluator) {
        this.size = 0;
        this.mappers = new FunctionMapper[16];
        this.evaluator = evaluator;
    }

    /**
     *
     * @param mappers
     */
    public EvalueatorFunctionMapper(FunctionMapper... mappers) {
        this.size = 0;
        this.mappers = new FunctionMapper[16];
        for (FunctionMapper mapper : mappers) {
            add(mapper);
        }
    }

    /**
     *
     * @param mapper
     */
    public void add(FunctionMapper mapper) {

        if (mapper == null) {
            throw new NullPointerException();
        }

        if (size >= mappers.length) {
            FunctionMapper[] newMappers = new FunctionMapper[size * 2];
            System.arraycopy(mappers, 0, newMappers, 0, size);
            mappers = newMappers;
        }

        mappers[size++] = mapper;
    }

    @Override
    public Method resolveFunction(String prefix, String name) {
        for (int i = 0; i < size; i++) {
            Method method = mappers[i].resolveFunction(prefix, name);
            if (method != null) {
                return method;
            }
        }
        if (evaluator.getELContext() != null && evaluator.getELContext().getFunctionMapper() != null) {
            Method method = evaluator.getELContext().getFunctionMapper().resolveFunction(prefix, name);
            if (method != null) {
                return method;
            }
        }

        return null;
    }

    public void reset() {
        size = 0;
    }

}
