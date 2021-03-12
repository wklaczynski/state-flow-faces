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
package javax.faces.state.task;

import java.io.IOException;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FacesProcessHolder {
    
    /**
     * <p><span class="changed_modified_2_0">Perform</span> the
     * element tree processing required by the <em>Apply Request
     * Values</em> phase of the request processing lifecycle for all
     * facets of this component, all children of this component, and
     * this component itself, as follows.</p>

     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    void processDecodes(FacesContext context);

    /**
     * <p>Begin render this element and all children
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    void encodeBegin(FacesContext context) throws IOException;

    /**
     * <p>End render this element, only root execution 
     * @param context {@link FacesContext} for the request we are processing
     *
     * @throws IOException if an input/output error occurs while rendering
     * @throws NullPointerException if <code>context</code>
     *  is <code>null</code>
     */
    void encodeEnd(FacesContext context) throws IOException;
    
}
