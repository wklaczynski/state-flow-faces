/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Assign;
import javax.faces.state.model.Finalize;
import javax.faces.state.model.If;
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.OnExit;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class AssignTagHandler extends AbstractFlowTagHandler<Assign> {

    protected final TagAttribute location;
    protected final TagAttribute expr;

    public AssignTagHandler(TagConfig config) {
        super(config, Assign.class);

        in("onentry", OnEntry.class);
        in("onexit", OnExit.class);
        in("transition", Transition.class);
        in("finalize", Finalize.class);
        in("if", If.class);

        top("onentry", OnEntry.class);
        top("onexit", OnExit.class);
        top("transition", Transition.class);
        top("finalize", Finalize.class);

        
        this.location = this.getRequiredAttribute("location");
        this.expr = this.getAttribute("expr");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Assign action = new Assign();
        decorate(ctx, parent, action);

        action.setLocation(location.getValue());
        action.setExpr(expr != null ? expr.getValue() : null);

        applyNext(ctx, parent, action);

        addAction(ctx, parent, action);
    }

}
