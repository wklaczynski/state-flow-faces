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
package org.ssoft.faces.impl.state.invokers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLSystemContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CompositeContext implements Context {

    private final Context root;
    
    private final List<Context> contexts = new ArrayList<>();

    /**
     *
     * @param root
     * @param contexts
     */
    public CompositeContext(Context root, Context... contexts) {
        this.root = root;
        this.contexts.addAll(Arrays.asList(contexts));
    }

    /**
     *
     * @return
     */
    public List<Context> getContexts() {
        return contexts;
    }

    /**
     *
     * @param context
     */
    public void add(Context context) {
        contexts.add(root);
    }
    
    @Override
    public void set(String name, Object value) {
        Context parent;
        if ((parent = contextAs(name)) != null) {
            parent.set(name, value);
        } else {
            root.set(name, value);
        }
    }

    @Override
    public void remove(String name) {
        Context parent;
        if ((parent = contextAs(name)) != null) {
            parent.remove(name);
        } else {
            root.remove(name);
        }
    }

    
    
    private Context contextAs(String name) {
        for (Context context : contexts) {
            if(context.has(name)) {
                return context;
            }
        }
        return null;
    }

    private Context contextAsLocal(String name) {
        for (Context context : contexts) {
            if(context.hasLocal(name)) {
                return context;
            }
        }
        return null;
    }
    
    
    @Override
    public void setLocal(String name, Object value) {
        Context parent;
        if ((parent = contextAsLocal(name)) != null) {
            parent.set(name, value);
        } else {
            root.setLocal(name, value);
        }
    }
    
    
    @Override
    public void removeLocal(String name) {
        Context parent;
        if ((parent = contextAsLocal(name)) != null) {
            parent.remove(name);
        } else {
            root.removeLocal(name);
        }
    }

    @Override
    public Object get(String name) {
        Context parent;
        if ((parent = contextAs(name)) != null) {
            return parent.get(name);
        } else {
            return  root.get(name);
        }
    }

    @Override
    public boolean has(String name) {
        Context parent;
        if ((parent = contextAs(name)) != null) {
            return true;
        } else {
            return  root.has(name);
        }
    }

    @Override
    public boolean hasLocal(String name) {
        Context parent;
        return (parent = contextAsLocal(name)) != null;
    }

    @Override
    public Map<String, Object> getVars() {
        Map<String, Object> result = new HashMap<>();
        for (Context context : contexts) {
            result.putAll(context.getVars());
        }
        return result;
    }

    @Override
    public void reset() {
        for (Context context : contexts) {
            context.reset();
        }
    }

    @Override
    public Context getParent() {
        return root;
    }

    @Override
    public SCXMLSystemContext getSystemContext() {
        return root.getSystemContext();
    }
    
}
