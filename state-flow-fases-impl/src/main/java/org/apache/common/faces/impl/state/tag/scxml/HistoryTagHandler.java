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
import javax.faces.state.scxml.model.History;
import javax.faces.state.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class HistoryTagHandler extends AbstractFlowTagHandler<History> {

    /**
     *
     */
    protected final TagAttribute id;

    /**
     *
     */
    protected final TagAttribute type;
    
    /**
     *
     * @param config
     */
    public HistoryTagHandler(TagConfig config) {
        super(config, History.class);
        
        in("state", State.class);
       
        this.id = this.getRequiredAttribute("id");
        this.type = this.getAttribute("type");
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
        History target = new History();
        decorate(ctx, parent, target);

        target.setId(id.getValue());
        target.setType(type != null ? type.getValue() : null);

        addHistory(ctx, parent, target);
        
        addTransitionTarget(ctx, parent, target);
        
        applyNext(ctx, parent, target);
        
        
    }

}
