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
package org.apache.common.faces.demo;

import javax.enterprise.inject.Specializes;
import org.apache.deltaspike.jsf.api.config.JsfModuleConfig;
import org.apache.deltaspike.jsf.spi.scope.window.ClientWindowConfig;

/**
 *
 * @author Waldemar Kłaczyński
 */
@Specializes
public class MyJsfModuleConfig extends JsfModuleConfig {

    @Override
    public ClientWindowConfig.ClientWindowRenderMode getDefaultWindowMode() {
         return ClientWindowConfig.ClientWindowRenderMode.LAZY;
    }
    

}
