/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.demo.basic;

import java.io.Serializable;
import javax.faces.state.annotation.DialogScoped;
import javax.inject.Named;

/**
 *
 * @author Waldemar Kłaczyński
 */
@DialogScoped
@Named("main")
public class MainBean implements Serializable {
    
    public boolean prepare() {
        return true;
    }
    
}
