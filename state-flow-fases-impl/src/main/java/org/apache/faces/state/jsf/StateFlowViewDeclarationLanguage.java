/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.jsf;

import java.io.IOException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguage extends ViewDeclarationLanguageWrapper {

    public final ViewDeclarationLanguage wrapped;
    private String sufix;
    boolean lock;
    
    public StateFlowViewDeclarationLanguage(ViewDeclarationLanguage wrapped) {
        super();
        this.wrapped = wrapped;
    }

    @Override
    public ViewDeclarationLanguage getWrapped() {
        return wrapped;
    }
    
    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        return wrapped.getViewMetadata(context, viewId);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        return wrapped.createView(context, viewId);
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return wrapped.restoreView(context, viewId);
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot root) throws IOException {
        wrapped.buildView(context, root);

    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException {
        wrapped.renderView(context, view);
    }

}
