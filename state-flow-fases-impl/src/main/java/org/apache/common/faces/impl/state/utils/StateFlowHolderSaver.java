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
package org.apache.common.faces.impl.state.utils;

import java.io.Serializable;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowHolderSaver implements Serializable {

    private static final long serialVersionUID = 6470180891722042701L;

    private String className = null;
    private Serializable savedState = null;

    public static final String DYNAMIC_COMPONENT = "com.sun.faces.DynamicComponent";
    
    private enum StateHolderTupleIndices {
        StateHolderSaverInstance,
        ComponentAddedDynamically,
        LastMember
    };

    public boolean componentAddedDynamically() {
        boolean result = false;

        if (null == className && null != savedState) {
            return result;
        }

        if (className == null) {
            return result;
        }

        if (null != savedState) {
            Serializable [] tuple = (Serializable []) savedState;
            result = (Boolean) tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()];
        }

        return result;
    }

    public StateFlowHolderSaver(FacesContext context, Object toSave) {
        if(toSave == null) {
            className = null;
        } else {
            className = toSave.getClass().getName();
        }

        if (toSave instanceof StateHolder) {
            // do not save an attached object that is marked transient.
            if (!((StateHolder) toSave).isTransient()) {
                Serializable [] tuple = new Serializable[StateHolderTupleIndices.LastMember.ordinal()];

                tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()] = (Serializable) ((StateHolder) toSave).saveState(context);
                if (toSave instanceof UIComponent) {
                    tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()] = ((UIComponent)toSave).getAttributes().containsKey(DYNAMIC_COMPONENT) ? Boolean.TRUE : Boolean.FALSE;
                }
                savedState = tuple;
            } else {
                className = null;
            }
        } else if (toSave instanceof Serializable) {
            savedState = (Serializable) toSave;
            className = null;
        }
    }

    public Object restore(FacesContext context) throws IllegalStateException {
        Object result = null;
        Class toRestoreClass;

        if (null == className && null != savedState) {
            return savedState;
        }

        if (className == null) {
            return null;
        }

        try {
            toRestoreClass = loadClass(className, this);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        if (null != toRestoreClass) {
            try {
                result = toRestoreClass.newInstance();
            }
            catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        if (null != result && null != savedState && result instanceof StateHolder) {
            Serializable [] tuple = (Serializable []) savedState;
            ((StateHolder) result).restoreState(context, tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()]);
        }
        return result;
    }


    private static Class loadClass(String name, Object fallbackClass) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return Class.forName(name, false, loader);
    }
}
