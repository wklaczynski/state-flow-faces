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
package org.apache.common.scxml.io;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.common.scxml.Context;
import static org.apache.common.scxml.SCXMLConstants.STATE_MACHINE_HINT;
import org.apache.common.scxml.SCXMLLogger;
import org.apache.common.scxml.model.EnterableState;
import org.apache.common.scxml.model.SCXML;
import org.apache.common.scxml.model.TransitionTarget;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateHolderSaver implements Serializable {

    private static final Logger LOGGER = SCXMLLogger.SCXML.getLogger();

    private String className = null;
    private String ttid = null;
    private Serializable savedState = null;

    private enum StateHolderTupleIndices {
        StateHolderSaverInstance,
        LastMember
    };

    /**
     *
     * @param context
     * @param toSave
     */
    public StateHolderSaver(Context context, Object toSave) {
        if (toSave != null) {
            className = toSave.getClass().getName();

            if (toSave instanceof EnterableState) {
                EnterableState tt = (EnterableState) toSave;
                ttid = tt.getId();
            } else if (toSave instanceof StateHolder) {
                Serializable[] tuple = new Serializable[StateHolderTupleIndices.LastMember.ordinal()];

                tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()]
                        = (Serializable) ((StateHolder) toSave).saveState(context);

                savedState = tuple;
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
    public Object restore(Context context) throws IllegalStateException {
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
            SCXML stateMachine = (SCXML) context.get(STATE_MACHINE_HINT);
            Object found = stateMachine.findElement(ttid);
            if (found == null) {
                throw new IllegalStateException(String.format("Restored element %s not found.", ttid));
            }

            TransitionTarget tt = (TransitionTarget) found;
            return tt;
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

        if (null != result && null != savedState && result instanceof StateHolder) {
            // don't need to check transient, since that was done on
            // the saving side.
            Serializable[] tuple = (Serializable[]) savedState;
            ((StateHolder) result).restoreState(context, tuple[StateHolderTupleIndices.StateHolderSaverInstance.ordinal()]);
        }
        return result;
    }

    /**
     *
     * @param name
     * @param fallbackClass
     * @return
     * @throws ClassNotFoundException
     */
    public static Class loadClass(String name, Object fallbackClass) throws ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = fallbackClass.getClass().getClassLoader();
        }
        return Class.forName(name, false, loader);
    }

    /**
     *
     * @param context
     * @param attachedObject
     * @return
     */
    public static Object saveAttachedState(Context context, Object attachedObject) {
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
                value = entry.getValue();
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

    /**
     *
     * @param context
     * @param stateObj
     * @return
     * @throws IllegalStateException
     */
    @SuppressWarnings("UseSpecificCatch")
    public static Object restoreAttachedState(Context context, Object stateObj)
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

    /**
     *
     * @param context
     * @param instance
     * @return
     */
    public static Object saveObjectState(Context context, Object instance) {
        return saveObjectState(context, instance.getClass(), instance);
    }

    private static boolean serializable(int modifiers) {
        if (Modifier.isTransient(modifiers)) {
            return false;
        }
        if (Modifier.isStatic(modifiers)) {
            return false;
        }
        if (Modifier.isNative(modifiers)) {
            return false;
        }
        if (Modifier.isAbstract(modifiers)) {
            return false;
        }
        return !Modifier.isVolatile(modifiers);
    }

    /**
     *
     * @param context
     * @param clazz
     * @param instance
     * @return
     */
    public static Object saveObjectState(Context context, Class<?> clazz, Object instance) {
        if (clazz == null) {
            return null;
        }

        Object results[] = new Object[2];

        Field[] fields = clazz.getDeclaredFields();
        Object[] attached = new Object[fields.length];
        int i = 0;
        for (Field field : fields) {
            Object entry[] = null;
            if (serializable(field.getModifiers())) {
                entry = new Object[2];
                Object value = null;
                boolean accessibility = field.isAccessible();
                try {
                    field.setAccessible(true);
                    value = field.get(instance);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalStateException(String.format("Save statefull field %s error.", field.getName()), ex);
                } finally {
                    field.setAccessible(accessibility);
                }
                entry[0] = field.getName();
                entry[1] = saveValueState(context, field.getName(), value);
            }
            attached[i++] = entry;
        }
        results[0] = attached;
        results[1] = saveObjectState(context, clazz.getSuperclass(), instance);

        return results;
    }

    /**
     *
     * @param context
     * @param state
     * @param instance
     */
    public static void restoreObjectState(Context context, Object state, Object instance) {
        restoreObjectState(context, state, instance.getClass(), instance);
    }

    /**
     *
     * @param context
     * @param state
     * @param clazz
     * @param instance
     */
    public static void restoreObjectState(Context context, Object state, Class<?> clazz, Object instance) {
        if (clazz == null || null == state) {
            return;
        }

        Object[] states = (Object[]) state;

        Field[] fields = clazz.getDeclaredFields();
        Object[] attached = (Object[]) states[0];
        int i = 0;
        for (Field field : fields) {
            if (serializable(field.getModifiers())) {
                Object[] entry = (Object[]) attached[i];
                Object value = restoreValueState(context, field.getName(), entry[1]);
                boolean accessibility = field.isAccessible();
                try {
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    throw new IllegalStateException(String.format("Restored statefull field %s error.", field.getName()), ex);
                } finally {
                    field.setAccessible(accessibility);
                }
            }
            i++;
        }
        restoreObjectState(context, state, clazz.getSuperclass(), instance);

    }

    /**
     *
     * @param context
     * @param name
     * @param value
     * @return
     */
    public static Object saveValueState(Context context, String name, Object value) {
        if (value == null) {
            return null;
        }
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
    public static Object restoreValueState(Context context, String name, Object state) {
        if (state == null) {
            return null;
        }
        Object value = restoreAttachedState(context, state);
        return value;
    }

    /**
     *
     * @param context
     * @param ctx
     * @return
     */
    public static Object saveContext(Context context, Context ctx) {
        Object state = null;
        if (ctx != null) {
            if (ctx instanceof StateHolder) {
                state = ((StateHolder) ctx).saveState(context);
            } else {
                state = saveAttachedState(context, ctx.getVars());
            }
        }
        return state;
    }

    /**
     *
     * @param context
     * @param ctx
     * @param state
     */
    public static void restoreContext(Context context, Context ctx, Object state) {
        if (ctx != null) {
            if (ctx instanceof StateHolder) {
                ((StateHolder) ctx).restoreState(context, state);
            } else {
                Map vars = (Map) restoreAttachedState(context, state);
                ctx.getVars().putAll(vars);
            }
        }
    }

    /**
     *
     * @param context
     * @param chart
     * @param id
     * @return
     */
    public static Object findElement(Context context, SCXML chart, String id) {
        Object found = chart.findElement(id);
        if (found == null) {
            throw new IllegalStateException(String.format("Restored element %s not found.", id));
        }
        return found;
    }

}
