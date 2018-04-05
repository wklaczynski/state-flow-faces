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
package org.apache.common.faces.impl.state.evaluator;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.el.FunctionMapper;
import org.apache.common.faces.impl.state.log.FlowLogger;
import org.apache.common.faces.impl.state.tag.FacesFlowBuiltin;
import org.apache.common.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class EvaluatorBuiltinFunctionMapper extends FunctionMapper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *
     */
    public static final Logger log = FlowLogger.APPLICATION.getLogger();
    private final ELContext context;
    private final SCXML scxml;

    /**
     *
     * @param context
     * @param scxml
     */
    public EvaluatorBuiltinFunctionMapper(ELContext context, SCXML scxml) {
        super();
        this.context = context;
        this.scxml = scxml;
    }

    @Override
    public Method resolveFunction(final String prefix, final String localName) {
        String namespace = scxml.getNamespaces().get(prefix);
        if(namespace != null && namespace.equals(scxml.getDatamodelName())) {
            switch (localName) {
                case "in": {
                    Class[] attrs = new Class[]{String.class};
                    try {
                        return FacesFlowBuiltin.class.getMethod("isMember", attrs);
                    } catch (SecurityException | NoSuchMethodException e) {
                        log.log(Level.SEVERE, "resolving isMember(String)", e);
                    }
                }
                default:
                    break;
            }
        }
        return null;
    }
}
