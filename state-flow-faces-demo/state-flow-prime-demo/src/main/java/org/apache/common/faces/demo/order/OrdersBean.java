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
package org.apache.common.faces.demo.order;

import java.io.Serializable;
import javax.inject.Named;
import org.apache.common.faces.state.annotation.StateDialogScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateDialogScoped
@Named("orders")
public class OrdersBean implements Serializable {
    
    public boolean prepare() {
        return true;
    }
    
    
}
