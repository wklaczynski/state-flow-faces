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

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.view.Location;
import org.ssoft.faces.impl.state.execute.ExecutorContextStackManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class ExecuteValueExpression extends ValueExpression {

    private final ValueExpression originalVE;
    private final Location location;
    private final UIComponent component;

    public ExecuteValueExpression(ValueExpression originalVE) {
        this.originalVE = originalVE;
        this.location = null;

        FacesContext ctx = FacesContext.getCurrentInstance();
        component = UIComponent.getCurrentComponent(ctx);
    }

    public ExecuteValueExpression(Location location, ValueExpression originalVE) {
        this.originalVE = originalVE;
        this.location = location;

        FacesContext ctx = FacesContext.getCurrentInstance();
        component = UIComponent.getCurrentComponent(ctx);
    }

    @Override
    public Object getValue(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getValue(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public void setValue(ELContext elContext, Object o) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            originalVE.setValue(elContext, o);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public boolean isReadOnly(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.isReadOnly(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public Class<?> getType(ELContext elContext) {

        FacesContext ctx = (FacesContext) elContext.getContext(FacesContext.class);
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getType(elContext);
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public Class<?> getExpectedType() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        boolean pushed = pushExecutor(ctx);
        try {
            return originalVE.getExpectedType();
        } finally {
            if (pushed) {
                popExecutor(ctx);
            }
        }

    }

    @Override
    public String getExpressionString() {
        return originalVE.getExpressionString();
    }

    @SuppressWarnings({"EqualsWhichDoesntCheckParameterClass"})
    @Override
    public boolean equals(Object o) {
        return originalVE.equals(o);
    }

    @Override
    public int hashCode() {
        return originalVE.hashCode();
    }

    @Override
    public boolean isLiteralText() {
        return originalVE.isLiteralText();
    }

    @Override
    public String toString() {
        return originalVE.toString();
    }

    /**
     * @return the {@link Location} of this <code>ValueExpression</code>
     */
    public Location getLocation() {
        return location;
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

}
