/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.impl;

import java.util.HashMap;
import java.util.Map;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DefaultVariableMapper extends VariableMapper {

    private Map vars;

    public DefaultVariableMapper() {
        super();
    }

    /**
     * @param name
     * @return 
     * @see javax.el.VariableMapper#resolveVariable(java.lang.String)
     */
    @Override
    public ValueExpression resolveVariable(String name) {
        if (this.vars != null) {
            return (ValueExpression) this.vars.get(name);
        }
        return null;
    }

    /**
     * @param name
     * @return 
     * @see javax.el.VariableMapper#setVariable(java.lang.String, javax.el.ValueExpression)
     */
    @Override
    public ValueExpression setVariable(String name, ValueExpression expression) {
        if (this.vars == null) {
            this.vars = new HashMap();
        }
        return (ValueExpression) this.vars.put(name, expression);
    }

}
