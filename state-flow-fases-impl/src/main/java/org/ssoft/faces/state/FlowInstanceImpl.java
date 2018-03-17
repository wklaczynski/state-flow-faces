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
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowInstance;
import javax.faces.state.PathResolver;
import javax.faces.state.PathResolverHolder;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.invoke.Invoker;
import javax.faces.state.model.Action;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.State;
import javax.faces.state.model.TransitionTarget;
import org.ssoft.faces.state.cdi.CdiUtil;
import org.ssoft.faces.state.el.BuiltinFunctionMapper;
import org.ssoft.faces.state.el.CompositeFunctionMapper;
import org.ssoft.faces.state.impl.VariableMapperWrapper;
import org.ssoft.faces.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class FlowInstanceImpl extends FlowInstance {

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";
    
    
    private final ELContext ctx;
    private VariableMapper varMapper;
    private FunctionMapper fnMapper;
    private final ExpressionFactory elFactory;

    @SuppressWarnings("LeakingThisInConstructor")
    public FlowInstanceImpl(StateFlowExecutor executor, FacesContext facesContext) {
        super(executor, facesContext);
        this.ctx = facesContext.getELContext();
        this.elFactory = facesContext.getApplication().getExpressionFactory();
        facesContext.getAttributes().put(FLOW_CONTEXT_KEY, this);

        this.varMapper = new VariableMapperWrapper(ctx.getVariableMapper());
        this.fnMapper = new CompositeFunctionMapper(new BuiltinFunctionMapper(), ctx.getFunctionMapper());

        putContext(StateFlowExecutor.class, executor);
    }

    @Override
    protected void postNewInvoker(Invoke invoke, Invoker invoker) throws IOException {
        FacesContext fc = getFacesContext();

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
        FacesContext fc = getFacesContext();
        PathResolver pr = invoke.getPathResolver();

        FlowContext flowCtx = getContext(target);
        putContext(FlowContext.class, flowCtx);

        try {
            if (pr != null) {
                FlowInstance.push(PathResolver.class, pr);
            }

            return fn.call();

        } finally {
            if (pr != null) {
                FlowInstance.pop(PathResolver.class, pr);
            }
            putContext(FlowContext.class, getEvaluator().newContext(null, null));
        }

    }

    @Override
    protected <V> V processExecute(Action action, Callable<V> fn) throws Exception {

        TransitionTarget parentTarget = action.getParentTransitionTarget();
        
        FlowContext flowCtx = getContext(parentTarget);
        flowCtx.setLocal(NAMESPACES_KEY, action.getNamespaces());

        putContext(FlowContext.class, flowCtx);
        try {
            return fn.call();

        } finally {
            putContext(FlowContext.class, getEvaluator().newContext(null, null));
            flowCtx.setLocal(NAMESPACES_KEY, null);
        }
    }


    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletContext#setVariableMapper(javax.el.VariableMapper)
     */
    @Override
    public void setVariableMapper(VariableMapper varMapper) {
        // Assert.param("varMapper", varMapper);
        this.varMapper = varMapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletContext#setFunctionMapper(javax.el.FunctionMapper)
     */
    @Override
    public void setFunctionMapper(FunctionMapper fnMapper) {
        // Assert.param("fnMapper", fnMapper);
        this.fnMapper = fnMapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ELContext#getFunctionMapper()
     */
    @Override
    public FunctionMapper getFunctionMapper() {
        return this.fnMapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ELContext#getVariableMapper()
     */
    @Override
    public VariableMapper getVariableMapper() {
        return this.varMapper;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ELContext#getContext(java.lang.Class)
     */
    @Override
    public Object getContext(Class key) {
        return this.ctx.getContext(key);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ELContext#putContext(java.lang.Class, java.lang.Object)
     */
    @Override
    public void putContext(Class key, Object contextObject) {
        this.ctx.putContext(key, contextObject);
    }


    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletContext#getAttribute(java.lang.String)
     */
    @Override
    public Object getAttribute(String name) {
        if (this.varMapper != null) {
            ValueExpression ve = this.varMapper.resolveVariable(name);
            if (ve != null) {
                return ve.getValue(this);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.faces.view.facelets.FaceletContext#setAttribute(java.lang.String,
     *      java.lang.Object)
     */
    @Override
    public void setAttribute(String name, Object value) {
        if (this.varMapper != null) {
            if (value == null) {
                this.varMapper.setVariable(name, null);
            } else {
                this.varMapper.setVariable(name,
                        this.elFactory.createValueExpression(value, Object.class));
            }
        }
    }

    @Override
    public ELResolver getELResolver() {
        return this.ctx.getELResolver();
    }

}
