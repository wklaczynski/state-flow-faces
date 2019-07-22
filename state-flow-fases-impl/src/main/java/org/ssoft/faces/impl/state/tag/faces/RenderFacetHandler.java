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
package org.ssoft.faces.impl.state.tag.faces;

import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.StateFlow;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartFacetRender;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.utils.ComponentUtils;
import javax.faces.state.execute.ExecutorController;
import org.ssoft.faces.impl.state.execute.ExecutorContextStackManager;
import org.ssoft.faces.impl.state.log.FlowLogger;

/**
 */
public class RenderFacetHandler extends ComponentHandler {

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();

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
        FacesContext context = ctx.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        UIStateChartFacetRender render = (UIStateChartFacetRender) c;

        UIComponent cc = ComponentUtils.findCompositeComponentUsingLocation(context, tag.getLocation());

        SCXMLExecutor executor;
        if (cc != null) {
            ExecutorController controller = (ExecutorController) cc
                    .getAttributes().get(StateFlow.EXECUTOR_CONTROLLER_KEY);
            
            if (controller == null) {
                throw new TagException(this.tag,
                        "Unable to render facet execute component, controller "
                        + "executor can not be defined in the composite component.");
            }

            executor = controller.getExecutor();

            if (executor == null) {
                throw new TagException(this.tag,
                        "Unable to render facet execute component, controller "
                        + "executor can not be started.");
            }

            if (!executor.isRunning()) {
                LOGGER.warning(String.format(
                        "%s request to activate bean in executor, "
                        + "but that executor is not active.", tag));
            }

            render.setExecutor(executor);
        } else {
            executor = handler.getRootExecutor(context);
            if (executor == null) {
                throw new TagException(this.tag,
                        "Unable to render facet execute component, "
                        + " view root executor can not be started.");
            }

            if (!executor.isRunning()) {
                LOGGER.warning(String.format(
                        "%s request to activate bean in executor, "
                        + "but that executor is not active.", tag));
            }

            render.setExecutor(executor);
        }

        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(context);
        ExecuteContext executeContext = manager.findExecuteContextByComponent(context, render);
        manager.push(executeContext);
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        FacesContext context = ctx.getFacesContext();
        ExecutorContextStackManager manager = ExecutorContextStackManager.getManager(context);
        manager.pop();
    }

}
