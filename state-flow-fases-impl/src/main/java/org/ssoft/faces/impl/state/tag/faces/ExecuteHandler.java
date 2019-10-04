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

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.state.StateFlow;
import static javax.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import javax.faces.view.facelets.ComponentConfig;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import static javax.faces.state.StateFlow.CONTROLLER_SET_HINT;
import static javax.faces.state.StateFlow.EXECUTOR_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.FACES_CHART_CONTINER_NAME;
import static javax.faces.state.StateFlow.FACES_CHART_CONTINER_SOURCE;
import static javax.faces.state.StateFlow.FACES_CHART_CONTROLLER_TYPE;
import static javax.faces.state.StateFlow.FACES_CHART_EXECUTOR_VIEW_ID;
import javax.faces.state.component.UIStateChartExecutor;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import static javax.faces.state.StateFlow.STATE_CHART_FACET_NAME;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.execute.ExecutorController;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.view.facelets.CompositeFaceletHandler;
import javax.faces.view.facelets.FaceletHandler;
import javax.faces.view.facelets.Tag;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;
import org.ssoft.faces.impl.state.log.FlowLogger;
import static org.ssoft.faces.impl.state.utils.Util.findStateMachine;

/**
 */
public class ExecuteHandler extends ComponentHandler {

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();

    public static final String CONTROLLER_PUBLISH_QUEUE_HINT = "javax.faces.flow.CONTROLLER_PUBLISH_QUEUE_HINT";

    // Supported attribute names
    private static final String NAME_ATTRIBUTE = "name";

    // Attributes
    // This attribute is required.
    TagAttribute name;

    public ExecuteHandler(ComponentConfig config) {
        super(config);
        name = this.getAttribute(NAME_ATTRIBUTE);
    }

    @Override
    public void applyNextHandler(FaceletContext ctx, UIComponent c) throws IOException, FacesException, ELException {
        FacesContext context = ctx.getFacesContext();
        UIStateChartExecutor component = (UIStateChartExecutor) c;

        ExecuteContextManager manager = ExecuteContextManager.getManager(context);
        boolean pushed = false;
        try {
            String executorId = component.getExecutorId();
            ExecuteExpressionFactory.getBuildPathStack(context).push(executorId);

            StateFlowHandler handler = StateFlowHandler.getInstance();
            SCXMLExecutor executor = handler.getRootExecutor(context, executorId);
            if (executor != null) {
                String executePath = executor.getId();
                Context ectx = executor.getGlobalContext();
                ExecuteContext executeContext = new ExecuteContext(
                        executePath, executor, ectx);

                manager.initExecuteContext(context, executePath, executeContext);
                pushed = manager.push(executeContext);
            }

            super.applyNextHandler(ctx, c);

        } finally {
            if (pushed) {
                manager.pop();
            }
            ExecuteExpressionFactory.getBuildPathStack(context).pop();
        }

    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        FacesContext context = ctx.getFacesContext();

        UIViewRoot viewRoot = context.getViewRoot();
        String viewId = viewRoot.getViewId();

        ValueExpression ve = name.getValueExpression(ctx, String.class);
        String scxmlName = (String) ve.getValue(ctx);

        UIStateChartExecutor component = (UIStateChartExecutor) c;

        String rootId = (String) context.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);

        URL url = getCompositeURL(ctx);
        if (url == null) {
            throw new TagException(this.tag,
                    "Unable to localize composite url '"
                    + scxmlName
                    + "' in parent composite component with id '"
                    + parent.getClientId(ctx.getFacesContext())
                    + '\'');
        }

        c.getAttributes().put(UIStateChartExecutor.SCXML_URL, url);
        c.getAttributes().put(UIStateChartExecutor.SCXML_NAME, scxmlName);

        String executorName = "controller[" + tag + "]" + viewId + "!" + url.getPath() + "#" + scxmlName;
        String executorId = rootId + ":" + UUID.nameUUIDFromBytes(executorName.getBytes()).toString();

        String currentExecutorId = component.getExecutorId();
        if (currentExecutorId != null && !currentExecutorId.equals(executorId)) {
            throw new TagException(this.tag, "Render state component can not multiple start in the same composite component.");
        }

        Map<String, Object> ccattrs = null;
        UIComponent cc = null;

