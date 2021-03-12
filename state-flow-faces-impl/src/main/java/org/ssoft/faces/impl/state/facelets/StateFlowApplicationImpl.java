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
package org.ssoft.faces.impl.state.facelets;

import jakarta.el.ExpressionFactory;
import jakarta.faces.application.Application;
import jakarta.faces.application.ApplicationWrapper;
import jakarta.faces.application.ResourceHandler;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowApplicationImpl extends ApplicationWrapper {

    public StateFlowApplicationImpl(Application wrapped) {
        super(wrapped);
    }

    @Override
    public ExpressionFactory getExpressionFactory() {
        return new ExecuteExpressionFactory(super.getExpressionFactory());
    }

    @Override
    public ResourceHandler getResourceHandler() {
        ResourceHandler handler = super.getResourceHandler(); 
        return new StateFlowResourceHandler(handler);
    }
    
}
