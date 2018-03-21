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
import javax.scxml.model.Cancel;
import javax.scxml.model.Finalize;
import javax.scxml.model.If;
import javax.scxml.model.OnEntry;
import javax.scxml.model.OnExit;
import javax.scxml.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CancelTagHandler extends AbstractFlowTagHandler<Cancel> {

    protected final TagAttribute sendid;

    public CancelTagHandler(TagConfig config) {
        super(config, Cancel.class);
        
        in("onentry", OnEntry.class);
        in("onexit", OnExit.class);
        in("transition", Transition.class);
        in("finalize", Finalize.class);
        in("if", If.class);

        top("onentry", OnEntry.class);
        top("onexit", OnExit.class);
        top("transition", Transition.class);
        top("finalize", Finalize.class);

        this.sendid = this.getRequiredAttribute("sendid");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Cancel action = new Cancel();
        decorate(ctx, parent, action);

        action.setSendid(sendid.getValue());
        
        applyNext(ctx, parent, action);
        
        addAction(ctx, parent, action);
    }

}
