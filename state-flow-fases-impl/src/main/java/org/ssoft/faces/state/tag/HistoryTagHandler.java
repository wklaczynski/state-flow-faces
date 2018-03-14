/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.History;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class HistoryTagHandler extends AbstractFlowTagHandler<History> {

    protected final TagAttribute id;
    protected final TagAttribute type;
    
    public HistoryTagHandler(TagConfig config) {
        super(config, History.class);
        
        in("state", State.class);
       
        this.id = this.getRequiredAttribute("id");
        this.type = this.getAttribute("type");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        History target = new History();
        decorate(ctx, parent, target);

        target.setId(id.getValue());
        target.setType(type != null ? type.getValue() : null);

        applyNext(ctx, parent, target);
        
        addChild(ctx, parent, target);
    }

}
