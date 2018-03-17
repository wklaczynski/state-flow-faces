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
        decorate(ctx, parent, action);

        action.setValueExpression("cond" ,cond.getValueExpression(ctx, Boolean.class));

        applyNext(ctx, parent, action);

        If aif = (If) parentElement;
        aif.addAction(aif);
    }

}
