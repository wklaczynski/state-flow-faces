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
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateTagHandler extends AbstractFlowTagHandler<State> {

    protected final TagAttribute id;

    public StateTagHandler(TagConfig config) {
        super(config, State.class);

        in("scxml", StateChart.class);
        in("parallel", Parallel.class);
        in("state", State.class);

        this.id = this.getRequiredAttribute("id");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        State state = new State();
        decorate(ctx, parent, state);

        state.setId(id.getValue(ctx));

        applyNext(ctx, parent, state);

        addChild(ctx, parent, state);
        addTransitionTarget(ctx, parent, state);
    }

}
