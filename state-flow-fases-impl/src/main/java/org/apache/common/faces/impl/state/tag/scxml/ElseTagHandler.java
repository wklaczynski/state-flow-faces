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
package org.apache.common.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import org.apache.common.scxml.model.Else;
import org.apache.common.scxml.model.If;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.common.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ElseTagHandler extends AbstractFlowTagHandler<Else> {

    public ElseTagHandler(TagConfig config) {
        super(config, Else.class);

        in("if", If.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Else action = new Else();
        decorate(ctx, parent, action);

        If aif = (If) parentElement;
        aif.addAction(action);

        applyNext(ctx, parent, action);
    }

}
