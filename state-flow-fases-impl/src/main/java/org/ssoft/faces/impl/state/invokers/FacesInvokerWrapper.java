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
package org.ssoft.faces.impl.state.invokers;

import java.util.Map;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.InvokerWrapper;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.utils.ComponentUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FacesInvokerWrapper extends InvokerWrapper {

    public FacesInvokerWrapper(Invoker wrapped) {
        super(wrapped);
    }

    @Override
    public void invoke(InvokeContext ictx, String url, Map<String, Object> params) throws InvokerException {
        super.invoke(ictx, url, params);

        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            Invoker invoker = unwrap(this);
            ComponentUtils.loadResorces(fc, viewRoot, invoker, "head");
        }
    }

    @Override
    public void invokeContent(InvokeContext ictx, String content, Map<String, Object> params) throws InvokerException {
        super.invokeContent(ictx, content, params); //To change body of generated methods, choose Tools | Templates.

        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            Invoker invoker = unwrap(this);
            ComponentUtils.loadResorces(fc, viewRoot, invoker, "head");
        }
    }

    @Override
    public void cancel() throws InvokerException {
        super.cancel();

        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            Invoker invoker = unwrap(this);
            ComponentUtils.loadResorces(fc, viewRoot, invoker, "head");
        }
    }


    
}
