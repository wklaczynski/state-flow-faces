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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.state.StateFlow;
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_CONTROLLER;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_VIEW_ID;
import static org.apache.common.faces.state.StateFlow.PORTLET_CONTROLLER_TYPE;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.component.UIStateChartController;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.SCXMLConstants;
import org.apache.common.faces.state.scxml.SCXMLExecutor;
import org.apache.common.faces.state.scxml.model.CommonsSCXML;
import org.apache.common.faces.state.scxml.model.CustomAction;
import org.apache.common.faces.state.scxml.model.CustomActionWrapper;
import org.apache.common.faces.state.scxml.model.ModelException;
import org.apache.common.faces.state.scxml.model.SCXML;
import org.apache.common.faces.state.scxml.model.Var;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_CONTINER_NAME;
import static org.apache.common.faces.state.StateFlow.FACES_CHART_CONTINER_SOURCE;

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
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        FacesContext context = ctx.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        UIViewRoot viewRoot = context.getViewRoot();
        String viewId = viewRoot.getViewId();

        String scxmlName = name.getValue(ctx);

        String rootId = handler.getExecutorViewRootId(context);

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        if (compositeParent != null) {
            URL url = getCompositeURL(ctx);
            if (url == null) {
                throw new TagException(this.tag,
                        "Unable to localize composite url '"
                        + scxmlName
                        + "' in parent composite component with id '"
                        + compositeParent.getClientId(ctx.getFacesContext())
                        + '\'');
            }
            String executorName = "controller[" + tag + "]" + viewId + "!" + url.getPath() + "#" + scxmlName;
            String executorId = rootId + ":" + UUID.nameUUIDFromBytes(executorName.getBytes()).toString();

            String uuid = UUID.nameUUIDFromBytes(url.getPath().getBytes()).toString();
            String stateContinerName = STATECHART_FACET_NAME + "_" + uuid;

            applyNext(ctx, parent, executorId, stateContinerName, url);
        } else {
            String executorName = "controller[" + tag + "]" + viewId + "#" + scxmlName;
            String executorId = rootId + ":" + UUID.nameUUIDFromBytes(executorName.getBytes()).toString();

            String stateContinerName = STATECHART_FACET_NAME;

            applyNext(ctx, parent, executorId, stateContinerName, rootId);
        }
    }

    public void applyNext(FaceletContext ctx, UIComponent parent, String executorId, String continerName, Object continerSource) throws IOException {
        FacesContext context = ctx.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String viewId = context.getViewRoot().getViewId();

        SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
        if (executor == null) {

            SCXML stateMachine = findStateMachine(ctx, continerName, continerSource);
            try {
                executor = handler.createRootExecutor(executorId, context, stateMachine);
                executor.getSCInstance().getSystemContext();
                Context sctx = executor.getRootContext();
                sctx.setLocal(FACES_CHART_CONTROLLER, PORTLET_CONTROLLER_TYPE);
                sctx.setLocal(FACES_CHART_CONTINER_NAME, continerName);
                sctx.setLocal(FACES_CHART_CONTINER_SOURCE, continerSource);

                sctx.setLocal(FACES_CHART_VIEW_ID, viewId);

            } catch (ModelException ex) {
                throw new IOException(ex);
            }
            context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);

            Map<String, Object> params = getParamsMap(ctx, parent);
            handler.execute(context, executor, params, true);
        }

        if (!executor.isRunning()) {
            handler.close(context, executor);
        }

        if (context.getResponseComplete()) {
            handler.writeState(context);
        }

        String path = viewId + "!" + executorId;
        StateFlow.pushExecutorToEL(context, executor, path);
        try {
            super.apply(ctx, parent);
        } finally {
            StateFlow.popExecutorFromEL(context);
        }
    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        FacesContext context = ctx.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        SCXMLExecutor executor = handler.getRootExecutor(context);
        Context sctx = executor.getRootContext();

        UIStateChartController controller = (UIStateChartController) c;
        String executorId = executor.getId();

        controller.setExecutorId(executorId);

    }

    public static URL getCompositeURL(FaceletContext ctx) {
        URL url = null;
        try {
            Field ffield = ctx.getClass().getDeclaredField("facelet");
            boolean faccessible = ffield.isAccessible();
            try {
                ffield.setAccessible(true);
                Facelet facelet = (Facelet) ffield.get(ctx);
                Field sfield = facelet.getClass().getDeclaredField("src");
                boolean saccessible = sfield.isAccessible();
                try {
                    sfield.setAccessible(true);
                    url = (URL) sfield.get(facelet);
                } finally {
                    sfield.setAccessible(saccessible);
                }

            } finally {
                ffield.setAccessible(faccessible);
            }

        } catch (Throwable ex) {
        }

        return url;
    }

    public SCXML findStateMachine(FaceletContext ctx, String continerName, Object continerSource) throws IOException {
        FacesContext context = ctx.getFacesContext();
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String scxmlId = name.getValue(ctx);

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(context);
        if (compositeParent != null) {

            URL url = (URL) continerSource;
            if (url == null) {
                throw new TagException(this.tag,
                        "Unable to localize composite url '"
                        + scxmlId
                        + "' in parent composite component with id '"
                        + compositeParent.getClientId(ctx.getFacesContext())
                        + '\'');
            }

            if (continerName == null) {
                throw new TagException(tag, String.format(
                        "Can not find scxml definition \"%s\", "
                        + "view location not found in composite component.",
                        scxmlId));
            }

            try {
                SCXML scxml = handler.getStateMachine(context, url, continerName, scxmlId);
                if (scxml == null) {
                    throw new TagException(tag, String.format(
                            "Can not find scxml definition id=\"%s\", not found"
                            + " in composite <f:metadata...",
                            scxmlId));
                }

                return scxml;
            } catch (ModelException ex) {
                throw new TagException(tag, String.format(
                        "can not find scxml definition \"%s\", throw model exception.",
                        scxmlId), ex);
            }
        } else {
            try {
                SCXML scxml = handler.findStateMachine(context, scxmlId);
                if (scxml == null) {
                    throw new TagException(tag, String.format(
                            "can not find scxml definition id=\"%s\", not found"
                            + " in composite <f:metadata...",
                            scxmlId));
                }
                return scxml;
            } catch (ModelException ex) {
                throw new TagException(tag, String.format(
                        "can not find scxml definition \"%s\", throw model exception.",
                        scxmlId), ex);
            }
        }
    }

    private Map<String, Object> getParamsMap(FaceletContext ctx, UIComponent parent) {
        Map<String, Object> params = new LinkedHashMap<>();

        FaceletHandler next = nextHandler;
        if (next instanceof CompositeFaceletHandler) {
            CompositeFaceletHandler compo = (CompositeFaceletHandler) nextHandler;
            FaceletHandler[] children = compo.getHandlers();
            if (children != null) {
                for (FaceletHandler child : children) {
                    if (child instanceof ComponentHandler) {
                        ComponentHandler ch = (ComponentHandler) child;
                        Tag ctag = ch.getTag();
                        String namespace = ctag.getNamespace();
                        if (ctag.getLocalName().equals("param")
                                && (namespace.equals("http://xmlns.jcp.org/jsf/core")
                                || namespace.equals("http://java.sun.com/jsf/core"))) {

                            TagAttribute nameAttr = ctag.getAttributes().get("name");
                            TagAttribute valueAttr = ctag.getAttributes().get("value");
                            
                            String pname = nameAttr.getValue(ctx);
                            Object pvalue = valueAttr.getValue(ctx);
                            params.put(pname, pvalue);
                        }
                    }
                }
            }
        }


        return params;
    }

    private static String localPath(FacesContext context, String path) {
        String base = context.getExternalContext().getRealPath("/").replace("\\", "/");
        String result = path.replaceFirst(base, "");

        if (result.startsWith("/resources")) {
            result = result.substring(10);
            return result;
        }

        int sep = result.lastIndexOf("/META-INF/resources");
        if (sep > -1) {
            result = result.substring(sep + 19);
            return result;
        }

        return result;
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
