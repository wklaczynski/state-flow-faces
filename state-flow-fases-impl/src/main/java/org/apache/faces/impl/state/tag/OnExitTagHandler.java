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
import org.apache.scxml.model.Final;
import org.apache.scxml.model.OnExit;
import org.apache.scxml.model.Parallel;
import org.apache.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import org.apache.scxml.model.EnterableState;
import org.apache.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OnExitTagHandler extends AbstractFlowTagHandler<OnExit> {

    public OnExitTagHandler(TagConfig config) {
        super(config, OnExit.class);

        in("parallel", Parallel.class);
        in("state", State.class);
        in("final", Final.class);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        EnterableState target = (EnterableState) parentElement;
        decorate(ctx, parent, target);

        OnExit executable = new OnExit();

        applyNext(ctx, parent, executable);

        target.addOnExit(executable);
    }

}
