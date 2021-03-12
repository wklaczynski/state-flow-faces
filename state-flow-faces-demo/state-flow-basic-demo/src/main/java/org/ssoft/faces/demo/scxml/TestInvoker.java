/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ssoft.faces.demo.scxml;

import jakarta.faces.application.FacesMessage;
import static jakarta.faces.application.FacesMessage.SEVERITY_INFO;
import jakarta.faces.context.FacesContext;
import java.io.IOException;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.logging.Logger;
import javax.faces.state.annotation.StateChartInvoker;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.io.StateHolder;

/**
 * A test custom invoker {@link Invoker} for SCXML documents. Invoked demo 
 * functions. The invocer must be Serializable or StateHolder for save self
 * state in distribuable session. Serializable is last if invoker implemented
 * StateHolder then save or restore state
 * 
 */
@StateChartInvoker("demo")
public class TestInvoker implements Invoker, StateHolder {

    private final static Logger logger = Logger.getLogger(TestInvoker.class.getName());

    private transient String invokeId;
    private transient SCXMLExecutor executor;
    private boolean cancelled;

    @Override
    public String getInvokeId() {
        return invokeId;
    }

    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
        this.executor = parentSCXMLExecutor;
    }

    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        return null;
    }

    @Override
    public void invoke(final InvokeContext ictx, final String url, final Map<String, Object> params)
            throws InvokerException {
        FacesContext fc = FacesContext.getCurrentInstance();

        FacesMessage facesMessage = new FacesMessage("Yes, I'm starting witch src=" + url + "!");
        facesMessage.setSeverity(SEVERITY_INFO);
        fc.addMessage(null, facesMessage);

    }

    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    @Override
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void parentEvent(final InvokeContext ictx, final TriggerEvent event) throws InvokerException {
        if (cancelled) {
            return;
        }

    }

    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext fc = FacesContext.getCurrentInstance();

        FacesMessage facesMessage = new FacesMessage("Oh, I'm canceling!");
        facesMessage.setSeverity(SEVERITY_INFO);
        fc.addMessage(null, facesMessage);
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object saveState(Context context) {
        Object[] state = new Object[2];
        state[0] = "This remember after session restore in other machine or redeploy";
        return state;
    }

    @Override
    public void restoreState(Context context, Object state) {
        Object[] values = (Object[]) state;
        String remember = (String) values[0];
        
        logger.info(remember);
    }

}
