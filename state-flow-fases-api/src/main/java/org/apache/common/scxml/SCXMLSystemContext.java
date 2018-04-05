/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.scxml;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.common.scxml.io.StateHolder;
import static org.apache.common.scxml.io.StateHolderSaver.restoreAttachedState;
import static org.apache.common.scxml.io.StateHolderSaver.saveAttachedState;

/**
 * The SCXMLSystemContext is used as a read only Context wrapper and provides
 * the SCXML (read only) system variables which are injected via the unwrapped
 * {@link #getContext()}.
 *
 * @see
 * <a href="http://www.w3.org/TR/scxml/#SystemVariables">http://www.w3.org/TR/scxml/#SystemVariables</a>
 */
public final class SCXMLSystemContext implements Context, StateHolder, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The protected system variables names as defined in the SCXML
     * specification
     *
     * @see
     * <a href="http://www.w3.org/TR/scxml/#SystemVariables">http://www.w3.org/TR/scxml/#SystemVariables</a>
     */
    public static final String EVENT_KEY = "_event";

    /**
     *
     */
    public static final String SESSIONID_KEY = "_sessionid";

    /**
     *
     */
    public static final String SCXML_NAME_KEY = "_name";

    /**
     *
     */
    public static final String IOPROCESSORS_KEY = "_ioprocessors";

    /**
     *
     */
    public static final String X_KEY = "_x";

    /**
     * The Commons SCXML internal
     * {@link #getPlatformVariables() platform variable key} holding the current
     * SCXML status instance *
     */
    public static final String STATUS_KEY = "status";

    /**
     * The Commons SCXML internal
     * {@link #getPlatformVariables() platform variable key} holding the
     * (optionally)
     * <final><donedata/></final> produced data after the current SCXML
     * completed its execution. *
     */
    public static final String FINAL_DONE_DATA_KEY = "finalDoneData";

    /**
     * The set of protected system variables names
     */
    private static final Set<String> PROTECTED_NAMES = new HashSet<>(Arrays.asList(
            EVENT_KEY, SESSIONID_KEY, SCXML_NAME_KEY, IOPROCESSORS_KEY, X_KEY));

    /**
     * The set of transient to restore or save system variables names
     */
    private static final Set<String> TRANSIENT_NAMES = new HashSet<>(Arrays.asList(
            EVENT_KEY, IOPROCESSORS_KEY, X_KEY, STATUS_KEY));
    
    
    /**
     * The wrapped system context
     */
    private Context systemContext;

    /**
     * The auto-generated next sessionId prefixed ID
     *
     * @see #generateSessionId()
     */
    private long nextSessionSequenceId;

    /**
     * Initialize or replace systemContext
     *
     * @param systemContext the system context to set
     * @throws java.lang.NullPointerException if systemContext == null
     */
    void setSystemContext(Context systemContext) {
        if (this.systemContext != null) {
            // replace systemContext
            systemContext.getVars().putAll(this.systemContext.getVars());
        } else {
            // create Platform variables map
            systemContext.setLocal(X_KEY, new HashMap<String, Object>());
        }
        this.systemContext = systemContext;
        this.protectedVars = Collections.unmodifiableMap(systemContext.getVars());
    }

    /**
     * The unmodifiable wrapped variables map from the wrapped system context
     */
    private Map<String, Object> protectedVars;

    /**
     *
     * @param systemContext
     */
    public SCXMLSystemContext(Context systemContext) {
        setSystemContext(systemContext);
    }

    /**
     *
     * @return
     */
    public String generateSessionId() {
        return getContext().get(SESSIONID_KEY) + "-" + nextSessionSequenceId++;
    }

    @Override
    public void set(final String name, final Object value) {
        if (PROTECTED_NAMES.contains(name)) {
            throw new UnsupportedOperationException();
        }
        // non-protected variables are set on the parent of the system context (e.g. root context)
        systemContext.getParent().set(name, value);
    }

    @Override
    public void setLocal(final String name, final Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(final String name) {
        return systemContext.get(name);
    }

    @Override
    public boolean has(final String name) {
        return systemContext.has(name);
    }

    @Override
    public boolean hasLocal(final String name) {
        return systemContext.hasLocal(name);
    }

    @Override
    public Map<String, Object> getVars() {
        return protectedVars;
    }

    @Override
    public void reset() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Context getParent() {
        return systemContext.getParent();
    }

    @Override
    public SCXMLSystemContext getSystemContext() {
        return this;
    }

    /**
     * @return The Platform specific system variables map stored under the
     * {@link #X_KEY _x} root system variable
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPlatformVariables() {
        return (Map<String, Object>) get(X_KEY);
    }

    /**
     * @return Returns the wrapped (modifiable) system context
     */
    Context getContext() {
        return systemContext;
    }

    /**
     *
     * @param context
     * @return
     */
    @Override
    public Object saveState(Context context) {
        Object values[] = new Object[2];

        Context rctx = getSystemContext();
        if (rctx != null) {
            if (rctx instanceof StateHolder) {
                values[0] = saveVarsState(context);
            }
        }

        values[1] = nextSessionSequenceId;
        
        return values;
    }

    /**
     *
     * @param context
     * @param state
     */
    @Override
    public void restoreState(Context context, Object state) {
        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        Context rctx = getSystemContext();
        if (rctx != null) {
            restoreVarsState(context, values[0]);
        }
        
        nextSessionSequenceId = (long) values[1];
    }

    /**
     *
     * @param context
     * @return
     */
    protected Object saveVarsState(Context context) {
        Object state = null;
        Context rctx = getContext();
        Map<String, Object> vars = rctx.getVars();
        if (null != vars && vars.size() > 0) {
            Object[] attached = new Object[vars.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                if (SCXMLSystemContext.TRANSIENT_NAMES.contains(entry.getKey())) {
                    continue;
                }
//                if (SCXMLSystemContext.FINAL_DONE_DATA_KEY.equals(entry.getKey())) {
//                    continue;
//                }

                Object vstate = saveValueState(context, entry.getKey(), entry.getValue());
                attached[i++] = new Object[]{entry.getKey(), vstate};
            }
            state = attached;
        }
        return state;
    }

    /**
     *
     * @param context
     * @param state
     */
    protected void restoreVarsState(Context context, Object state) {
        Context rctx = getContext();
        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                if (value != null) {
                    Object[] entry = (Object[]) value;
                    String key = (String) entry[0];

                    Object vobj = restoreValueState(context, key, entry[1]);
                    rctx.setLocal(key, vobj);
                }
            }
        }
    }

    /**
     *
     * @param context
     * @param name
     * @param value
     * @return
     */
    protected Object saveValueState(Context context, String name, Object value) {
        value = saveAttachedState(context, value);

        return value;
    }

    /**
     *
     * @param context
     * @param name
     * @param state
     * @return
     */
    protected Object restoreValueState(Context context, String name, Object state) {
        Object value = restoreAttachedState(context, state);
        return value;
    }

}
