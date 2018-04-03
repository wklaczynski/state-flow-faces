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

import org.apache.common.scxml.Evaluator;
import org.apache.common.scxml.EvaluatorProvider;
import org.apache.common.scxml.model.SCXML;
import static org.apache.common.faces.impl.state.StateFlowImplConstants.SCXML_DATA_MODEL;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowEvaluatorProvider implements EvaluatorProvider {

    @Override
    public String getSupportedDatamodel() {
        return SCXML_DATA_MODEL;
    }

    @Override
    public Evaluator getEvaluator() {
        return new StateFlowEvaluator();
    }

    @Override
    public Evaluator getEvaluator(SCXML document) {
        return new StateFlowEvaluator();
    }

}