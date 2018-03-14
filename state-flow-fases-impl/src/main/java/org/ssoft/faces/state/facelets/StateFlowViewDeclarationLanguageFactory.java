/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.facelets;

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
