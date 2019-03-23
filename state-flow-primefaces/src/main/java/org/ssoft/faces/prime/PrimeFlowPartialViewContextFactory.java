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
package org.ssoft.faces.prime;

import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.context.PartialViewContextFactory;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PrimeFlowPartialViewContextFactory extends PartialViewContextFactory {

    private final PartialViewContextFactory wrapped;

    /**
     *
     * @param wrapped
     */
    public PrimeFlowPartialViewContextFactory(PartialViewContextFactory wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public PartialViewContext getPartialViewContext(FacesContext context) {
        return new PrimeFlowPartialViewContext(wrapped.getPartialViewContext(context));
    }

    @Override
    public PartialViewContextFactory getWrapped() {
        return wrapped;
    }

}
