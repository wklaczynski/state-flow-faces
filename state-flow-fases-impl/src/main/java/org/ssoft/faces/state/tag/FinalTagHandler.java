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
import javax.faces.state.model.Final;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FinalTagHandler extends AbstractFlowTagHandler<Final> {

    protected final TagAttribute id;

    public FinalTagHandler(TagConfig config) {
        super(config, Final.class);

        in("scxml", StateChart.class);
        in("state", State.class);

        this.id = this.getRequiredAttribute("id");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {

        Final state = new Final();
        decorate(ctx, parent, state);

        state.setId(id.getValue(ctx));

        applyNext(ctx, parent, state);

        addChild(ctx, parent, state);
        addTransitionTarget(ctx, parent, state);
    }

}
