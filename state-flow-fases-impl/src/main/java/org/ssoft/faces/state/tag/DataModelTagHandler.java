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
import javax.faces.state.model.Datamodel;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataModelTagHandler extends AbstractFlowTagHandler<Datamodel> {

    public DataModelTagHandler(TagConfig config) {
        super(config, Datamodel.class);
        
        in("scxml", StateChart.class);
        in("parallel", Parallel.class);
        in("state", State.class);
    }
    
    @Override
    public void apply(FaceletContext ctx, UIComponent parent, StateChart chart, Object parentElement) throws IOException {
        if(isDatamodel(ctx, parent)) {
            throw new TagException(this.tag, "already defined in this element!");
        }
        
        Datamodel datamodel = new Datamodel();
        decorate(ctx, parent, datamodel);
        
        applyNext(ctx, parent, datamodel);
        
        setDatamodel(ctx, parent, datamodel);
    }


    protected void setDatamodel(FaceletContext ctx, UIComponent parent, Datamodel datamodel) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        if (currentFlow instanceof StateChart) {
            StateChart chat = (StateChart) currentFlow;
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

    protected boolean isDatamodel(FaceletContext ctx, UIComponent parent) throws IOException {
        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);
        boolean result = false;
        if (currentFlow instanceof StateChart) {
            StateChart chat = (StateChart) currentFlow;
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
