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
package org.ssoft.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.state.scxml.model.Final;
import javax.faces.state.scxml.model.OnEntry;
import javax.faces.state.scxml.model.Parallel;
import javax.faces.state.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnEntryTagHandler extends AbstractFlowTagHandler<OnEntry> {

    /**
     *
     * @param config
     */
    public OnEntryTagHandler(TagConfig config) {
        super(config, OnEntry.class);

        in("parallel", Parallel.class);
        in("state", State.class);
        in("final", Final.class);
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
        EnterableState target = (EnterableState) parentElement;
        decorate(ctx, parent, target);
        
        OnEntry executable = new OnEntry();

        target.addOnEntry(executable);

        applyNext(ctx, parent, executable);
    }

}
