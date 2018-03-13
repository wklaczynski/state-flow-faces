/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Final;
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.TransitionTarget;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnEntryTagHandler extends AbstractFlowTagHandler<OnEntry> {

    public OnEntryTagHandler(TagConfig config) {
        super(config, OnEntry.class);

        in("parallel", Parallel.class);
        in("state", State.class);
        in("final", Final.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        TransitionTarget target = (TransitionTarget) parentElement;
        if (target.getOnEntry() != null) {
            throw new TagException(this.tag, "already defined in this element!");
        }

        OnEntry executable = new OnEntry();

        applyNext(ctx, parent, executable);

        target.setOnEntry(executable);
    }

}
