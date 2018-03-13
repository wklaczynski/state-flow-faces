/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.jsf;

import java.util.logging.Logger;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageFactory;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguageFactory extends ViewDeclarationLanguageFactory {

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();
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
        if (viewId.endsWith(".scxml")) {
            return new ScxmlFileViewHandlingStrategy();
        }
        return new StateFlowViewDeclarationLanguage(getWrapped().getViewDeclarationLanguage(viewId));
    }

}
