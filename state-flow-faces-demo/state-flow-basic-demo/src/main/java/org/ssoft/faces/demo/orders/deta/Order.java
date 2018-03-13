/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.demo.orders.deta;

import java.io.Serializable;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Order implements Serializable {

    private final String id;
    private String name;
    private String description;

    public Order(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
