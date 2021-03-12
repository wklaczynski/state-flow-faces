/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.utils;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotWritableException;
import jakarta.el.ValueExpression;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class IteratedValueExpression extends ValueExpression {

    private static final long serialVersionUID = 1L;

    private final ValueExpression orig;

    private final int start;
    private final int index;

    public IteratedValueExpression(ValueExpression orig, int start, int index) {
        this.orig = orig;
        this.start = start;
        this.index = index;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#getValue(javax.el.ELContext)
     */
    @Override
    public Object getValue(ELContext context) {
        Collection collection = (Collection) orig.getValue(context);
        Iterator iterator = collection.iterator();
        Object result = null;
        int i = start;
        if (i != 0) {
            while(i != 0) {
                result = iterator.next();
                if (!iterator.hasNext()) {
                    throw new ELException("Unable to position start");
                }
                i--;
            }
        } else {
            result = iterator.next();
        }
        while(i < index) {
            if (!iterator.hasNext()) {
                throw new ELException("Unable to get given value");
            }
            i++;
            result = iterator.next();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#setValue(javax.el.ELContext,
     *      java.lang.Object)
     */
    @Override
    public void setValue(ELContext context, Object value) {
        context.setPropertyResolved(false);
        throw new PropertyNotWritableException();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#isReadOnly(javax.el.ELContext)
     */
    @Override
    public boolean isReadOnly(ELContext context) {
        context.setPropertyResolved(false);
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.ValueExpression#getType(javax.el.ELContext)
     */
    @Override
    public Class getType(ELContext context) {
        context.setPropertyResolved(false);
        return Object.class;
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
        return this.orig.hashCode();
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.el.Expression#isLiteralText()
     */
    @Override
    public boolean isLiteralText() {
        return false;
    }

}
