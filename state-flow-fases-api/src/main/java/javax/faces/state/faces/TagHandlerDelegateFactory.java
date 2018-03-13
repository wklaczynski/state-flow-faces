/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.faces;

import javax.faces.FacesWrapper;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.TagHandlerDelegate;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class TagHandlerDelegateFactory implements FacesWrapper<TagHandlerDelegateFactory> {

    public TagHandlerDelegateFactory() {
    }

    @Override
    public TagHandlerDelegateFactory getWrapped() {
        return null;
    }
    
    public abstract TagHandlerDelegate createStateFlowActionDelegate(ComponentHandler owner);


}
