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
package org.ssoft.faces.state.invokers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowStatus;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.invoke.AbstractInvoker;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.invoke.InvokerException;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import org.ssoft.faces.state.utils.AsyncTrigger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SubInvoker extends AbstractInvoker implements Invoker {

    private final static Logger logger = Logger.getLogger(SubInvoker.class.getName());

    /**
     * Cancellation status.
     */
    private boolean cancelled;
    //// Constants
    private String executorPrefix;

    public SubInvoker() {
        super();
    }

    @Override
    public void setParentStateId(final String parentStateId) {
        super.setParentStateId(parentStateId);
        this.executorPrefix = prefix("executor");
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final String source, final Map params) throws InvokerException {
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            StateFlowHandler handler = StateFlowHandler.getInstance();

            String viewId = source;
            int pos = viewId.indexOf("META-INF/resources/");
            if (pos >= 0) {
                viewId = viewId.substring(pos + 18);
            }

            StateChart stateMachine = handler.createStateMachine(fc, viewId);

            handler.startExecutor(fc, parentStateId, stateMachine, params, false);
        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex);
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvents(final FlowTriggerEvent[] evts) throws InvokerException {
        if (cancelled) {
            return;
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        AsyncTrigger execTriger = new AsyncTrigger(instance.getExecutor());

        List<FlowTriggerEvent> slevt = new ArrayList<>();
        for (FlowTriggerEvent event : evts) {
            if (event.getType() == FlowTriggerEvent.EXECUTOR_EVENT && event.getName().startsWith(executorPrefix)) {
                String outcome = event.getName().substring(executorPrefix.length());
                if (outcome.equals("stop")) {
                    FlowTriggerEvent te = new FlowTriggerEvent(invokePrefix + "cancel", FlowTriggerEvent.SIGNAL_EVENT);
                    execTriger.add(te);
                }
            } else {
                slevt.add(event);
            }
        }

        StateFlowExecutor executor = handler.getExecutor(fc, instance.getExecutor());
        if (executor != null) {

            boolean doneBefore = executor.getCurrentStatus().isFinal();
            try {
                FlowTriggerEvent[] subevts = slevt.toArray(new FlowTriggerEvent[slevt.size()]);
                executor.triggerEvents(subevts);
            } catch (ModelException me) {
                throw new InvokerException(me.getMessage(), me.getCause());
            }

            if (!doneBefore && executor.getCurrentStatus().isFinal()) {

                AsyncTrigger finalTriger = new AsyncTrigger(instance.getExecutor());

                FlowStatus status = executor.getCurrentStatus();
                for (State state : status.getStates()) {
                    if (state.isFinal()) {
                        FlowTriggerEvent te = new FlowTriggerEvent(invokePrefix + state.getId() + ".done", FlowTriggerEvent.CHANGE_EVENT);
                        finalTriger.add(te);
                    }
                }
//                FlowContext ctx = executor.getRootContext();
//                if (ctx.has("@result")) {
//                    FlowContext result = (FlowContext) ctx.get("@result");
//                    FlowStatus pstatus = instance.getExecutor().getCurrentStatus();
//                    State pstate = (State) pstatus.getStates().iterator().next();
//                    FlowContext pcontext = instance.getContext(pstate);
//                    pcontext.setLocal("@result", result);
//                }
                finalTriger.start();
            }
        }
        execTriger.start();
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        if (cancelled) {
            return;
        }
        cancelled = true;
        FlowTriggerEvent te = new FlowTriggerEvent(invokePrefix + "cancel", FlowTriggerEvent.SIGNAL_EVENT);
        new AsyncTrigger(instance.getExecutor(), te).start();

        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        StateFlowExecutor executor = handler.getExecutor(fc, instance.getExecutor());
        if (executor != null) {
            handler.stopExecutor(fc, instance.getExecutor());
        }

    }

}
