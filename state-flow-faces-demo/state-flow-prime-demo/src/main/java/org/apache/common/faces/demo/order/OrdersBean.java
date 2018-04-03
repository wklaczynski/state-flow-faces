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
import java.util.ArrayList;
import java.util.List;
import javax.inject.Named;
import org.apache.common.faces.demo.order.data.Order;
import org.apache.common.faces.demo.order.data.Product;
import org.apache.common.faces.state.annotation.StateDialogScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateDialogScoped
@Named("orders")
public class OrdersBean implements Serializable {
    
    private final List<Product> products = new ArrayList<>();

    private final List<Order> data = new ArrayList<>();
    
    private Order selected;
    
    public boolean prepare() {
        products.add(new Product(
                "Phone", "Microtec Phonee Model 1.0 ", 230.56
        ));
        products.add(new Product(
                "Computer", "Coomputer Intel Model 1.0 ", 230.56
        ));
        products.add(new Product(
                "Processor", "Processor Intel i7 ", 230.56
        ));
        products.add(new Product(
                "Nvidia GTX 8000", "Graphics Cart Nvidia GTX 8000", 230.56
        ));
        return true;
    }

    public List<Product> getProducts() {
        return products;
    }

    public List<Order> getData() {
        return data;
    }

    public Order getSelected() {
        return selected;
    }

    public void setSelected(Order selected) {
        this.selected = selected;
    }
    
    public boolean isEditable() {
        return selected != null;
    }
    
    public boolean isRemovable() {
        return selected != null;
    }
    
    public boolean isOpenable() {
        return selected != null;
    }
    
}
