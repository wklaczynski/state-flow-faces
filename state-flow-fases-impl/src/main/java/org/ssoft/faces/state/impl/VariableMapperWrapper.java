/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.impl;

import java.util.HashMap;
import java.util.Map;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class VariableMapperWrapper extends VariableMapper {

    private final VariableMapper target;

    private Map vars;

    public VariableMapperWrapper(VariableMapper orig) {
        super();
        this.target = orig;
    }

    @Override
    public ValueExpression resolveVariable(String variable) {
        ValueExpression ve = null;
        try {
            if (this.vars != null) {
                ve = (ValueExpression) this.vars.get(variable);
            }
            if (ve == null) {
                return this.target.resolveVariable(variable);
            }
            return ve;
        } catch (StackOverflowError e) {
            throw new ELException("Could not Resolve Variable [Overflow]: " + variable, e);
        }
    }

    @Override
    public ValueExpression setVariable(String variable,
            ValueExpression expression) {
        if (this.vars == null) {
            this.vars = new HashMap();
        }
        return (ValueExpression) this.vars.put(variable, expression);
    }
}
