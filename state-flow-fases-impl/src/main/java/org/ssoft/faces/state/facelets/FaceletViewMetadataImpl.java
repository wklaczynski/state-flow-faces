/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.facelets;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FaceletViewMetadataImpl extends ScxmlViewMetadataImpl {

    private final ViewMetadata wrapped;

    public FaceletViewMetadataImpl(ViewDeclarationLanguage vdl, ViewMetadata wrapped) {
        super(vdl, wrapped.getViewId());
        this.wrapped = wrapped;
    }

    @Override
    protected  UIViewRoot createView(FacesContext context) {
       return wrapped.createMetadataView(context);
    }

}
