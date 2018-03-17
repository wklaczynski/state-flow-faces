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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.ModelException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class If extends Action {

    /**
     * The set of executable elements (those that inheriting from Action) that
     * are contained in this &lt;if&gt; element.
     */
    private final List<Action> actions;

    /**
     * The boolean value that dictates whether the particular child action
     * should be executed.
     */
    private boolean execute;

    /**
     * Constructor.
     */
    public If() {
        super();
        this.actions = new ArrayList();
        this.execute = false;
    }

    /**
     * Get the executable actions contained in this &lt;if&gt;.
     *
     * @return Returns the actions.
     */
    public final List getActions() {
        return actions;
    }

    /**
     * Add an Action to the list of executable actions contained in this
     * &lt;if&gt;.
     *
     * @param action The action to add.
     */
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance instance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {

        execute = (boolean) getAttribute("cond");
        execute = true;

        // The "if" statement is a "container"
        for (Action aa : actions) {
            if (execute && !(aa instanceof ElseIf) && !(aa instanceof Else)) {
                aa.execute(evtDispatcher, errRep, instance, derivedEvents);
            } else if (execute && (aa instanceof ElseIf || aa instanceof Else)) {
                break;
            } else if (aa instanceof Else) {
                execute = true;
            } else if (aa instanceof ElseIf) {
                execute = (boolean) ((ElseIf) aa).getAttribute("cond");
            }
        }
    }

}
