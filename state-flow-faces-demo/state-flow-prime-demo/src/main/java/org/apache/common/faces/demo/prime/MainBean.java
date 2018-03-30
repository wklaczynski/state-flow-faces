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
package org.apache.common.faces.demo.prime;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import static javax.faces.application.FacesMessage.SEVERITY_INFO;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import org.apache.common.faces.state.annotation.StateDialogScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateDialogScoped
@Named("main")
public class MainBean implements Serializable {
    
    private String assignedTest1 = "";
    private String assignedTest2 = "";
    private String description = "";
    
    public boolean prepare() {
        return true;
    }
    
    public String getBeanTitle() {
        return "Main Bean";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAssignedTest1() {
        return assignedTest1;
    }

    public void setAssignedTest1(String assignedTest1) {
        this.assignedTest1 = assignedTest1;
    }

    public String getAssignedTest2() {
        return assignedTest2;
    }

    public void setAssignedTest2(String assignedTest2) {
        this.assignedTest2 = assignedTest2;
    }
    
    public void testCall(String name, String value) {
        FacesContext fc = FacesContext.getCurrentInstance();

        FacesMessage facesMessage = new FacesMessage(String.format(
                "I'm called to testCall method in main bean, witch params name=%s value=%s.",
        name, value));
        facesMessage.setSeverity(SEVERITY_INFO);
        fc.addMessage(null, facesMessage);
        
        
    }
    
}
