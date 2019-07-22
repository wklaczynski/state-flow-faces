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
package javax.faces.state.execute;

import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ExecuteContext {

    /**
     * invokeId ID.
     */
    private final transient String invokeId;
    
    /**
     * View SCXMLExecutor
     */
    private final SCXMLExecutor executor;

    /**
     * View Context
     */
    private final Context context;

    /**
     *
     * @param invokeId
     * @param executor
     * @param context
     */
    public ExecuteContext(String invokeId, SCXMLExecutor executor, Context context) {
        this.invokeId = invokeId;
        this.executor = executor;
        this.context = context;
    }

    /**
     * @return Returns the invokeId for view
     */
    public String getInvokeId() {
        return invokeId;
    }
    
    /**
     * @return Returns the current invoke view context
     */
    public Context getContext() {
        return context;
    }

    /**
     * @return Returns the current invoke view executor
     */
    public SCXMLExecutor getExecutor() {
        return executor;
    }

}
