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
import javax.faces.state.model.History;
import javax.faces.state.model.Initial;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TransitionTagHandler extends AbstractFlowTagHandler<Transition> {

    public static final String CURRENT_TRANSITION = "facelets.CURRENT_TRANSITION";

    protected final TagAttribute event;
    protected final TagAttribute cond;
    protected final TagAttribute target;
    protected final TagAttribute type;

    public TransitionTagHandler(TagConfig config) {
        super(config, Transition.class);

        in("scxml", StateChart.class);
        in("parallel", Parallel.class);
        in("state", State.class);
        in("history", History.class);
        in("initial", Initial.class);

        this.event = this.getAttribute("event");
        this.cond = this.getAttribute("cond");
        this.target = this.getAttribute("target");
        this.type = this.getAttribute("type");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {

        if (parentElement instanceof Initial) {
            Initial initial = (Initial) parentElement;
            if (initial.getTransition() != null) {
                throw new TagException(this.tag, "already defined in this element!");
            }
            if (event != null) {
                throw new TagAttributeException(event, "illegal definition attribute in initial transition");
            }
            if (cond != null) {
                throw new TagAttributeException(cond, "illegal definition attribute in initial transition");
            }
        }

        Transition transition = new Transition();
        decorate(ctx, parent, transition);

        if (type != null) {
            String tvalue = type.getValue();
            if (tvalue.equals("internal") || tvalue.equals("external")) {
                transition.setType(tvalue);
            } else {
                throw new TagAttributeException(type, String.format("illegal tranisition type \"%s\", transition tyme mus be match \"internal\" or \"external\".", tvalue));
            }
        }

        if (event != null) {
            transition.setEvent(event.getValue());
        }

        if (cond != null) {
            transition.setCond(cond.getValueExpression(ctx, Boolean.class));
        }

        if (target != null) {
            transition.setNext(target.getValue());
        }

        applyNext(ctx, parent, transition);

        addTransition(ctx, parent, transition);
    }

}