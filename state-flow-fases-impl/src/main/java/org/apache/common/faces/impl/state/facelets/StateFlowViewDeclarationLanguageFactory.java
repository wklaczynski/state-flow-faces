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
package org.apache.common.faces.impl.state.facelets;

import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory {

    private final ViewDeclarationLanguageFactory wrapped;

    public StateFlowViewDeclarationLanguageFactory(ViewDeclarationLanguageFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public ViewDeclarationLanguageFactory getWrapped() {
        return wrapped;
    }
    
    @Override
    public ViewDeclarationLanguage getViewDeclarationLanguage(String viewId) {
        return new StateFlowViewDeclarationLanguage(getWrapped().getViewDeclarationLanguage(viewId));
    }

}
