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

import com.sun.faces.facelets.tag.TagLibrary;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import javax.el.FunctionMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TagsFunctionMapper extends FunctionMapper implements Serializable {

    private final Map namespaces;
    private final TagLibrary tagLibrary;

    public TagsFunctionMapper(Map namespaces, TagLibrary tagLibrary) {
        super();
        this.namespaces = namespaces;
        this.tagLibrary = tagLibrary;
    }

    @Override
    public Method resolveFunction(final String prefix, final String localName) {
        
        if(namespaces.containsKey(prefix)){
            String ns = (String) namespaces.get(prefix);
            if(tagLibrary.containsFunction(ns, localName)){
                Method result = tagLibrary.createFunction(ns, localName);
                return result;
            }
        }
        
        return null;
    }
}
