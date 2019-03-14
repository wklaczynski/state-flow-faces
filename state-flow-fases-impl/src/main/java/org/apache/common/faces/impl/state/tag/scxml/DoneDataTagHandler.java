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
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.common.faces.state.scxml.model.DoneData;
import org.apache.common.faces.state.scxml.model.Final;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DoneDataTagHandler extends AbstractFlowTagHandler<DoneData> {

    /**
     *
     * @param config
     */
    public DoneDataTagHandler(TagConfig config) {
        super(config, DoneData.class);
        
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
        Final continer = (Final) parentElement;
        if(continer.getDoneData()!= null) {
            throw new TagException(this.tag, "already defined in this element!");
        }
        
        DoneData donedata = new DoneData();
        decorate(ctx, parent, donedata);
        
        continer.setDoneData(donedata);
        
        applyNext(ctx, parent, donedata);
    }
    
}
