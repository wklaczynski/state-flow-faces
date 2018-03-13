/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.el;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.FunctionMapper;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class BuiltinFunctionMapper extends FunctionMapper implements Serializable {

        private static final long serialVersionUID = 1L;
        public static final Logger log = FlowLogger.FLOW.getLogger();

        public BuiltinFunctionMapper() {
            super();
        }

        @Override
        public Method resolveFunction(final String prefix, final String localName) {
            switch (localName) {
                case "In": {
                    Class[] attrs = new Class[]{Set.class, String.class};
                    try {
                        return Builtin.class.getMethod("isMember", attrs);
                    } catch (SecurityException | NoSuchMethodException e) {
                        log.log(Level.SEVERE, "resolving isMember(Set, String)", e);
                    }
                }
                case "Data": {
                    // rvalue in expressions, coerce to String
                    Class[] attrs =
                            new Class[]{Map.class, Object.class, String.class};
                    try {
                        return Builtin.class.getMethod("data", attrs);
                    } catch (SecurityException | NoSuchMethodException e) {
                        log.log(Level.SEVERE, "resolving data(Node, String)", e);
                    }
                }
                case "LData": {
                    // lvalue in expressions, retain as Node
                    Class[] attrs =
                            new Class[]{Map.class, Object.class, String.class};
                    try {
                        return Builtin.class.getMethod("dataNode", attrs);
                    } catch (SecurityException | NoSuchMethodException e) {
                        log.log(Level.SEVERE, "resolving data(Node, String)", e);
                    }
                }
                default:
                    break;
            }
            return null;
        }
    }