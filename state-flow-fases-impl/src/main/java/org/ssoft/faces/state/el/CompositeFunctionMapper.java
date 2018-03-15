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
package org.ssoft.faces.state.el;

import java.lang.reflect.Method;
import javax.el.FunctionMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CompositeFunctionMapper extends FunctionMapper {

    private FunctionMapper[] mappers;
    private int size;

    public CompositeFunctionMapper() {
        this.size = 0;
        this.mappers = new FunctionMapper[16];
    }

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
        return null;
    }

}
