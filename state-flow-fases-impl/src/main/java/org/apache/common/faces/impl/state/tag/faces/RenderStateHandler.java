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

import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import static org.apache.common.faces.state.component.UIStateChartController.SCXML_PATH_KEY;
import org.apache.common.faces.state.component.UIStateChartDefinition;
import org.apache.common.scxml.SCXMLConstants;
import org.apache.common.scxml.model.CommonsSCXML;
import org.apache.common.scxml.model.CustomAction;
import org.apache.common.scxml.model.CustomActionWrapper;
import org.apache.common.scxml.model.Var;
import static org.apache.common.faces.impl.state.utils.Util.getScxmlPath;

/**
 * The class in this SCXML object model that corresponds to the
 * {@link CustomAction} &lt;var&gt; SCXML element.
 * <p>
 * When manually constructing or modifying a SCXML model using this custom
 * action, either:
 * <ul>
 * <li>derive from {@link CommonsSCXML}, or</li>
 * <li>make sure to add the {@link SCXMLConstants#XMLNS_COMMONS_SCXML} namespace
 * with the {@link SCXMLConstants#XMLNS_COMMONS_SCXML_PREFIX} prefix to the
 * SCXML object, or</li>
 * <li>wrap the {@link Var} instance in a {@link CustomActionWrapper} (for which
 * the {@link #CUSTOM_ACTION} can be useful) before adding it to the object
 * model</li>
 * </ul>
 * before write the SCXML model with {@link SCXMLWriter}. The writing will fail
 * otherwise!
 * </p>
 */
public class RenderStateHandler extends ComponentHandler {

    // Supported attribute names
    private static final String NAME_ATTRIBUTE = "name";
    private static final String REQUIRED_ATTRIBUTE = "required";

    // Attributes
    // This attribute is required.
    TagAttribute name;

    // This attribute is not required.  If not defined, then assume the facet
    // isn't necessary.
    TagAttribute required;

    public RenderStateHandler(ComponentConfig config) {
        super(config);
        name = this.getAttribute(NAME_ATTRIBUTE);
        required = this.getAttribute(REQUIRED_ATTRIBUTE);
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        FacesContext fc = ctx.getFacesContext();
        UIComponent root = fc.getViewRoot();
        UIComponent stateContiner = null;

        boolean requiredValue = ((this.required != null) && this.required.getBoolean(ctx));
        String nameValue = this.name.getValue(ctx);

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        if (compositeParent != null) {
            stateContiner = null;
            Map<String, UIComponent> facetMap = compositeParent.getFacets();
            UIComponent panel = facetMap.get(UIComponent.COMPOSITE_FACET_NAME);
            if (panel.getFacetCount() > 0) {
                stateContiner = panel.getFacets().get(STATECHART_FACET_NAME);
            }

            if (requiredValue && stateContiner == null) {
                throwRequiredException(ctx, nameValue, compositeParent);
            }

            if (stateContiner != null) {
                if (stateContiner.getChildCount() == 0 && requiredValue) {
                    throwRequiredException(ctx, nameValue, compositeParent);
                }

                UIStateChartDefinition uichart = (UIStateChartDefinition)
                        stateContiner.findComponent(nameValue);
                
                if (uichart == null && requiredValue) {
                    throwRequiredException(ctx, nameValue, compositeParent);
                }
            }
        } else {
            if (root.getFacetCount() > 0) {
                stateContiner = root.getFacets().get(STATECHART_FACET_NAME);
            }

            if (requiredValue && stateContiner == null) {
                throwRequiredInRootException(ctx, nameValue, root);
            }

            if (stateContiner != null) {
                if (stateContiner.getChildCount() == 0 && requiredValue) {
                    throwRequiredInRootException(ctx, nameValue, root);
                }

                UIStateChartDefinition uichart = (UIStateChartDefinition)
                        stateContiner.findComponent(nameValue);
                
                if (uichart == null && requiredValue) {
                    throwRequiredInRootException(ctx, nameValue, root);
                }
            }
        }
        
        String path = getScxmlPath(ctx, fc.getViewRoot());
        c.getAttributes().put(SCXML_PATH_KEY, path);
        
    }
    
    // --------------------------------------------------------- Private Methods
    private void throwRequiredException(FaceletContext ctx,
            String name,
            UIComponent compositeParent) {

        throw new TagException(this.tag,
                "Unable to find facet named '"
                + name
                + "' in parent composite component with id '"
                + compositeParent.getClientId(ctx.getFacesContext())
                + '\'');

    }

    private void throwRequiredInRootException(FaceletContext ctx,
            String name,
            UIComponent root) {

        throw new TagException(this.tag,
                "Unable to find facet named '"
                + name
                + "' in view component with id '"
                + root.getClientId(ctx.getFacesContext())
                + '\'');

    }

}
