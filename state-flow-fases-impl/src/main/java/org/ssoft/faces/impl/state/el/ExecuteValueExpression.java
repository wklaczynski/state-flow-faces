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
import javax.faces.context.FacesContext;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.view.Location;
import org.ssoft.faces.impl.state.executor.ExecutorStackManager;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class ExecuteValueExpression extends ValueExpression {

    private ValueExpression originalVE;
    private Location location;

    public ExecuteValueExpression() {
    }

    public ExecuteValueExpression(Location location, ValueExpression originalVE) {

        this.originalVE = originalVE;
        this.location = location;

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

        ExecutorStackManager manager = ExecutorStackManager.getManager(ctx);
        SCXMLExecutor cc = manager.findExecutorUsingLocation(ctx, location);
        return manager.push(cc);

    }


    private void popExecutor(FacesContext ctx) {

        ExecutorStackManager manager = ExecutorStackManager.getManager(ctx);
        manager.pop();

    }    
    

}