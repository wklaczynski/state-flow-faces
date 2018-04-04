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
import javax.inject.Named;
import org.apache.common.faces.demo.order.data.Order;
import org.apache.common.faces.demo.order.data.Product;
import org.apache.common.faces.state.annotation.StateChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateChartScoped
@Named("order")
public class OrderBean implements Serializable {
    
    private Order data;
    
    private Product selectedProduct;
    
    public boolean prepareInsert() {
        data = new Order();
        return true;
    }

    public Order getData() {
        return data;
    }

    public List<Product> getProducts() {
        return data.getProducts();
    }
    
    public Product getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(Product selectedProduct) {
        this.selectedProduct = selectedProduct;
    }
    
    public boolean isRemovableProduct() {
       return selectedProduct != null;
    }

    public void addProduct(Product product) {
        data.getProducts().add(product);
        calculate();
    }

    public void removeProduct(Product product) {
        data.getProducts().remove(product);
        calculate();
    }

    private void calculate() {
        Double sum = 0.0;
        for (Product product : data.getProducts()) {
            sum += product.getCost();
        }
        data.setCost(sum);
    }
    
}
