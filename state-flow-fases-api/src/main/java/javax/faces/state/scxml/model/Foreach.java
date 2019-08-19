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
package javax.faces.state.scxml.model;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.utils.IndexedValueExpression;
import javax.faces.state.utils.IteratedValueExpression;
import javax.faces.state.utils.MappedValueExpression;
import javax.faces.view.facelets.TagAttributeException;

/**
 * The class in this SCXML object model that corresponds to the &lt;foreach&gt;
 * SCXML element, which allows an SCXML application to iterate through a
 * collection in the data model and to execute the actions contained within it
 * for each item in the collection.
 */
public class Foreach extends Action implements ActionsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    private ValueExpression array;
    private ValueExpression item;
    private String index;

    /**
     * The set of executable elements (those that inheriting from Action) that
     * are contained in this &lt;if&gt; element.
     */
    private final List<Action> actions;

    /**
     *
     */
    public Foreach() {
        this.actions = new ArrayList<>();
    }

    @Override
    public final List<Action> getActions() {
        return actions;
    }

    @Override
    public final void addAction(final Action action) {
        if (action != null) {
            this.actions.add(action);
        }
    }

    /**
     *
     * @return
     */
    public ValueExpression getArray() {
        return array;
    }

    /**
     *
     * @param array
     */
    public void setArray(final ValueExpression array) {
        this.array = array;
    }

    /**
     *
     * @return
     */
    public ValueExpression getItem() {
        return item;
    }

    /**
     *
     * @param item
     */
    public void setItem(final ValueExpression item) {
        this.item = item;
    }

    /**
     *
     * @return
     */
    public String getIndex() {
        return index;
    }

    /**
     *
     * @param index
     */
    public void setIndex(final String index) {
        this.index = index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = exctx.getContext(getParentEnterableState());
        Evaluator eval = exctx.getEvaluator();

        Object src = eval.eval(ctx, array);

        if (src != null) {
            Iterator itr = this.toIterator(src);
            if (itr != null) {
                int i = 0;

                String v = null;
                String vs = null;

                if (item != null) {
                    v = (String) eval.eval(ctx, item);
                }

                if (index != null) {
                    v = index;
                }

                @SuppressWarnings("UnusedAssignment")
                ValueExpression ve = null;
                ValueExpression vO = null;
                ValueExpression vsO = null;

                if (v != null) {
                    vO = eval.setVariable(ctx, v, null);
                }
                if (vs != null) {
                    vsO = eval.setVariable(ctx, vs, null);
                }

                int mi = 0;
                @SuppressWarnings("UnusedAssignment")
                Object value = null;
                int count = 0;
                try {
                    boolean first = true;
                    while (itr.hasNext()) {
                        count++;
                        value = itr.next();

                        // set the var
                        if (v != null) {
                            if (array == null) {
                                ctx.setLocal(v, value);
                            } else {
                                ve = this.getVarExpr(array, src, value, i, 0);
                                eval.setVariable(ctx, v, ve);
                            }
                        }

                        if (index != null) {
                            ctx.setLocal(index, i);
                        }
                        // The "foreach" statement is a "container"
                        for (Action aa : actions) {
                            aa.execute(exctx);
                        }

                        i++;

                        first = false;
                    }
                } finally {
                    if (v != null) {
                        eval.setVariable(ctx, v, vO);
                    }
                    if (vs != null) {
                        eval.setVariable(ctx, vs, vsO);
                    }
                }

            } else {
                throw new ActionExecutionError("<foreach> in state " + getParentEnterableState().getId() + ": invalid iterable value '" + array + "'");
            }
        } else {
            throw new ActionExecutionError("<foreach> in state " + getParentEnterableState().getId() + ": invalid array value '" + array + "'");
        }
    }

    private ValueExpression getVarExpr(ValueExpression ve, Object src,
            Object value, int i, int start) {
        if (src instanceof List || src.getClass().isArray()) {
            return new IndexedValueExpression(ve, i);
        } else if (src instanceof Map && value instanceof Map.Entry) {
            return new MappedValueExpression(ve, (Map.Entry) value);
        } else if (src instanceof Collection) {
            return new IteratedValueExpression(ve, start, i);
        }
        throw new IllegalStateException("Cannot create VE for: " + src);
    }

    private Iterator toIterator(Object src) {
        if (src == null) {
            return null;
        } else if (src instanceof Collection) {
            return ((Collection) src).iterator();
        } else if (src instanceof Map) {
            return ((Map) src).entrySet().iterator();
        } else if (src.getClass().isArray()) {
            return new ArrayIterator(src);
        } else {
            throw new ActionExecutionError("Must evaluate to a Collection, Map, Array, or null.");
        }
    }

    private static class ArrayIterator implements Iterator {

        protected final Object array;

        protected int i;

        protected final int len;

        public ArrayIterator(Object src) {
            this.i = 0;
            this.array = src;
            this.len = Array.getLength(src);
        }

        @Override
        public boolean hasNext() {
            return this.i < this.len;
        }

        @Override
        public Object next() {
            try {
                return Array.get(this.array, this.i++);
            } catch (ArrayIndexOutOfBoundsException ioob) {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
