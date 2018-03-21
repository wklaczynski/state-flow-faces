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
package javax.faces.state.model;

import java.util.Collection;
import java.util.logging.Level;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Var extends Action {

    /**
     * The name of the variable to be created.
     */
    private String name;

    /**
     * Constructor.
     */
    public Var() {
        super();
    }

    /**
     * Get the name of the (new) variable.
     *
     * @return String Returns the name.
     */
    public final String getName() {
        return name;
    }

    /**
     * Set the name of the (new) variable.
     *
     * @param name The name to set.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {

        FlowContext ctx = scInstance.getContext(getParentTransitionTarget());
        Object varObj = getAttribute("expr");
        ctx.setLocal(name, varObj);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "<var>: Defined variable ''{0}'' with initial value ''{1}''", new Object[]{name, String.valueOf(varObj)});
        }
        FlowTriggerEvent ev = new FlowTriggerEvent(name + ".change", FlowTriggerEvent.CHANGE_EVENT);
        derivedEvents.add(ev);
    }

}