/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Final;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FinalTagHandler extends AbstractFlowTagHandler<Final> {

    protected final TagAttribute id;

    public FinalTagHandler(TagConfig config) {
        super(config, Final.class);

        in("scxml", StateChart.class);
        in("state", State.class);

        this.id = this.getRequiredAttribute("id");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {

        Final state = new Final();
        decorate(ctx, parent, state);

        state.setId(id.getValue(ctx));

        applyNext(ctx, parent, state);

        addChild(ctx, parent, state);
        addTransitionTarget(ctx, parent, state);
    }

}
