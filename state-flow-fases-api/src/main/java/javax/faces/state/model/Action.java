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
package javax.faces.state.model;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.faces.state.NamespacePrefixesHolder;
import javax.faces.state.ModelException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.enterprise.context.spi.Context;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import static javax.faces.component.UIComponentBase.restoreAttachedState;
import static javax.faces.component.UIComponentBase.saveAttachedState;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import static javax.faces.state.FlowInstance.FLOW_EL_CONTEXT_KEY;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class Action implements NamespacePrefixesHolder {

    protected static final Logger log = Logger.getLogger("javax.faces.state");

    /**
     * Current document namespaces are saved under this key in the parent
     * state's context.
     */
    private static final String NAMESPACES_KEY = "_ALL_NAMESPACES";

    /**
     * Link to its parent or container.
     */
    private Executable parent;

    /**
     * The current XML namespaces in the flow chart document for this action
     * node, preserved for deferred XPath evaluation.
     */
    private Map namespaces;

    /**
     * <p>
     * The <code>Map</code> containing our bindings, keyed by attribute
     * name.</p>
     */
    private Map<String, ValueExpression> bindings;

    /**
     * <p>
     * The <code>Map</code> containing our attributesMap, keyed by attribute
     * name.</p>
     */
    private final Map<String, Object> attributesMap;

    /**
     * <p>
     * The <code>Map</code> containing our attributes, keyed by attribute
     * name.</p>
     */
    private AttributesMap attributes = null;

    /**
     * <p>
     * Each entry is an map of <code>PropertyDescriptor</code>s describing the
     * properties of a concrete {@link UIComponent} implementation, keyed by the
     * corresponding <code>java.lang.Class</code>.</p>
     * <p/>
     */
    private Map<Class<?>, Map<String, PropertyDescriptor>> descriptors;

    /**
     * Reference to the map of <code>PropertyDescriptor</code>s for this class
     * in the <code>descriptors<code> <code>Map<code>.
     */
    private Map<String, PropertyDescriptor> pdMap = null;

    /**
     * Constructor.
     */
    public Action() {
        super();
        this.parent = null;
        this.namespaces = null;
        this.attributesMap = new HashMap<>();
        populateDescriptorsMapIfNecessary();
    }

    private void populateDescriptorsMapIfNecessary() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Class<?> clazz = getClass();

        /*
         * If we can find a valid FacesContext we are going to use it to get
         * access to the property descriptor map.
         */
        if (facesContext != null
                && facesContext.getExternalContext() != null
                && facesContext.getExternalContext().getApplicationMap() != null) {

            Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();

            if (!applicationMap.containsKey("com.sun.flow.model.COMPONENT_DESCRIPTORS_MAP")) {
                applicationMap.put("com.sun.flow.model.COMPONENT_DESCRIPTORS_MAP", new ConcurrentHashMap<>());
            }

            descriptors = (Map<Class<?>, Map<String, PropertyDescriptor>>) applicationMap.get("com.sun.flow.model.COMPONENT_DESCRIPTORS_MAP");
            pdMap = descriptors.get(clazz);
        }

        if (pdMap == null) {
            /*
             * We did not find the property descriptor map so we are now 
             * going to load it.
             */
            PropertyDescriptor pd[] = getPropertyDescriptors();
            if (pd != null) {
                pdMap = new HashMap<>(pd.length, 1.0f);
                for (PropertyDescriptor aPd : pd) {
                    pdMap.put(aPd.getName(), aPd);
                }

                if (descriptors != null && !descriptors.containsKey(clazz)) {
                    descriptors.put(clazz, pdMap);
                }
            }
        }
    }

    /**
     * <p>
     * Return an array of <code>PropertyDescriptors</code> for this
     * {@link Action}'s implementation class. If no descriptors can be
     * identified, a zero-length array will be returned.</p>
     *
     * @throws FacesException if an introspection exception occurs
     */
    private PropertyDescriptor[] getPropertyDescriptors() {
        PropertyDescriptor[] pd;
        try {
            pd = Introspector.getBeanInfo(this.getClass()).getPropertyDescriptors();
        } catch (IntrospectionException e) {
            throw new FacesException(e);
        }
        return (pd);
    }

    /**
     * Get the Executable parent.
     *
     * @return Returns the parent.
     */
    public final Executable getParent() {
        return parent;
    }

    /**
     * Set the Executable parent.
     *
     * @param parent The parent to set.
     */
    public final void setParent(final Executable parent) {
        this.parent = parent;
    }

    /**
     * Get the XML namespaces at this action node in the SCXML document.
     *
     * @return Returns the map of namespaces.
     */
    @Override
    public final Map getNamespaces() {
        return namespaces;
    }

    /**
     * Set the XML namespaces at this action node in the SCXML document.
     *
     * @param namespaces The document namespaces.
     */
    @Override
    public final void setNamespaces(final Map namespaces) {
        this.namespaces = namespaces;
    }

    public Map<String, Object> getAttributes() {

        if (attributes == null) {
            attributes = new AttributesMap(this);
        }
        return (attributes);

    }

    public Object getAttribute(String name) {
        return getAttributes().get(name);
    }

    public void setAttribute(String name, Object value) {
        getAttributes().put(name, value);
    }

    /**
     * Return the {@link TransitionTarget} whose {@link Context} this action
     * executes in.
     *
     * @return The parent {@link TransitionTarget}
     * @throws ModelException For an unknown TransitionTarget subclass
     */
    public final TransitionTarget getParentTransitionTarget() throws ModelException {
        TransitionTarget tt = parent.getParent();
        if (tt instanceof State || tt instanceof Parallel) {
            return tt;
        } else if (tt instanceof History || tt instanceof Initial) {
            return tt.getParent();
        } else {
            throw new ModelException("Unknown TransitionTarget subclass:"
                    + tt.getClass().getName());
        }
    }

    /**
     * <p>
     * Return the {@link ValueExpression} used to calculate the value for the
     * specified attribute or property name, if any.</p>
     *
     * @param name Name of the attribute or property for which to retrieve a
     * {@link ValueExpression}
     * @return the value expression, or <code>null</code>.
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     *
     */
    public ValueExpression getValueExpression(String name) {

        if (name == null) {
            throw new NullPointerException();
        }

        Map<String, ValueExpression> map = bindings;
        return ((map != null) ? map.get(name) : null);
    }

    /**
     * <p>
     * Set the {@link ValueExpression} used to calculate the value for the
     * specified attribute or property name, if any.</p>
     *
     * <p>
     * The implementation must call {@link
     * ValueExpression#isLiteralText} on the argument <code>expression</code>.
     * If <code>isLiteralText()</code> returns <code>true</code>, invoke
     * {@link ValueExpression#getValue} on the argument expression and pass the
     * result as the <code>value</code> parameter in a call to <code>this.{@link
     * #getAttributes()}.put(name, value)</code> where <code>name</code> is the
     * argument <code>name</code>. If an exception is thrown as a result of
     * calling {@link ValueExpression#getValue}, wrap it in a
     * {@link javax.faces.FacesException} and re-throw it. If
     * <code>isLiteralText()</code> returns <code>false</code>, simply store the
     * un-evaluated <code>expression</code> argument in the collection of
     * <code>ValueExpression</code>s under the key given by the argument
     * <code>name</code>.</p>
     *
     * @param name Name of the attribute or property for which to set a
     * {@link ValueExpression}
     * @param binding The {@link ValueExpression} to set, or <code>null</code>
     * to remove any currently set {@link ValueExpression}
     *
     * @throws IllegalArgumentException if <code>name</code> is one of
     * <code>id</code> or <code>parent</code>
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     *
     */
    public void setValueExpression(String name, ValueExpression binding) {

        if (name == null) {
            throw new NullPointerException();
        } else if ("namespaces".equals(name) || "parent".equals(name)) {
            throw new IllegalArgumentException();
        }

        if (binding != null) {
            if (!binding.isLiteralText()) {
                if (bindings == null) {
                    //noinspection CollectionWithoutInitialCapacity
                    bindings = new HashMap<>();
                }
                bindings.put(name, binding);
            } else {
                ELContext context = FacesContext.getCurrentInstance().getELContext();
                try {
                    getAttributes().put(name, binding.getValue(context));
                } catch (ELException ele) {
                    throw new FacesException(ele);
                }
            }
        } else {
            if (bindings != null) {
                bindings.remove(name);
                if (bindings.isEmpty()) {
                    bindings = null;
                }
            }
        }

    }

    /**
     * Execute this action instance.
     *
     * @param dispatcher The EventDispatcher for this execution instance
     * @param err The ErrorReporter to broadcast any errors during execution.
     * @param instance The state machine execution instance information.
     * @param events The collection to which any internal events arising from
     * the execution of this action must be added.
     *
     * @throws ModelException If the execution causes the model to enter a
     * non-deterministic state.
     * @throws FlowExpressionException If the execution involves trying to
     * evaluate an expression which is malformed.
     */
    public abstract void execute(final FlowEventDispatcher dispatcher,
            final FlowErrorReporter err, final FlowInstance instance,
            final Collection<FlowTriggerEvent> events)
            throws ModelException, FlowExpressionException;

    /**
     * Return the key under which the current document namespaces are saved in
     * the parent state's context.
     *
     * @return The namespaces key
     */
    protected static String getNamespacesKey() {
        return NAMESPACES_KEY;
    }

    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object values[] = new Object[1];

        values[0] = saveAtributesState(context);

        return values;
    }

    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        Object[] values = (Object[]) state;

        restoreAtributesState(context, values[0]);
    }

    private Object saveAtributesState(FacesContext context) {
        Object state = null;
        if (null != attributesMap && attributesMap.size() > 0) {
            Object[] attached = new Object[attributesMap.size()];
            int i = 0;
            for (Map.Entry<String, Object> entry : attributesMap.entrySet()) {
                Object values[] = new Object[2];
                values[0] = entry.getKey();
                values[1] = saveValueState(context, entry.getKey(), entry.getValue());
                attached[i++] = values;
            }
            state = attached;
        }
        return state;
    }

    private void restoreAtributesState(FacesContext context, Object state) {
        attributesMap.clear();

        if (null != state) {
            Object[] values = (Object[]) state;
            for (Object value : values) {
                Object[] entry = (Object[]) value;

                String key = (String) entry[0];
                Object rvalue = restoreValueState(context, key, entry[1]);
                attributesMap.put(key, rvalue);
            }
        }
    }

    public static Object saveValueState(FacesContext context, String name, Object value) {
        if (value == null) {
            return null;
        }
        value = saveAttachedState(context, value);
        return value;
    }

    public static Object restoreValueState(FacesContext context, String name, Object state) {
        if (state == null) {
            return null;
        }
        Object value = restoreAttachedState(context, state);
        return value;
    }

    private static final Object EMPTY_OBJECT_ARRAY[] = new Object[0];

    private ELContext getELContext() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        return (ELContext) facesContext.getAttributes().get(FLOW_EL_CONTEXT_KEY);
    }

    Map<String, PropertyDescriptor> getDescriptorMap() {
        return pdMap;
    }

    private static class AttributesMap implements Map<String, Object>, Serializable {

        //private Map<String, Object> attributes;
        private final transient Map<String, PropertyDescriptor> pdMap;
        private transient ConcurrentMap<String, Method> readMap;
        private Action action;

        // -------------------------------------------------------- Constructors
        private AttributesMap(Action action) {
            this.action = action;
            this.pdMap = action.getDescriptorMap();
        }

        @Override
        public boolean containsKey(Object keyObj) {
            String key = (String) keyObj;
            PropertyDescriptor pd = getPropertyDescriptor(key);
            if (pd == null) {
                Map<String, Object> attributes = action.attributesMap;
                if (attributes != null) {
                    return attributes.containsKey(key);
                } else {
                    return (false);
                }
            } else {
                return (false);
            }
        }

        @Override
        public Object get(Object keyObj) {
            String key = (String) keyObj;
            Object result = null;
            if (key == null) {
                throw new NullPointerException();
            }
            Map<String, Object> attributes = action.attributesMap;
            if (null == result) {
                PropertyDescriptor pd = getPropertyDescriptor(key);
                if (pd != null) {
                    try {
                        if (null == readMap) {
                            readMap = new ConcurrentHashMap<>();
                        }
                        Method readMethod = readMap.get(key);
                        if (null == readMethod) {
                            readMethod = pd.getReadMethod();
                            Method putResult = readMap.putIfAbsent(key, readMethod);
                            if (null != putResult) {
                                readMethod = putResult;
                            }
                        }

                        if (readMethod != null) {
                            result = (readMethod.invoke(action, EMPTY_OBJECT_ARRAY));
                        } else {
                            throw new IllegalArgumentException(key);
                        }
                    } catch (IllegalAccessException e) {
                        throw new FacesException(e);
                    } catch (InvocationTargetException e) {
                        throw new FacesException(e.getTargetException());
                    }
                } else if (attributes != null) {
                    if (attributes.containsKey(key)) {
                        result = attributes.get(key);
                    }
                }
            }
            if (null == result) {
                ValueExpression ve = action.getValueExpression(key);
                if (ve != null) {
                    try {
                        result = ve.getValue(action.getELContext());
                    } catch (ELException e) {
                        throw new FacesException(e);
                    }
                }
            }

            return result;
        }

        @Override
        public Object put(String keyValue, Object value) {
            if (keyValue == null) {
                throw new NullPointerException();
            }

            PropertyDescriptor pd = getPropertyDescriptor(keyValue);
            if (pd != null) {
                try {
                    Object result = null;
                    Method readMethod = pd.getReadMethod();
                    if (readMethod != null) {
                        result = readMethod.invoke(action, EMPTY_OBJECT_ARRAY);
                    }
                    Method writeMethod = pd.getWriteMethod();
                    if (writeMethod != null) {
                        writeMethod.invoke(action, value);
                    } else {
                        throw new IllegalArgumentException("Setter not found for property " + keyValue);
                    }
                    return (result);
                } catch (IllegalAccessException e) {
                    throw new FacesException(e);
                } catch (InvocationTargetException e) {
                    throw new FacesException(e.getTargetException());
                }
            } else {
                if (value == null) {
                    throw new NullPointerException();
                }
                return putAttribute(keyValue, value);
            }
        }

        @Override
        public void putAll(Map<? extends String, ?> map) {
            if (map == null) {
                throw new NullPointerException();
            }

            for (Map.Entry<? extends String, ?> entry : map.entrySet()) {
                this.put(entry.getKey(), entry.getValue());
            }
        }

        @Override
        public Object remove(Object keyObj) {
            String key = (String) keyObj;
            if (key == null) {
                throw new NullPointerException();
            }
            PropertyDescriptor pd = getPropertyDescriptor(key);
            if (pd != null) {
                throw new IllegalArgumentException(key);
            } else {
                Map<String, Object> attributes = getAttributes();
                if (attributes != null) {
                    return action.attributesMap.remove(keyObj);
                } else {
                    return null;
                }
            }
        }

        @Override
        public int size() {
            Map attributes = getAttributes();
            return (attributes != null ? attributes.size() : 0);
        }

        @Override
        public boolean isEmpty() {
            Map attributes = getAttributes();
            return (attributes == null || attributes.isEmpty());
        }

        @Override
        public boolean containsValue(java.lang.Object value) {
            Map attributes = getAttributes();
            return (attributes != null && attributes.containsValue(value));
        }

        @Override
        public void clear() {
            action.attributesMap.clear();
        }

        @Override
        public Set<String> keySet() {
            Map<String, Object> attributes = getAttributes();
            if (attributes != null) {
                return Collections.unmodifiableSet(attributes.keySet());
            }
            return Collections.emptySet();
        }

        @Override
        public Collection<Object> values() {
            Map<String, Object> attributes = getAttributes();
            if (attributes != null) {
                return Collections.unmodifiableCollection(attributes.values());
            }
            return Collections.emptyList();
        }

        @Override
        public Set<Map.Entry<String, Object>> entrySet() {
            Map<String, Object> attributes = getAttributes();
            if (attributes != null) {
                return Collections.unmodifiableSet(attributes.entrySet());
            }
            return Collections.emptySet();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Map)) {
                return false;
            }
            Map t = (Map) o;
            if (t.size() != size()) {
                return false;
            }

            try {
                for (Object e : entrySet()) {
                    Map.Entry entry = (Map.Entry) e;
                    Object key = entry.getKey();
                    Object value = entry.getValue();
                    if (value == null) {
                        if (!(t.get(key) == null && t.containsKey(key))) {
                            return false;
                        }
                    } else {
                        if (!value.equals(t.get(key))) {
                            return false;
                        }
                    }
                }
            } catch (ClassCastException | NullPointerException unused) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int h = 0;
            for (Object o : entrySet()) {
                h += o.hashCode();
            }
            return h;
        }

        private Map<String, Object> getAttributes() {
            return action.attributesMap;
        }

        private Object putAttribute(String key, Object value) {
            return action.attributesMap.put(key, value);
        }

        /**
         * <p>
         * Return the <code>PropertyDescriptor</code> for the specified property
         * name for this {@link UIComponent}'s implementation class, if any;
         * otherwise, return <code>null</code>.</p>
         *
         * @param name Name of the property to return a descriptor for
         * @throws FacesException if an introspection exception occurs
         */
        PropertyDescriptor getPropertyDescriptor(String name) {
            if (pdMap != null) {
                return (pdMap.get(name));
            }
            return (null);
        }

        // ----------------------------------------------- Serialization Methods
        // This is dependent on serialization occuring with in a
        // a Faces request, however, since Action.{save,restore}State()
        // doesn't actually serialize the AttributesMap, these methods are here
        // purely to be good citizens.
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.writeObject(action.getClass());
            //noinspection NonSerializableObjectPassedToObjectStream
            out.writeObject(action.saveState(FacesContext.getCurrentInstance()));
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            //noinspection unchecked
            Class clazz = (Class) in.readObject();
            try {
                action = (Action) clazz.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            action.restoreState(FacesContext.getCurrentInstance(), in.readObject());
        }

    }

}
