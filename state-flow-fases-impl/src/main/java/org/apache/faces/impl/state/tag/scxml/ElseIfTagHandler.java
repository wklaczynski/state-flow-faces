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
package org.apache.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import org.apache.scxml.model.ElseIf;
import org.apache.scxml.model.If;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.scxml.model.SCXML;

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
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        ElseIf action = new ElseIf();
        decorate(ctx, parent, action);

        action.setCond(cond.getValue());

        applyNext(ctx, parent, action);

        If aif = (If) parentElement;
        aif.addAction(aif);
    }

}