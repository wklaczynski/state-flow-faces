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
import org.apache.common.faces.state.scxml.model.Datamodel;
import org.apache.common.faces.state.scxml.model.Parallel;
import org.apache.common.faces.state.scxml.model.State;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataModelTagHandler extends AbstractFlowTagHandler<Datamodel> {

    /**
     *
     * @param config
     */
    public DataModelTagHandler(TagConfig config) {
        super(config, Datamodel.class);
        
        in("scxml", SCXML.class);
        in("parallel", Parallel.class);
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
        if(isDatamodel(ctx, parent)) {
            throw new TagException(this.tag, "already defined in this element!");
        }
        
        Datamodel datamodel = new Datamodel();
        decorate(ctx, parent, datamodel);
        
        setDatamodel(ctx, parent, datamodel);
        
        applyNext(ctx, parent, datamodel);
    }

    /**
     *
     * @param ctx
     * @param parent
     * @param datamodel
     * @throws IOException
     */
    protected void setDatamodel(FaceletContext ctx, UIComponent parent, Datamodel datamodel) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof SCXML) {
            SCXML chat = (SCXML) currentFlow;
            chat.setDatamodel(datamodel);
        } else if (currentFlow instanceof Parallel) {
            Parallel parallel = (Parallel) currentFlow;
            parallel.setDatamodel(datamodel);
        } else if (currentFlow instanceof State) {
            State state = (State) currentFlow;
            state.setDatamodel(datamodel);
        } else {
            throw new TagException(this.tag, "can not stored this element on parent element!");
        }
    }

    /**
     *
     * @param ctx
     * @param parent
     * @return
     * @throws IOException
     */
    protected boolean isDatamodel(FaceletContext ctx, UIComponent parent) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        boolean result = false;
        if (currentFlow instanceof SCXML) {
            SCXML chat = (SCXML) currentFlow;
            result = chat.getDatamodel() != null;
        } else if (currentFlow instanceof Parallel) {
            Parallel parallel = (Parallel) currentFlow;
            result = parallel.getDatamodel() != null;
        } else if (currentFlow instanceof State) {
            State state = (State) currentFlow;
            result = state.getDatamodel() != null;
        }
        return result;
    }
    
    
}
