/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParallelTagHandler extends AbstractFlowTagHandler<Parallel> {

    protected final TagAttribute id;
    
    public ParallelTagHandler(TagConfig config) {
        super(config, Parallel.class);
        
        in("scxml", StateChart.class);
        
        this.id = this.getAttribute("id");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Parallel parallel = new Parallel();
        decorate(ctx, parent, parallel);
        
        String cid = null;
        if(id != null) {
            cid = id.getValue();
        } else {
            cid = generateUniqueId(ctx, parent, parallel, "parallel_");
        }

        applyNext(ctx, parent, parallel);
        
        addChild(ctx, parent, parallel);
    }

}
