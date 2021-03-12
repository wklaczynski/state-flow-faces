/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.scxml.model;

import jakarta.el.ValueExpression;
import jakarta.faces.view.facelets.Tag;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.SCXMLExpressionException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SCComponent {

    protected Map<Serializable, ValueExpression> bindings;
    private final Map<Serializable, Object> valueMap = new HashMap<>();

    protected Tag tag;

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    public ValueExpression getValueExpression(String name) {
        if (name == null) {
            throw new NullPointerException();
        }

        return bindings != null ? bindings.get(name) : null;
    }

    public void setValueExpression(String name, ValueExpression binding) {

        if (name == null) {
            throw new NullPointerException();
        }

        if ("id".equals(name) || "parent".equals(name)) {
            throw new IllegalArgumentException();
        }

        if (binding != null) {
            if (bindings == null) {
                bindings = new HashMap<>();
            }
            bindings.put(name, binding);
        } else {
            if (bindings != null) {
                bindings.remove(name);
                if (bindings.isEmpty()) {
                    bindings = null;
                }
            }
        }
    }

    public Object getValue(String name) {
        return valueMap.get(name);
    }

    public void setValue(String name, Object value) {
        if (value != null) {
            valueMap.put(name, value);
        } else {
            valueMap.remove(name);
        }
    }

    public Object eval(Evaluator evaluator, Context ctx, String name, Object defaultValue) throws SCXMLExpressionException {
        Object retVal = getValue(name);
        if (retVal == null) {
            ValueExpression ve = getValueExpression(name);
            if (ve != null) {
                retVal = evaluator.eval(ctx, ve);
            }
        }
        return ((retVal != null) ? retVal : defaultValue);
    }

}