        if (null != parent && null != (cc = parent.getParent()) && UIComponent.isCompositeComponent(cc)) {
            ccattrs = cc.getAttributes();
        }

        component.setExecutorId(executorId);
        if (ccattrs != null) {
            ExecutorController controller = (ExecutorController) ccattrs
                    .get(StateFlow.EXECUTOR_CONTROLLER_KEY);

            if (controller == null) {
                controller = new ExecutorController();
                ccattrs.put(StateFlow.EXECUTOR_CONTROLLER_KEY, controller);
            }
            controller.setExecutorId(executorId);
        }

        buildController(ctx, viewRoot, component);

    }

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        FacesContext context = ctx.getFacesContext();
        ArrayList<String> clientIds = (ArrayList<String>) context.getViewRoot().getAttributes().get(CONTROLLER_SET_HINT);
        if (clientIds == null) {
            clientIds = new ArrayList<>();
            context.getViewRoot().getAttributes().put(CONTROLLER_SET_HINT, clientIds);
        }

//        String clientId = c.getClientId(context);
//        if (!clientIds.contains(clientId)) {
//            clientIds.add(clientId);
//        }

    }

    private SCXMLExecutor buildController(FaceletContext ctx, UIViewRoot viewRoot, UIStateChartExecutor component) {
        FacesContext fc = ctx.getFacesContext();
        String executorId = component.getExecutorId();
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String viewId = viewRoot.getViewId();
        String rootId = (String) fc.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);

        String scxmlName = (String) component.getAttributes().get(UIStateChartExecutor.SCXML_NAME);
        URL url = (URL) component.getAttributes().get(UIStateChartExecutor.SCXML_URL);

        SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);
        if (executor == null) {
            String uuid = UUID.nameUUIDFromBytes(url.getPath().getBytes()).toString();
            String continerName = STATE_CHART_FACET_NAME + "_" + uuid;

            Map<String, Object> params = new HashMap<>();
            //            component.pushComponentToEL(fc, component);
            try {
                SCXML stateMachine = findStateMachine(fc, continerName, scxmlName, url);
                executor = handler.createRootExecutor(executorId, fc, stateMachine);
                executor.getSCInstance().getSystemContext();
                Context sctx = executor.getRootContext();
                sctx.setLocal(FACES_CHART_CONTROLLER_TYPE, EXECUTOR_CONTROLLER_TYPE);
                sctx.setLocal(FACES_CHART_CONTINER_NAME, continerName);
                sctx.setLocal(FACES_CHART_CONTINER_SOURCE, url);
                sctx.setLocal(FACES_EXECUTOR_VIEW_ROOT_ID, rootId);

                sctx.setLocal(FACES_CHART_EXECUTOR_VIEW_ID, viewId);

            } catch (Throwable ex) {
                throw new TagException(tag, ex);
            } finally {
//                component.popComponentFromEL(fc);
            }

            resolveParams(ctx, component, params);
            handler.execute(fc, executor, params);
        }

        if (!executor.isRunning()) {
            LOGGER.warning(String.format(
                    "%s request to activate bean in executor \"%s\", "
                    + "but that executor is not active.", tag, scxmlName));
        }

        try {
            String evtname = BEFORE_PHASE_EVENT_PREFIX
                    + PhaseId.RESTORE_VIEW.getName().toLowerCase();

            EventBuilder eb = new EventBuilder(evtname, TriggerEvent.CALL_EVENT)
                    .sendId(viewId);

            executor.triggerEvent(eb.build());
        } catch (ModelException ex) {
            throw new TagException(tag, ex);
        }

        if (fc.getResponseComplete()) {
            handler.writeState(fc);
        }

        return executor;
    }

    private void resolveParams(FaceletContext ctx, UIStateChartExecutor component, Map<String, Object> params) {
        Map<String, Object> paramsMap = getParamsMap(ctx);
        params.putAll(paramsMap);
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

    private Map<String, Object> getParamsMap(FaceletContext ctx) {
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
                            ValueExpression paramValueExpression = valueAttr.getValueExpression(ctx, Object.class);
                            if (paramValueExpression.isLiteralText()) {

                            } else {

                            }

                            Object pvalue = valueAttr.getValue(ctx);
                            params.put(pname, pvalue);
                        }
                    }
                }
            }
        }

        return params;
    }

}
