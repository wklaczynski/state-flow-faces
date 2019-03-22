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
package org.ssoft.faces.prime;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PrimeFlowViewHandler extends ViewHandlerWrapper {

    public PrimeFlowViewHandler(ViewHandler wrapped) {
        super(wrapped);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        UIViewRoot viewRoot = super.createView(context, viewId);
        //ComponentUtils.addComponentResource(context, "fix.core.ajax.js", "primeflow", "head");
        return viewRoot;
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        UIViewRoot viewRoot = super.restoreView(context, viewId);
        //ComponentUtils.addComponentResource(context, "fix.core.ajax.js", "primeflow", "head");
        return viewRoot;
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot viewToRender) throws IOException, FacesException {
        //ComponentUtils.addComponentResource(context, "fix.core.ajax.js", "primeflow", "head");
        super.renderView(context, viewToRender); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    
    
}
