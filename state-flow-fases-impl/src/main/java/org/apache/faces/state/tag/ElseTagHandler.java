/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Else;
import javax.faces.state.model.If;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ElseTagHandler extends AbstractFlowTagHandler<Else> {

    public ElseTagHandler(TagConfig config) {
        super(config, Else.class);

        in("if", If.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Else action = new Else();

        applyNext(ctx, parent, action);

        If aif = (If) parentElement;
        aif.addAction(action);
    }

}
