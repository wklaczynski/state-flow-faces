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
import org.apache.common.scxml.model.Finalize;
import org.apache.common.scxml.model.OnEntry;
import org.apache.common.scxml.model.OnExit;
import org.apache.common.scxml.model.Raise;
import org.apache.common.scxml.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.common.scxml.model.ActionsContainer;
import org.apache.common.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class RaiseTagHandler extends AbstractFlowTagHandler<Raise> {

    protected final TagAttribute event;
    
    public RaiseTagHandler(TagConfig config) {
        super(config, Raise.class);
        
        in("onentry", OnEntry.class);
        in("onexit", OnExit.class);
        in("transition", Transition.class);
        in("finalize", Finalize.class);
        
        impl("actions continer", ActionsContainer.class);

        top("onentry", OnEntry.class);
        top("onexit", OnExit.class);
        top("transition", Transition.class);
        top("finalize", Finalize.class);
        
        this.event = this.getRequiredAttribute("event");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Raise action = new Raise();
        decorate(ctx, parent, action);

        action.setEvent(event.getValue());

        addAction(ctx, parent, action);

        applyNext(ctx, parent, action);
    }

}
