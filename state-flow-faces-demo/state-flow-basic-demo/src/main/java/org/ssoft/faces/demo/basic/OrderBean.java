/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.demo.basic;

import javax.faces.state.annotation.DialogScoped;
import javax.inject.Named;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogScoped
@Named("orders")
public class OrderBean {
    
    public boolean prepare() {
        return true;
    }
    
    
}
