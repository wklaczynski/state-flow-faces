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
package org.apache.common.faces.prime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import static org.apache.common.faces.prime.StateFlowImplConstants.STATE_FLOW_DISPATCH_TASK;
import static org.apache.common.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import org.apache.common.faces.state.task.DelayedEventTask;
import org.apache.common.faces.state.task.TimerEventProducer;
import org.primefaces.PrimeFaces;
import org.primefaces.component.api.ClientBehaviorRenderingMode;
import org.primefaces.context.RequestContext;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.ComponentTraversalUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TimerEventProducerImpl extends TimerEventProducer {

    @Override
    public void encodeBegin(List<DelayedEventTask> taskList) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (!taskList.isEmpty()) {
            Map<Object, Object> attrs = context.getAttributes();
            DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
            taskList = new ArrayList<>(taskList);

            DelayedEventTask newTask = taskList.stream()
                    .sorted((o1, o2) -> {
                        return (int) (o1.getTime() - o2.getTime());
                    })
                    .findFirst().get();

            if (curTask == null) {
                curTask = newTask;
            } else if (curTask.getTime() > newTask.getTime()) {
                curTask = newTask;
            }
            attrs.put(STATE_FLOW_DISPATCH_TASK, curTask);
        }
    }

    @Override
    public void encodeEnd() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> attrs = context.getAttributes();
        DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
        if (curTask != null) {
            sendTaskScript(curTask);
        }
    }

    private void sendTaskScript(DelayedEventTask task) {
        if (task == null) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> attrs = context.getAttributes();

        UIViewRoot viewRoot = context.getViewRoot();
        String update = "@form";

        String sourceComponentId = (String) attrs.get(CURRENT_COMPONENT_HINT);

        RequestContext requestContext = RequestContext.getCurrentInstance();
        ClientBehaviorRenderingMode renderingMode
                = ClientBehaviorRenderingMode.OBSTRUSIVE;

        String formId = null;
        String sourceId = viewRoot.getClientId(context);
        UIComponent component;

        if (sourceComponentId != null) {
            component = context.getViewRoot().findComponent(sourceComponentId);
            if (component != null) {
                UIComponent form = ComponentTraversalUtils.closestForm(context, component);
                if (form != null) {
                    formId = form.getClientId(context);
                }
                sourceId = component.getClientId();
            }
        } else {
            update = "@all";
            component = viewRoot;
        }

        long delay = task.getTime() - System.currentTimeMillis();

        AjaxRequestBuilder builder = requestContext.getAjaxRequestBuilder();
        String ajaxscript = builder.init()
                .source(sourceId)
                .form(formId)
                .event("scxmltask")
                .update(component, update)
                .process(component, "@none")
                .async(false)
                .global(false)
                .delay(null)
                .timeout(0)
                .partialSubmit(false, false, null)
                .resetValues(false, false)
                .ignoreAutoUpdate(true)
                .onstart(null)
                .onerror(null)
                .onsuccess(null)
                .oncomplete(null)
                .buildBehavior(renderingMode);

        ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
        Resource resource = resourceHandler.createResource("scxml.js", "flowfaces");
        String scpath = resource.getRequestPath();

        StringBuilder sb = new StringBuilder();

        sb.append("PrimeFaces.getScript('").append(scpath)
                .append("', function(){");

        sb.append("window.scxmltask = setTimeout(function(){");
        sb.append("clearTimeout(window.scxmltask);");
        sb.append(ajaxscript);
        sb.append("},");
        sb.append(String.valueOf(delay));
        sb.append(")");

        sb.append("});");

        PrimeFaces.current().executeScript(sb.toString());
        sb.setLength(0);

    }

}
