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
package org.ssoft.faces.impl.state.tag.faces;

import jakarta.faces.view.facelets.MetaRule;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagConfig;
import javax.faces.state.tag.ActionHandler;
import org.ssoft.faces.impl.state.tag.MethodPropertyTagRule;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class MethodCallHandler extends ActionHandler {

    private static final MetaRule EXPR_FUNCTION
            = new MethodPropertyTagRule("expr", Void.class);

    
    public MethodCallHandler(TagConfig config) {
        super(config);
    }
    
    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        MetaRuleset metaRuleset = super.createMetaRuleset(type);
        
        metaRuleset.addRule(EXPR_FUNCTION);
        
        return metaRuleset;
    }
    
    
    
}
