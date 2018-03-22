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
package org.apache.faces.impl.state.el;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import javax.el.FunctionMapper;
import org.apache.faces.impl.state.log.FlowLogger;

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
//            switch (localName) {
//                case "In": {
//                    Class[] attrs = new Class[]{String.class};
//                    try {
//                        return Builtin.class.getMethod("isMember", attrs);
//                    } catch (SecurityException | NoSuchMethodException e) {
//                        log.log(Level.SEVERE, "resolving isMember(Set, String)", e);
//                    }
//                }
//                case "Data": {
//                    // rvalue in expressions, coerce to String
//                    Class[] attrs =
//                            new Class[]{Map.class, Object.class, String.class};
//                    try {
//                        return Builtin.class.getMethod("data", attrs);
//                    } catch (SecurityException | NoSuchMethodException e) {
//                        log.log(Level.SEVERE, "resolving data(Node, String)", e);
//                    }
//                }
//                case "LData": {
//                    // lvalue in expressions, retain as Node
//                    Class[] attrs =
//                            new Class[]{Map.class, Object.class, String.class};
//                    try {
//                        return Builtin.class.getMethod("dataNode", attrs);
//                    } catch (SecurityException | NoSuchMethodException e) {
//                        log.log(Level.SEVERE, "resolving data(Node, String)", e);
//                    }
//                }
//                default:
//                    break;
//            }
            return null;
        }
    }