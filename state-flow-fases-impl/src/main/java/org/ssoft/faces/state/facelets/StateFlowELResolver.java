/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.facelets;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.PropertyNotFoundException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.ssoft.faces.state.invokers.ViewParamsContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowELResolver extends ELResolver implements Serializable {

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        Object result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ViewParamsContext ctx = getViewParamsContext(fc);
            if (ctx.containsKey(property.toString())) {
                context.setPropertyResolved(true);
                result = ctx.get(property.toString());
            }
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        Class result = null;
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ViewParamsContext ctx = getViewParamsContext(fc);
            if (ctx.containsKey(property.toString())) {
                context.setPropertyResolved(true);
                result = ctx.get(property.toString()).getClass();
            }
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (null == property) {
            String message = " base " + base + " property " + property;
            throw new PropertyNotFoundException(message);
        }
        if (null == base) {
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ViewParamsContext ctx = getViewParamsContext(fc);
        if (ctx.containsKey(property.toString())) {
            context.setPropertyResolved(true);
            return true;
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }

    private ViewParamsContext getViewParamsContext(FacesContext fc) {
        ExternalContext ec = fc.getExternalContext();
        ViewParamsContext viewParamsContext = (ViewParamsContext) ec.getRequestMap().get(ViewParamsContext.class.getName());
        if (viewParamsContext == null) {
            viewParamsContext = new ViewParamsContext();
            ec.getRequestMap().put(ViewParamsContext.class.getName(), viewParamsContext);
        }
        return viewParamsContext;

    }

}
