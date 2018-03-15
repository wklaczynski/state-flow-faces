/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state;

import java.io.IOException;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.invoke.Invoker;
import org.ssoft.faces.state.cdi.CdiUtil;
import org.ssoft.faces.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowInstanceImpl extends FlowInstance {

    public FlowInstanceImpl(StateFlowExecutor executor) {
        super(executor);
    }

    @Override
    protected void decorateInvoker(Invoker invoker) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();

        if (Util.isCdiAvailable(fc)) {
            BeanManager bm = Util.getCdiBeanManager(fc);
            CdiUtil.injectFields(bm, invoker);
        }
    }

}
