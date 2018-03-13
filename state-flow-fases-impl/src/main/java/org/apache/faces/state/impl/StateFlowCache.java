/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.impl;

import java.io.IOException;
import java.net.URL;
import javax.faces.state.ModelException;
import javax.faces.state.model.StateChart;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class StateFlowCache {

    public interface MemberFactory {

        public StateChart newInstance(final URL key) throws IOException, ModelException;

    }

    public abstract StateChart getStateFlow(URL url) throws ModelException;

    public abstract boolean isStateFlowCached(URL url);

    public void setCacheFactories(MemberFactory stateFlowFactory) {
        this.memberFactory = stateFlowFactory;
    }

    private MemberFactory memberFactory;

    public MemberFactory getMemberFactory() {
        return memberFactory;
    }

}
