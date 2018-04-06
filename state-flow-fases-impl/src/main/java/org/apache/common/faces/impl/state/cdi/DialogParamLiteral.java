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
package org.apache.common.faces.impl.state.cdi;

import javax.enterprise.util.AnnotationLiteral;
import org.apache.common.faces.state.annotation.DialogParam;

/**
 *
 * @author Waldemar Kłaczyński
 */
@SuppressWarnings("AnnotationAsSuperInterface")
public class DialogParamLiteral extends AnnotationLiteral<DialogParam> implements DialogParam {

    private final String value;

    public DialogParamLiteral() {
        this("");
    }

    public DialogParamLiteral(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
    
     public static final DialogParamLiteral INSTANCE = new DialogParamLiteral();
    
}
