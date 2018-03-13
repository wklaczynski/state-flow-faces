/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.el;

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
