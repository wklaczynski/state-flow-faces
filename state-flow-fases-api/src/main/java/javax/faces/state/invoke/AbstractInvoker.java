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
package javax.faces.state.invoke;

import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class AbstractInvoker implements Invoker {

    protected String type;
    protected String parentStateId;
    protected FlowInstance instance;

    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    public String getParentStateId() {
        return parentStateId;
    }

    @Override
    public void setParentStateId(String parentStateId) {
        this.parentStateId = parentStateId;
    }

    public FlowInstance getInstance() {
        return instance;
    }

    @Override
    public void setInstance(FlowInstance instance) {
        this.instance = instance;
    }

    @Override
    public void parentEvents(FlowTriggerEvent[] evts) throws InvokerException {

    }

    @Override
    public void cancel() throws InvokerException {

    }

    @Override
    public Object saveState(FacesContext context) {
        return null;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {

    }

}
