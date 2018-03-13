/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Initial;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class InitialTagHandler extends AbstractFlowTagHandler<Initial> {

    public InitialTagHandler(TagConfig config) {
        super(config, Initial.class);
        
        in("state", State.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        State state = (State) parentElement;
        if(state.getInitial()!= null) {
            throw new TagException(this.tag, "already defined in this element!");
        }

        Initial target = new Initial();

        applyNext(ctx, parent, target);
        
        state.setInitial(target);
    }

}
