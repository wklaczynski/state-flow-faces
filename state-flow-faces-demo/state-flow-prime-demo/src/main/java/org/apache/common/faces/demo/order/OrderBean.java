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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import org.apache.common.faces.demo.order.data.Order;
import org.apache.common.faces.demo.order.data.OrderProduct;
import org.apache.common.faces.demo.order.data.Product;
import org.apache.common.faces.state.annotation.ChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@ChartScoped
@Named("order")
public class OrderBean implements Serializable {

    private Order data;
    
    private boolean insertable;
    private boolean removable;
    private boolean updatable;

    private OrderProduct selectedProduct;

    public boolean prepareInsert() {
        insertable = true;
        data = new Order();
        return true;
    }

    public boolean prepareEdit(Order data) {
        try {
            updatable = true;
            this.data = data.clone();
            return true;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(OrderBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean prepareRemove(Order data) {
        try {
            removable = true;
            this.data = data.clone();
            return true;
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(OrderBean.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public boolean prepareView(Order data) {
        this.data = data;
        return true;
    }
    
    
    public Order getData() {
        return data;
    }

    public List<OrderProduct> getProducts() {
        return data.getProducts();
    }

    public OrderProduct getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(OrderProduct selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public boolean isRemovableProduct() {
        return selectedProduct != null;
    }

    public void addProduct(OrderProduct product) {
        data.getProducts().add(product);
        calculate();
    }

    public void addProduct(Product product) {
        OrderProduct orderProduct = new OrderProduct();
        orderProduct.setProductId(product.getId());
        orderProduct.setName(product.getName());
        orderProduct.setDescription(product.getDescription());
        orderProduct.setCost(product.getCost());
        addProduct(orderProduct);
    }

    public void removeProduct(OrderProduct product) {
        data.getProducts().remove(product);
        calculate();
    }

    private void calculate() {
        Double sum = 0.0;
        for (OrderProduct product : data.getProducts()) {
            sum += product.getCost();
        }
        data.setCost(sum);
    }

    public boolean isRemovable() {
        return removable;
    }

    public void setRemovable(boolean removable) {
        this.removable = removable;
    }

    public boolean isUpdatable() {
        return updatable;
    }

    public void setUpdatable(boolean updatable) {
        this.updatable = updatable;
    }

    public boolean isInsertable() {
        return insertable;
    }

    public void setInsertable(boolean insertable) {
        this.insertable = insertable;
    }
    
}
