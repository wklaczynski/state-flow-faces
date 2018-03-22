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
package org.apache.faces.impl.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import org.apache.scxml.model.Invoke;
import org.apache.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.scxml.model.SCXML;
import org.apache.scxml.model.TransitionalState;

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
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Invoke target = new Invoke();
        decorate(ctx, parent, target);

        
        target.setType(type.getValue());
        target.setSrc(src.getValue());
        
        target.setId(id != null ? id.getValue() : null);

        applyNext(ctx, parent, target);

        TransitionalState state = (TransitionalState) parentElement;
        state.addInvoke(target);
    }

}
