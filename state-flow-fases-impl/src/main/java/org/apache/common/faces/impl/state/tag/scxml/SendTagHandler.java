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
import javax.faces.state.scxml.model.Finalize;
import javax.faces.state.scxml.model.OnEntry;
import javax.faces.state.scxml.model.OnExit;
import javax.faces.state.scxml.model.Send;
import javax.faces.state.scxml.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.ActionsContainer;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SendTagHandler extends AbstractFlowTagHandler<Send> {

    /**
     *
     */
    protected final TagAttribute event;

    /**
     *
     */
    protected final TagAttribute target;

    /**
     *
     */
    protected final TagAttribute type;

    /**
     *
     */
    protected final TagAttribute id;

    /**
     *
     */
    protected final TagAttribute idlocation;

    /**
     *
     */
    protected final TagAttribute delay;

    /**
     *
     */
    protected final TagAttribute namelist;

    /**
     *
     * @param config
     */
    public SendTagHandler(TagConfig config) {
        super(config, Send.class);

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
        this.target = this.getAttribute("target");
        this.type = this.getAttribute("type");
        this.id = this.getAttribute("id");
        this.idlocation = this.getAttribute("idlocation");
        this.delay = this.getAttribute("delay");
        this.namelist = this.getAttribute("namelist");
    }

    /**
     *
     * @param ctx
     * @param parent
     * @param chart
     * @param parentElement
     * @throws IOException
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Send action = new Send();
        decorate(ctx, parent, action);

        if (event.isLiteral()) {
            action.setEvent(event.getValue());
        } else {
            action.setEventexpr(event.getValue());
        }

        if (target != null) {
            if (target.isLiteral()) {
                action.setTarget(target.getValue());
            } else {
                action.setTargetexpr(target.getValue());
            }
        }

        if (type != null) {
            if (type.isLiteral()) {
                action.setType(type.getValue());
            } else {
                action.setTypeexpr(type.getValue());
            }
        }

        if (id != null) {
            action.setId(id.getValue());
        }

        if (idlocation != null) {
            action.setIdlocation(idlocation.getValue());
        }

        if (delay != null) {
            if (delay.isLiteral()) {
                action.setDelay(delay.getValue());
            } else {
                action.setDelayexpr(delay.getValue());
            }
        }

        if (namelist != null) {
            action.setNamelist(namelist.getValue());
        }

        addAction(ctx, parent, action);

        applyNext(ctx, parent, action);
    }

}
