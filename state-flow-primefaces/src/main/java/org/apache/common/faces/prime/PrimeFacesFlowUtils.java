/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.prime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.Application;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.view.ViewMetadata;
import org.primefaces.util.ResourceUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class PrimeFacesFlowUtils {

    private final static Logger logger = Logger.getLogger(PrimeFacesFlowUtils.class.getName());

    /**
     *
     * @param expression
     * @return
     */
    public static boolean isMixedExpression(String expression) {

        if (null == expression) {
            return false;
        }

        // if it doesn't start and end with delimiters
        return (!(expression.startsWith("#{") && expression.endsWith("}")))
                && isExpression(expression);

    }

    /**
     *
     * @param expression
     * @return
     */
    public static boolean isExpression(String expression) {

        if (null == expression) {
            return false;
        }

        //check to see if attribute has an expression
        int start = expression.indexOf("#{");
        return start != -1 && expression.indexOf('}', start + 2) != -1;
    }

    /**
     *
     * @param context
     * @param map
     * @return
     */
    public static Map<String, List<String>> evaluateExpressions(FacesContext context, Map<String, List<String>> map) {
        if (map != null && !map.isEmpty()) {
            Map<String, List<String>> ret = new HashMap<>(map.size());
            for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                ret.put(entry.getKey(), evaluateExpressions(context, entry.getValue()));
            }

            return ret;
        }

        return map;
    }

    /**
     *
     * @param context
     * @param values
     * @return
     */
    public static List<String> evaluateExpressions(FacesContext context, List<String> values) {
        if (!values.isEmpty()) {
            List<String> ret = new ArrayList<>(values.size());
            Application app = context.getApplication();
            for (String val : values) {
                if (val != null) {
                    String value = val.trim();
                    if (isExpression(value)) {
                        value = app.evaluateExpressionGet(context, value, String.class);
                    }
                    ret.add(value);
                }
            }

            return ret;
        }
        return values;
    }

    /**
     *
     * @param context
     * @param isRedirect
     */
    public static void doLastPhaseActions(FacesContext context, boolean isRedirect) {
        try {
            Flash flash = context.getExternalContext().getFlash();
            Method getFlashMethod = flash.getClass().getMethod("doLastPhaseActions", FacesContext.class, Boolean.TYPE);
            getFlashMethod.invoke(flash, context, isRedirect);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            logger.log(Level.SEVERE, "Phase actions", e);
        }
    }

    /**
     *
     * @param context
     * @param view
     * @param instance
     * @param target
     */
    public static void loadResorces(FacesContext context, UIViewRoot view, Object instance, String target) {
        ResourceDependencies aresorces = instance.getClass().getAnnotation(ResourceDependencies.class);

        for (ResourceDependency resource : aresorces.value()) {
            String name = resource.name();
            ResourceUtils.addComponentResource(context, name, resource.library(), target);
        }
    }

    /**
     *
     * @param context
     * @param viewRoot
     * @param params
     */
    public static void applyParams(FacesContext context, UIViewRoot viewRoot, Map<String, Object> params) {
        if (viewRoot != null) {
            if (ViewMetadata.hasMetadata(viewRoot)) {
                VisitContext vc = VisitContext.createVisitContext(context);
                viewRoot.visitTree(vc, (VisitContext ivc, UIComponent target) -> {

                    if (target instanceof UIViewParameter) {
                        UIViewParameter parametr = (UIViewParameter) target;
                        String name = parametr.getName();
                        if (params.containsKey(name)) {
                            parametr.setValue(params.get(name));
                        }
                    }
                    return VisitResult.ACCEPT;
                });
            }
        }

    }

}
