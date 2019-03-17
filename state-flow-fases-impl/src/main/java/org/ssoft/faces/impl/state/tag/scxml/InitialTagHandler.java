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
import javax.faces.state.scxml.model.Initial;
import javax.faces.state.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class InitialTagHandler extends AbstractFlowTagHandler<Initial> {

    /**
     *
     * @param config
     */
    public InitialTagHandler(TagConfig config) {
        super(config, Initial.class);
        
        in("state", State.class);
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
        State state = (State) parentElement;
        if(state.getInitial()!= null) {
            throw new TagException(this.tag, "already defined in this element!");
        }

        Initial target = new Initial();
        decorate(ctx, parent, target);
        
        state.setInitial(target);

        applyNext(ctx, parent, target);
    }

}
