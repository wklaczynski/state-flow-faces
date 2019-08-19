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

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import static javax.faces.application.ResourceHandler.RESOURCE_IDENTIFIER;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextWrapper;
import javax.faces.event.PhaseId;
import org.ssoft.faces.prime.scxml.DialogInvoker;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PrimeFlowPartialViewContext extends PartialViewContextWrapper {

    private final PartialViewContext wrapped;

    public PrimeFlowPartialViewContext(PartialViewContext wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public PartialViewContext getWrapped() {
        return wrapped;
    }

    @Override
    public void processPartial(PhaseId phaseId) {
        
        if (phaseId == PhaseId.RENDER_RESPONSE) {
            if(isRenderAll()) {
                FacesContext context = FacesContext.getCurrentInstance();
                context.getAttributes().remove(RESOURCE_IDENTIFIER);
            }
        }
        
        super.processPartial(phaseId);
    }

//    @Override
//    public boolean isRenderAll() {
//        FacesContext context = FacesContext.getCurrentInstance();
//        Map<Object, Object> attrs = context.getAttributes();
//        
//        if(attrs.containsKey(DialogInvoker.DIALOG_CLOSE)) {
//            return false;
//        }
//        
//        return super.isRenderAll();
//    }
//    
//    @Override
//    public Collection<String> getRenderIds() {
//        FacesContext context = FacesContext.getCurrentInstance();
//        Map<Object, Object> attrs = context.getAttributes();
//        if(attrs.containsKey(DialogInvoker.DIALOG_CLOSE)) {
//            return Collections.EMPTY_SET;
//        }
//        
//        return super.getRenderIds();
//    }
    
}
