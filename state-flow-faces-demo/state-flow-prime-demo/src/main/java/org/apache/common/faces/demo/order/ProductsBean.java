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
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.common.faces.demo.order.data.Product;
import javax.faces.state.annotation.ChartScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
@ChartScoped
@Named("products")
public class ProductsBean implements Serializable {
    
    private List<Product> data;

    @Inject
    private OrdersBean orders;
    
    private Product selected;
    
    public boolean prepare() {
        data = orders.getProducts();
        return true;
    }

    public List<Product> getData() {
        return data;
    }

    public Product getSelected() {
        return selected;
    }

    public void setSelected(Product selected) {
        this.selected = selected;
    }
    
}
