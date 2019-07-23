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
package org.ssoft.faces.impl.state.tag;

import javax.faces.view.facelets.TagHandlerDelegate;
import javax.faces.state.tag.ActionHandler;
import javax.faces.state.tag.TagHandlerDelegateFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TagHandlerDelegateFactoryImpl extends TagHandlerDelegateFactory {

    @Override
    public TagHandlerDelegate createStateFlowActionDelegate(ActionHandler owner) {
        TagHandlerDelegate delegate = new CustomActionHandlerDelegateImpl(owner);
        return delegate;
    }
    
}
