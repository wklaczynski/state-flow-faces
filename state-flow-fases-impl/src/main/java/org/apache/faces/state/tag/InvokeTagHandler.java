/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class InvokeTagHandler extends AbstractFlowTagHandler<Invoke> {

    protected final TagAttribute type;
    protected final TagAttribute src;
    protected final TagAttribute id;

    public InvokeTagHandler(TagConfig config) {
        super(config, Invoke.class);

        in("state", State.class);

        this.type = this.getRequiredAttribute("type");
        this.src = this.getRequiredAttribute("src");
        this.id = this.getAttribute("id");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Invoke target = new Invoke();
        target.setTargettype(type.getValue());
        target.setSrc(src.getValue());
        
        target.setId(id != null ? id.getValue() : null);

        applyNext(ctx, parent, target);

        State state = (State) parentElement;
        state.setInvoke(target);
    }

}
