/*
 * Copyright 2018 Waldemar Kłaczyński.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ssoft.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.scxml.model.Assign;
import javax.faces.state.scxml.model.Finalize;
import javax.faces.state.scxml.model.If;
import javax.faces.state.scxml.model.OnEntry;
import javax.faces.state.scxml.model.OnExit;
import javax.faces.state.scxml.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class AssignTagHandler extends AbstractFlowTagHandler<Assign> {

    /**
     *
     */
    protected final TagAttribute location;

    /**
     *
     */
    protected final TagAttribute expr;

    /**
     *
     * @param config
     */
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

    /**
     *
     * @param ctx
     * @param parent
     * @param chart
     * @param parentElement
     * @throws IOException
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Assign action = new Assign();
        decorate(ctx, parent, action);

        action.setLocation(location.getValueExpression(ctx, Object.class));
        action.setExpr(expr != null ? expr.getValueExpression(ctx, Object.class): null);

        addAction(ctx, parent, action);

        applyNext(ctx, parent, action);
    }

}
