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
package org.apache.common.faces.state.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.enterprise.context.NormalScope;

/**
 *
 * @author Waldemar Kłaczyński
 */
@NormalScope
@Inherited
@Documented
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface StateTargetScoped {
    
    /**
     * State#getId of a defined scxml for this application.
     *
     * @return transition id or:
     * "@common" - when bean assigned to last target transition or top parallel for all active targets.
     * "@top" - when bean assigned to top composite transition or top parallel transition for all atomic transitions.
     * "@composite" - when bean assigned to last composite target or top parallel transition.
     */
    String value() default "@composite";

    
}
