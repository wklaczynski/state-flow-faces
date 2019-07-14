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
package org.ssoft.faces.impl.state.cdi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Map;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.env.AbstractContext;
import javax.faces.state.scxml.env.SimpleContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ScopedBeanContext extends AbstractContext implements Serializable {

    /**
     * Constructor.
     *
     */
    public ScopedBeanContext() {
        super(null, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     */
    public ScopedBeanContext(final Context parent) {
        super(parent, null);
    }

    /**
     * Constructor.
     *
     * @param parent A parent Context, can be null
     * @param initialVars A pre-populated initial variables map
     */
    public ScopedBeanContext(Context parent, Map<String, Object> initialVars) {
        super(parent, initialVars);
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        Object states[] = new Object[2];

        Context context = new SimpleContext();
        Context.setCurrentInstance(context);
        states[0] = saveState(context);

        out.writeObject(states);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Context context = new SimpleContext();
        Context.setCurrentInstance(context);

        Object state = in.readObject();
        if (state != null) {
            Object[] blocks = (Object[]) state;
            restoreState(context, blocks[0]);
        }
    }

}
