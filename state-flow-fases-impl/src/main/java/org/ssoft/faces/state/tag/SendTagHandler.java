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
import javax.faces.state.model.Finalize;
import javax.faces.state.model.If;
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.OnExit;
import javax.faces.state.model.Send;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SendTagHandler extends AbstractFlowTagHandler<Send> {

    protected final TagAttribute event;
    protected final TagAttribute target;
    protected final TagAttribute type;
    protected final TagAttribute id;
    protected final TagAttribute delay;
    protected final TagAttribute namelist;

    public SendTagHandler(TagConfig config) {
        super(config, Send.class);

        in("onentry", OnEntry.class);
        in("onexit", OnExit.class);
        in("transition", Transition.class);
        in("finalize", Finalize.class);
        in("if", If.class);

        top("onentry", OnEntry.class);
        top("onexit", OnExit.class);
        top("transition", Transition.class);
        top("finalize", Finalize.class);

        this.event = this.getRequiredAttribute("event");
        this.target = this.getAttribute("target");
        this.type = this.getAttribute("type");
        this.id = this.getAttribute("id");
        this.delay = this.getAttribute("delay");
        this.namelist = this.getAttribute("namelist");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        Send action = new Send();
        decorate(ctx, parent, action);

        action.setEvent(event.getValue());
        action.setTarget(target != null ? target.getValue() : null);
        action.setTargettype(type != null ? type.getValue() : null);
        action.setSendid(id != null ? id.getValue() : null);
        action.setDelay(delay != null ? delay.getValue() : null);
        action.setNamelist(namelist != null ? namelist.getValue() : null);

        applyNext(ctx, parent, action);

        addAction(ctx, parent, action);
    }

}