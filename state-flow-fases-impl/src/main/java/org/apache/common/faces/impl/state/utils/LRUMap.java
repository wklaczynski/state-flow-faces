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
package org.apache.common.faces.impl.state.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 * @param <K>
 * @param <V>
 */
public class LRUMap<K,V> extends LinkedHashMap<K,V> {

    private static final long serialVersionUID = -7137951139094651602L;
    private final int maxCapacity;

    // ------------------------------------------------------------ Constructors

    public LRUMap(int maxCapacity) {
        super(maxCapacity, 1.0f, true);
        this.maxCapacity = maxCapacity;        
    }

    // ---------------------------------------------- Methods from LinkedHashMap

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return (size() > maxCapacity);   
    }
    
    // TEST: com.sun.faces.TestLRUMap_local
}
