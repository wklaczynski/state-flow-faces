/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.demo.orders;

import java.io.Serializable;
import javax.inject.Named;
import org.ssoft.faces.demo.orders.deta.Order;
import javax.faces.state.annotation.StateChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateChartScoped
@Named("orders")
public class OrderBean implements Serializable {
    
    private Order selected;
    
    public boolean prepare() {
        return true;
    }

    public Order getSelected() {
        return selected;
    }

    public void setSelected(Order selected) {
        this.selected = selected;
    }
    
    public void dispatchSend() {
        
    }
    
}
