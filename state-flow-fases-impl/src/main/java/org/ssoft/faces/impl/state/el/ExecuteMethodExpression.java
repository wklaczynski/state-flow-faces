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
package org.ssoft.faces.impl.state.el;

import com.sun.faces.util.FacesLogger;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.MethodExpression;
import javax.el.MethodInfo;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.PostAddToViewEvent;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.validator.ValidatorException;
import javax.faces.view.Location;
import org.ssoft.faces.impl.state.execute.ExecutorContextStackManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteMethodExpression extends MethodExpression {

    private static final long serialVersionUID = -6281398928485392830L;
    
    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.FACELETS_EL.getLogger();

    private final MethodExpression delegate;
    private final Location location;
    private final UIComponent component;
    private String ccClientId;


    public ExecuteMethodExpression(MethodExpression delegate) {
        this.delegate = delegate;
        this.location = null;
        FacesContext ctx = FacesContext.getCurrentInstance();
        component = UIComponent.getCurrentComponent(ctx);
    }


    public ExecuteMethodExpression(Location location, MethodExpression delegate) {
        this.delegate = delegate;
        this.location = location;
        FacesContext ctx = FacesContext.getCurrentInstance();
        component = UIComponent.getCurrentComponent(ctx);
    }


    @Override
    public MethodInfo getMethodInfo(ELContext elContext) {
        return delegate.getMethodInfo(elContext);
    }

    @Override
    public Object invoke(ELContext elContext, Object[] objects) {
        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        try {
            boolean pushed = pushExecutor(ctx);
            try {
                return delegate.invoke(elContext, objects);
            } finally {
                if (pushed) {
                    popExecutor(ctx);
                }
            }
        } catch (ELException ele) {
            if (ele.getCause() != null && ele.getCause() instanceof ValidatorException) {
                throw (ValidatorException) ele.getCause();
            }
            throw ele;
        }
    }


    @Override
    public String getExpressionString() {
        return delegate.getExpressionString();
    }


    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        return delegate.equals(o);
    }


    @Override
    public int hashCode() {
        return delegate.hashCode();
    }


    @Override
    public boolean isLiteralText() {
        return delegate.isLiteralText();
    }

    private boolean pushExecutor(FacesContext ctx) {
        if(component == null) {
            return false;
        }
        
        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(ctx);
        ExecuteContext executeContext = manager.findExecuteContextByComponent(ctx, component);
        if(executeContext != null) {
            return manager.push(executeContext);
        }
        return false;
    }


    private void popExecutor(FacesContext ctx) {
        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(ctx);
        manager.pop();
    }

    private class SetClientIdListener implements ComponentSystemEventListener {

        private ExecuteMethodExpression ccME;
        
        public SetClientIdListener() {
        }
        
        public SetClientIdListener(ExecuteMethodExpression ccME) {
            this.ccME = ccME;
        }

        @Override
        public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
            ccME.ccClientId = event.getComponent().getClientId();
            event.getComponent().unsubscribeFromEvent(PostAddToViewEvent.class, this);
        }
    }

}
