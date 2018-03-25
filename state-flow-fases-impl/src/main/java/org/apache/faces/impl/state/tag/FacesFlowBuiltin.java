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
package org.apache.faces.impl.state.tag;

import java.io.Serializable;
import java.util.logging.Logger;
import org.apache.faces.impl.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FacesFlowBuiltin implements Serializable {

    public static final Logger log = FlowLogger.FLOW.getLogger();
    
    /**
     * Implements the In() predicate for flow documents. The method
     * name chosen is different since &quot;in&quot; is a reserved token
     * in some expression languages.
     *
     * Does this state belong to the given Set of States.
     * Simple ID based comparator, assumes IDs are unique.
     *
     * @param state The State ID to compare with
     * @return Whether this State belongs to this Set
     */
    public static boolean isMember(final String state) {
        
        return false;
    }
}

