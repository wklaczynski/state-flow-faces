/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.component;

import javax.faces.component.UIComponentBase;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class UIStateFlow extends UIComponentBase {

    public static final String COMPONENT_FAMILY = "javax.faces.StateFlow";

    @SuppressWarnings("OverridableMethodCallInConstructor")
    public UIStateFlow() {
        super();
        setTransient(true);
    }
    
    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }
    
}
