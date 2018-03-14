/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.model.Data;
import javax.faces.state.model.Datamodel;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataTagHandler extends AbstractFlowTagHandler<Data> {

    protected final TagAttribute id;
    protected final TagAttribute src;
    protected final TagAttribute expr;

    public DataTagHandler(TagConfig config) {
        super(config, Data.class);

        in("datamodel", Datamodel.class);

        this.id = this.getRequiredAttribute("id");
        this.src = this.getAttribute("src");
        this.expr = this.getAttribute("expr");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Data data = new Data();
        decorate(ctx, parent, data);

        data.setId(id.getValue());
        data.setSrc(src != null ? src.getValue() : null);
        data.setExpr(expr != null ? expr.getValue() : null);

        applyNext(ctx, parent, data);

        Datamodel datamodel = (Datamodel) parentElement;
        datamodel.addData(data);
    }

}
