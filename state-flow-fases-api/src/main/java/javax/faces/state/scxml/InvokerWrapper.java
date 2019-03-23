/*
 * Copyright 2019 Waldemar Kłaczyński.
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
package javax.faces.state.scxml;

import java.util.Map;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.io.StateHolder;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class InvokerWrapper implements Invoker, StateHolder {

    protected Invoker wrapped;

    public InvokerWrapper(Invoker wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getInvokeId() {
        return wrapped.getInvokeId();
    }

    @Override
    public void setInvokeId(String invokeId) {
        wrapped.setInvokeId(invokeId);
    }

    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor scxmlExecutor) {
        wrapped.setParentSCXMLExecutor(scxmlExecutor);
    }

    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        return wrapped.getChildIOProcessor();
    }

    @Override
    public void invoke(InvokeContext ictx, String url, Map<String, Object> params) throws InvokerException {
        wrapped.invoke(ictx, url, params);
    }

    @Override
    public void invokeContent(InvokeContext ictx, String content, Map<String, Object> params) throws InvokerException {
        wrapped.invokeContent(ictx, content, params);
    }

    @Override
    public void parentEvent(InvokeContext ictx, TriggerEvent event) throws InvokerException {
        wrapped.parentEvent(ictx, event);
    }

    @Override
    public void cancel() throws InvokerException {
        wrapped.cancel();
    }

    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[2];

        if (wrapped instanceof StateHolder) {
            values[0] = ((StateHolder) wrapped).saveState(context);
            values[1] = null;
        } else {
            values[0] = null;
            values[1] = wrapped;
        }
        return values;
    }

    @Override
    public void restoreState(Context context, Object state) {
        if (state == null) {
            return;
        }
        Object[] values = (Object[]) state;
        if (values.length >= 2) {
            if (wrapped instanceof StateHolder) {
                ((StateHolder) wrapped).restoreState(context, values[0]);
            } else {
                wrapped = (Invoker) values[1];
            }
        }

    }
    
    public Invoker unwrap(Invoker invoker) {
        if(invoker instanceof InvokerWrapper) {
            return unwrap(((InvokerWrapper) invoker).wrapped);
        } else {
            return invoker;
        }
    }

}
