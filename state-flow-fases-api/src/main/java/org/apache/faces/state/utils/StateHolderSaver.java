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
package org.apache.faces.state.utils;

import java.io.Serializable;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.StateHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.apache.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateHolderSaver implements Serializable {

    private static final Logger LOGGER = Logger.getLogger("javax.faces.flow");

    private String className = null;
    private String ttid = null;
    private Serializable savedState = null;

    public static final String DYNAMIC_COMPONENT
            = "com.sun.faces.flow.DynamicComponent";

    private enum StateHolderTupleIndices {
        StateHolderSaverInstance,
        ComponentAddedDynamically,
        LastMember
    };

    public boolean componentAddedDynamically() {
        boolean result = false;

        // if the Object to save implemented Serializable but not
        // StateHolder
        if (null == className && null != savedState) {
            return result;
        }

        // if the Object to save did not implement Serializable or
        // StateHolder
        if (className == null) {
            return result;
        }

        // else the object to save did implement StateHolder
        if (null != savedState) {
            // don't need to check transient, since that was done on
            // the saving side.
            Serializable[] tuple = (Serializable[]) savedState;
            result = (Boolean) tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()];
        }

        return result;
    }

    public StateHolderSaver(FacesContext context, Object toSave) {
        if (toSave != null) {
            className = toSave.getClass().getName();

            if (toSave instanceof TransitionTarget) {
                TransitionTarget tt = (TransitionTarget) toSave;
                ttid = tt.getId();
            } else if (toSave instanceof StateHolder) {
                // do not save an attached object that is marked transient.
                if (!((StateHolder) toSave).isTransient()) {
                    Serializable[] tuple = new Serializable[StateHolderTupleIndices.LastMember.ordinal()];

                    tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()]
                            = (Serializable) ((StateHolder) toSave).saveState(context);
                    if (toSave instanceof UIComponent) {
                        tuple[StateHolderTupleIndices.ComponentAddedDynamically.ordinal()] = ((UIComponent) toSave).getAttributes().containsKey(DYNAMIC_COMPONENT) ? Boolean.TRUE : Boolean.FALSE;
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
    }

    /**
     *
     * @param context
     * @return the restored {@link StateHolder} instance.
     */
    public Object restore(FacesContext context) throws IllegalStateException {
        Object result = null;
        Class toRestoreClass;

        // if the Object to save implemented Serializable but not
        // StateHolder
        if (null == className && null != savedState) {
            return savedState;
        }

        // if the Object to save did not implement Serializable or
        // StateHolder
        if (className == null) {
            return null;
        }

        if (ttid != null) {
//            SCXML chart = (SCXML) context.getAttributes().get(STATE_MACHINE_HINT);
//            Object found = chart.findElement(ttid);
//            if (found == null) {
//                throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
//            }

//            TransitionTarget tt = (TransitionTarget) found;
            return null;
        }

        // else the object to save did implement StateHolder
        try {
            toRestoreClass = loadClass(className, this);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

        if (null != toRestoreClass) {
            try {
                result = toRestoreClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        if (null != result && null != savedState
                && result instanceof StateHolder) {
            // don't need to check transient, since that was done on
            // the saving side.
            Serializable[] tuple = (Serializable[]) savedState;
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

    public static Object saveAttachedState(FacesContext context, Object attachedObject) {
        if (null == context) {
            throw new NullPointerException();
        }
        if (null == attachedObject) {
            return null;
        }
        Object result;
        Class mapOrCollectionClass = attachedObject.getClass();
        @SuppressWarnings("UnusedAssignment")
        boolean newWillSucceed = true;
        // first, test for newability of the class.
        try {
            int modifiers = mapOrCollectionClass.getModifiers();
            newWillSucceed = Modifier.isPublic(modifiers);
            if (newWillSucceed) {
                newWillSucceed = null != mapOrCollectionClass.getConstructor();
            }
        } catch (NoSuchMethodException | SecurityException e) {
            newWillSucceed = false;
        }

        if (newWillSucceed && attachedObject instanceof Collection) {
            Collection attachedCollection = (Collection) attachedObject;
            List<StateHolderSaver> resultList = null;
            for (Object item : attachedCollection) {
                if (item != null) {
                    if (item instanceof StateHolder && ((StateHolder) item).isTransient()) {
                        continue;
                    }
                    if (resultList == null) {
                        resultList = new ArrayList<>(attachedCollection.size() + 1);
                        resultList.add(new StateHolderSaver(context, mapOrCollectionClass));
                    }
                    resultList.add(new StateHolderSaver(context, item));
                }
            }
            result = resultList;
        } else if (newWillSucceed && attachedObject instanceof Map) {
            Map<Object, Object> attachedMap = (Map<Object, Object>) attachedObject;
            List<StateHolderSaver> resultList = null;
            Object key, value;
            for (Map.Entry<Object, Object> entry : attachedMap.entrySet()) {
                key = entry.getKey();
                if (key instanceof StateHolder && ((StateHolder) key).isTransient()) {
                    continue;
                }
                value = entry.getValue();
                if (value instanceof StateHolder && ((StateHolder) value).isTransient()) {
                    continue;
                }
                if (resultList == null) {
                    resultList = new ArrayList<>(attachedMap.size() * 2 + 1);
                    resultList.add(new StateHolderSaver(context, mapOrCollectionClass));
                }
                resultList.add(new StateHolderSaver(context, key));
                resultList.add(new StateHolderSaver(context, value));
            }
            result = resultList;
        } else {
            result = new StateHolderSaver(context, attachedObject);
        }

        return result;
    }

    @SuppressWarnings("UseSpecificCatch")
    public static Object restoreAttachedState(FacesContext context, Object stateObj)
            throws IllegalStateException {
        if (null == context) {
            throw new NullPointerException();
        }
        if (null == stateObj) {
            return null;
        }
        Object result;

        if (stateObj instanceof List) {
            List<StateHolderSaver> stateList = (List<StateHolderSaver>) stateObj;
            StateHolderSaver collectionSaver = stateList.get(0);
            Class mapOrCollection = (Class) collectionSaver.restore(context);
            if (Collection.class.isAssignableFrom(mapOrCollection)) {
                Collection<Object> retCollection = null;
                try {
                    retCollection = (Collection<Object>) mapOrCollection.newInstance();
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                    throw new IllegalStateException("Unknown object type");
                }
                for (int i = 1, len = stateList.size(); i < len; i++) {
                    try {
                        retCollection.add(stateList.get(i).restore(context));
                    } catch (ClassCastException cce) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, cce.toString(), cce);
                        }
                        throw new IllegalStateException("Unknown object type");
                    }
                }
                result = retCollection;
            } else {
                // If we were doing assertions: assert(mapOrList.isAssignableFrom(Map.class));
                Map<Object, Object> retMap = null;
                try {
                    retMap = (Map<Object, Object>) mapOrCollection.newInstance();
                } catch (Exception e) {
                    if (LOGGER.isLoggable(Level.SEVERE)) {
                        LOGGER.log(Level.SEVERE, e.toString(), e);
                    }
                    throw new IllegalStateException("Unknown object type");
                }
                for (int i = 1, len = stateList.size(); i < len; i += 2) {
                    try {
                        retMap.put(stateList.get(i).restore(context),
                                stateList.get(i + 1).restore(context));
                    } catch (ClassCastException cce) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, cce.toString(), cce);
                        }
                        throw new IllegalStateException("Unknown object type");
                    }
                }
                result = retMap;

            }
        } else if (stateObj instanceof StateHolderSaver) {
            StateHolderSaver saver = (StateHolderSaver) stateObj;
            result = saver.restore(context);
        } else {
            throw new IllegalStateException("Unknown object type");
        }
        return result;
    }

}
