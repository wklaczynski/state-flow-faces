/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Finalize;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FinalizeTagHandler extends AbstractFlowTagHandler<Finalize> {

    public FinalizeTagHandler(TagConfig config) {
        super(config, Finalize.class);

        in("invoke", Invoke.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Invoke invoke = (Invoke) parentElement;
        if(invoke.getFinalize() != null) {
            throw new TagException(this.tag, "already defined in this element!");
        }
        
        Finalize executable = new Finalize();

        applyNext(ctx, parent, executable);

        invoke.setFinalize(executable);
    }

}
