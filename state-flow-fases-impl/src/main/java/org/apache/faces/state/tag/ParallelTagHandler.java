/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParallelTagHandler extends AbstractFlowTagHandler<Parallel> {

    public ParallelTagHandler(TagConfig config) {
        super(config, Parallel.class);
        
        in("scxml", StateChart.class);
        in("state", State.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Parallel parallel = new Parallel();

        applyNext(ctx, parent, parallel);
        
        addChild(ctx, parent, parallel);
    }

}
