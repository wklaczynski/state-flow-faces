/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
