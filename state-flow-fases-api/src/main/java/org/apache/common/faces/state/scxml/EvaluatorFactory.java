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
package org.apache.common.faces.state.scxml;

import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import static org.apache.common.faces.state.scxml.Evaluator.DEFAULT_DATA_MODEL;
import org.apache.common.faces.state.scxml.env.minimal.MinimalEvaluator;
import org.apache.common.faces.state.scxml.model.ModelException;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 * A static singleton factory for {@link EvaluatorProvider}s by supported SCXML datamodel type.
 * <p>
 *  The EvaluatorFactory is used to automatically create an {@link Evaluator} instance for an SCXML
 *  statemachine when none has been pre-defined and configured for the {@link SCXMLExecutor}.
 * </p>
 * <p>
 *  The builtin supported providers are:
 * </p>
 * <ul>
 *  <li>no or empty datamodel (default) or datamodel="jexl": {@link JexlEvaluator.JexlEvaluatorProvider}</li>
 *  <li>datamodel="ecmascript": {@link JSEvaluator.JSEvaluatorProvider}</li>
 *  <li>datamodel="groovy": {@link GroovyEvaluator.GroovyEvaluatorProvider}</li>
 *  <li>datamodel="null": {@link MinimalEvaluator.MinimalEvaluatorProvider}</li>
 * </ul>
 * <p>
 *  For adding additional or overriding the builtin Evaluator implementations use
 *  {@link #registerEvaluatorProvider(EvaluatorProvider)} or {@link #unregisterEvaluatorProvider(String)}.
 * </p>
 * <p>
 *  The default provider can be overridden using the {@link #setDefaultProvider(EvaluatorProvider)} which will
 *  register the provider under the {@link Evaluator#DEFAULT_DATA_MODEL} ("") value for the datamodel.<br>
 *  Note: this is <em>not</em> the same as datamodel="null"!
 * </p>
 */
public class EvaluatorFactory {

    private static final EvaluatorFactory INSTANCE = new EvaluatorFactory();

    private final Map<String, EvaluatorProvider> providers = new ConcurrentHashMap<>();

    private EvaluatorFactory() {
        providers.put(MinimalEvaluator.SUPPORTED_DATA_MODEL, new MinimalEvaluator.MinimalEvaluatorProvider());
        providers.put(DEFAULT_DATA_MODEL, providers.get(MinimalEvaluator.SUPPORTED_DATA_MODEL));

        ServiceLoader<EvaluatorProvider> loader = ServiceLoader.load(EvaluatorProvider.class);
        
        Iterator<EvaluatorProvider> iterator = loader.iterator();
        while (iterator.hasNext()) {
            EvaluatorProvider next = iterator.next();
            providers.put(next.getSupportedDatamodel(), next);
        }
        
    }

    /**
     * Returns a dedicated Evaluator instance for a specific SCXML document its documentmodel.
     * <p>If no SCXML document is provided a default Evaluator will be returned.</p>
     * @param document The document to return a dedicated Evaluator for. May be null to retrieve the default Evaluator.
     * @return a new and not sharable Evaluator instance for the provided document, or a default Evaluator otherwise
     * @throws ModelException If the SCXML document datamodel is not supported.
     */
    public static Evaluator getEvaluator(final SCXML document) throws ModelException {
        String datamodelName = document != null ? document.getDatamodelName() : null;
        @SuppressWarnings("element-type-mismatch")
        EvaluatorProvider provider = INSTANCE.providers.get(datamodelName == null ? DEFAULT_DATA_MODEL : datamodelName);
        if (provider == null) {
            throw new ModelException("Unsupported SCXML document datamodel \""+(datamodelName)+"\"");
        }
        return document != null ? provider.getEvaluator(document) : provider.getEvaluator();
    }
}
