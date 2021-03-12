/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.utils;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class MappedValueExpression extends ValueExpression {

    private final static class Entry implements Map.Entry, Serializable {

        private static final long serialVersionUID = 4361498560718735987L;
        private final Map src;
        private final Object key;

        public Entry(Map src, Object key) {
            this.src = src;
            this.key = key;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return src.get(key);
        }

        @Override
        public Object setValue(Object value) {
            return src.put(key, value);
        }

    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final Object key;

    private final ValueExpression orig;

    /**
     * 
     * @param orig
     * @param entry
     */
    public MappedValueExpression(ValueExpression orig, Map.Entry entry) {
        this.orig = orig;
        this.key = entry.getKey();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#getValue(javax.el.ELContext)
     */
    @Override
    public Object getValue(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(true);
            return new Entry((Map) base, key);
            
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#setValue(javax.el.ELContext,
     *      java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object value) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            context.getELResolver().setValue(context, base, key, value);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#isReadOnly(javax.el.ELContext)
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            return context.getELResolver().isReadOnly(context, base, key);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#getType(javax.el.ELContext)
     */
    @Override
    public Class getType(ELContext context) {
        Object base = this.orig.getValue(context);
        if (base != null) {
            context.setPropertyResolved(false);
            return context.getELResolver().getType(context, base, key);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.ValueExpression#getExpectedType()
     */
    @Override
    public Class getExpectedType() {
        return Object.class;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.Expression#getExpressionString()
     */
    @Override
    public String getExpressionString() {
        return this.orig.getExpressionString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.Expression#equals(java.lang.Object)
     */
    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object obj) {
        return this.orig.equals(obj);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.el.Expression#hashCode()
     */
    @Override
    public int hashCode() {
        return 0;
    }

    /*
     * (non-Javadoc)eturn new Map.Entry<K, V>
     * 
     * @see javax.el.Expression#isLiteralText()
     */
    @Override
    public boolean isLiteralText() {
        return false;
    }

}
