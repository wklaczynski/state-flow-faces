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
package org.apache.common.faces.demo.basic;

import java.io.Serializable;
import javax.inject.Named;
import javax.faces.state.annotation.StateScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateScoped("show")
@Named("showState")
public class StateOnlyBean implements Serializable {
    
    private String assignedTest1 = "";
    
    public boolean prepare() {
        return true;
    }
    
    public String getBeanTitle() {
        return "Show State Bean";
    }

    public String getAssignedTest1() {
        return assignedTest1;
    }

    public void setAssignedTest1(String assignedTest1) {
        this.assignedTest1 = assignedTest1;
    }
    
    
}
