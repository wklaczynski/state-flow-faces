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
package org.ssoft.faces.state;

import java.io.IOException;
import java.util.concurrent.Callable;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.PathResolver;
import javax.faces.state.PathResolverHolder;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.State;
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
    protected void postNewInvoker(Invoke invoke, Invoker invoker) throws IOException {
        FacesContext fc = FacesContext.getCurrentInstance();

        PathResolver pr = invoke.getPathResolver();
        try {
            if (pr != null) {
                FlowInstance.push(PathResolver.class, pr);
            }

            if (invoker instanceof PathResolverHolder) {
                PathResolverHolder ph = (PathResolverHolder) invoker;
                ph.setPathResolver(pr);
            }

            if (Util.isCdiAvailable(fc)) {
                BeanManager bm = Util.getCdiBeanManager(fc);
                CdiUtil.injectFields(bm, invoker);
            }

            Util.postConstruct(invoker);

        } finally {
            if (pr != null) {
                FlowInstance.pop(PathResolver.class, pr);
            }
        }
    }

    @Override
    protected <V> V processInvoker(State target, Invoke invoke, Invoker invoker, Callable<V> fn) throws Exception {
        FacesContext fc = FacesContext.getCurrentInstance();

        PathResolver pr = invoke.getPathResolver();

        try {
            if (pr != null) {
                FlowInstance.push(PathResolver.class, pr);
            }

            return fn.call();

        } finally {
            if (pr != null) {
                FlowInstance.pop(PathResolver.class, pr);
            }
        }

    }

}
