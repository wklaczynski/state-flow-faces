/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.ElseIf;
import javax.faces.state.model.If;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ElseIfTagHandler extends AbstractFlowTagHandler<ElseIf> {

    protected final TagAttribute cond;

    public ElseIfTagHandler(TagConfig config) {
        super(config, ElseIf.class);

        in("if", If.class);

        this.cond = this.getRequiredAttribute("cond");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        ElseIf action = new ElseIf();
        action.setCond(cond.getValue());

        applyNext(ctx, parent, action);

        If aif = (If) parentElement;
        aif.addAction(aif);
    }

}
