/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.scxml.env;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.scxml.Context;
import javax.scxml.Evaluator;
import javax.scxml.SCXMLExpressionException;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Base Evaluator providing common functionality for most Evaluator implementations
 */
public abstract class AbstractBaseEvaluator implements Evaluator, Serializable {

    /**
     * Unique context variable name used for temporary reference to assign data (thus must be a valid variable name)
     */
    private static final String ASSIGN_VARIABLE_NAME = "a"+ UUID.randomUUID().toString().replace('-','x');

    /**
     * @see Evaluator#evalAssign(Context, String, Object)
     */
    @Override
    public void evalAssign(final Context ctx, final String location, final Object data) throws SCXMLExpressionException {
        String sb = location + "=" + ASSIGN_VARIABLE_NAME;
        try {
            ctx.getVars().put(ASSIGN_VARIABLE_NAME, data);
            eval(ctx, sb);
        } catch (SCXMLExpressionException e) {
            if (e.getCause() != null && e.getCause() != null && e.getCause().getMessage() != null) {
                throw new SCXMLExpressionException("Error evaluating assign to location=\"" + location + "\": " + e.getCause().getMessage());
            }
            throw e;
        } finally {
            ctx.getVars().remove(ASSIGN_VARIABLE_NAME);
        }
    }

    @Override
    public Object cloneData(final Object data) {
        if (data != null) {
            if (data instanceof String || data instanceof Number || data instanceof Boolean) {
                return data;
            }
            if (data instanceof Node) {
                return ((Node)data).cloneNode(true);
            }
            else if (data instanceof NodeList) {
                NodeList nodeList = (NodeList)data;
                ArrayList<Node> list = new ArrayList<>();
                for (int i = 0, size = nodeList.getLength(); i < size; i++) {
                    list.add(nodeList.item(i).cloneNode(true));
                }
                return list;
            }
            else if (data instanceof List) {
                ArrayList<Object> list = new ArrayList<>();
                for (Object v : (List)data) {
                    list.add(cloneData(v));
                }
                return list;
            }
            else if (data instanceof Map) {
                Map<?,?> dataMap = (Map<?,?>)data;
                HashMap<Object, Object> map = new LinkedHashMap<>();
                for (Map.Entry<?,?> entry : dataMap.entrySet()) {
                    map.put(cloneData(entry.getKey()), cloneData(entry.getValue()));
                }
                return map;
            }
            else {
                return cloneUnknownDataType(data);
            }
        }
        return null;
    }

    /**
     * Returns cloned value of data of unknown type, to be overridden as desired by specialized Evaluators
     * @param data data object of unknown type (not of type String, Number, Boolean, Node, NodeList, List or Map)
     * @return toString() value of data of unknown type
     */
    protected Object cloneUnknownDataType(final Object data) {
        return data.toString();
    }
}
