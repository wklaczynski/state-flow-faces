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
package org.ssoft.faces.impl.state.evaluator;

import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.EvaluatorProvider;
import javax.faces.state.scxml.model.SCXML;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.SCXML_DATA_MODEL;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author Waldemar Kłaczyński
 */
@MetaInfServices(EvaluatorProvider.class)
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
