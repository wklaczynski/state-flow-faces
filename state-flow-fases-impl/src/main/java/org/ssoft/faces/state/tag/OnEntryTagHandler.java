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
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.TransitionTarget;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnEntryTagHandler extends AbstractFlowTagHandler<OnEntry> {

    public OnEntryTagHandler(TagConfig config) {
        super(config, OnEntry.class);

        in("parallel", Parallel.class);
        in("state", State.class);
        in("final", Final.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        TransitionTarget target = (TransitionTarget) parentElement;
        decorate(ctx, parent, target);
        
        if (target.getOnEntry() != null) {
            throw new TagException(this.tag, "already defined in this element!");
        }

        OnEntry executable = new OnEntry();

        applyNext(ctx, parent, executable);

        target.setOnEntry(executable);
    }

}
