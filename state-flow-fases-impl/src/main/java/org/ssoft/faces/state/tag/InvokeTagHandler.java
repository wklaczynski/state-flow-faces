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
        decorate(ctx, parent, target);

        target.setTargettype(type.getValue());
        target.setSrc(src.getValueExpression(ctx, String.class));
        
        target.setId(id != null ? id.getValue() : null);

        applyNext(ctx, parent, target);

        State state = (State) parentElement;
        state.setInvoke(target);
    }

}
