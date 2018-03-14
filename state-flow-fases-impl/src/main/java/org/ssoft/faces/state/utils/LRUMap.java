/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.utils;

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
