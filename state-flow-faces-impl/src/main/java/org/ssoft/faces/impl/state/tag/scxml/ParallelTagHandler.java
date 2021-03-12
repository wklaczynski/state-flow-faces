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

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import java.io.IOException;
import javax.faces.state.scxml.model.Parallel;
import javax.faces.state.scxml.model.State;
import org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParallelTagHandler extends AbstractFlowTagHandler<Parallel> {

    /**
     *
     */
    protected final TagAttribute id;
    
    /**
     *
     * @param config
     */
    public ParallelTagHandler(TagConfig config) {
        super(config, Parallel.class);
        
        in("scxml", SCXML.class);
        in("state", State.class);
        
        this.id = this.getAttribute("id");
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
        Parallel parallel = new Parallel();
        decorate(ctx, parent, parallel);
        
        String cid;
        if(id != null) {
            cid = id.getValue();
        } else {
            cid = generateUniqueId(ctx, parent, parallel, "parallel_");
        }
        parallel.setId(cid);
        
        addTransitionTarget(ctx, parent, parallel);
        
        addChild(ctx, parent, parallel);
        
        applyNext(ctx, parent, parallel);
    }

}
