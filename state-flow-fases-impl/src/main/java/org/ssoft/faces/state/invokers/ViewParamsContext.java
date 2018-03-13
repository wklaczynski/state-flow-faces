/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.invokers;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ViewParamsContext extends AbstractMap<String, Object> implements Map<String, Object> {

    Map<String, Object> map = new HashMap<>();
    
    public ViewParamsContext() {
        super();
    }

    @Override
    public Object put(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return map.entrySet();
    }
    
}
