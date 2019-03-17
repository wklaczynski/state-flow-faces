/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.faces.impl.state.tag.faces;

import javax.faces.component.UIComponent;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.impl.state.utils.ComponentUtils;
import org.apache.common.faces.state.component.UIStateChartExecutor;
import org.apache.common.faces.state.component.UIStateChartFacetRender;

/**
 */
public class RenderFacetHandler extends ComponentHandler {

    // Supported attribute names
    private static final String SLOT_ATTRIBUTE = "slot";

    // Attributes
    // This attribute is required.
    TagAttribute slot;

    public RenderFacetHandler(ComponentConfig config) {
        super(config);
        slot = this.getAttribute(SLOT_ATTRIBUTE);
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        super.onComponentCreated(ctx, c, parent); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {

        UIStateChartExecutor controller = ComponentUtils.assigned(UIStateChartExecutor.class, parent);
        if (controller == null) {
            throw new TagException(this.tag,
                    "Unable to localize execute component, "
                    + "this component must be closet in execute component.");
        }

        String executorId = controller.getExecutorId();
        UIStateChartFacetRender render = (UIStateChartFacetRender) c;
        render.setExecutorId(executorId);
    }

}
