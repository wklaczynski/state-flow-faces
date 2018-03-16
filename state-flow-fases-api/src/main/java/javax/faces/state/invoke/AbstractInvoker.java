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

import java.lang.reflect.Field;
import static javax.faces.component.UIComponentBase.restoreAttachedState;
import static javax.faces.component.UIComponentBase.saveAttachedState;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import static javax.faces.state.FlowInstance.saveStatefullState;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.annotation.Statefull;

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
        if (context == null) {
            throw new NullPointerException();
        }
        Object values[] = new Object[3];
        values[0] = type;
        values[1] = parentStateId;
        values[2] = saveStatefullState(context, getClass(), this);

        return values;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object values[] = (Object[]) state;
        type = (String) values[0];
        parentStateId = (String) values[1];
        FlowInstance.restoreStatefullState(context, values[2], getClass(), this);

    }


}
