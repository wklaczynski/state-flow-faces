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
package org.ssoft.faces.prime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import static org.ssoft.faces.prime.StateFlowImplConstants.STATE_FLOW_DISPATCH_TASK;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import javax.faces.state.task.DelayedEventTask;
import javax.faces.state.task.TimerEventProducer;
import org.kohsuke.MetaInfServices;
import org.primefaces.PrimeFaces;
import org.primefaces.component.api.AjaxSource;
import org.primefaces.component.api.ClientBehaviorRenderingMode;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.ComponentTraversalUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
@MetaInfServices(TimerEventProducer.class)
public class TimerEventProducerImpl extends TimerEventProducer implements AjaxSource{

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

        PrimeRequestContext requestContext = PrimeRequestContext.getCurrentInstance();
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
            update = "@none";
            component = viewRoot;
        }

        long delay = task.getTime() - System.currentTimeMillis();
        
        String process = "@none";
        
        AjaxSource ajaxBehavior = this;

        AjaxRequestBuilder builder = requestContext.getAjaxRequestBuilder();
        String request = builder.init()
                .source(sourceId)
                .event("scxmltask")
                .form(ajaxBehavior, component)
                .process(component, process)
                .update(component, ajaxBehavior.getUpdate())
                .async(ajaxBehavior.isAsync())
                .global(ajaxBehavior.isGlobal())
                .delay(ajaxBehavior.getDelay())
                .timeout(ajaxBehavior.getTimeout())
                .partialSubmit(ajaxBehavior.isPartialSubmit(), ajaxBehavior.isPartialSubmitSet(), ajaxBehavior.getPartialSubmitFilter())
                .resetValues(ajaxBehavior.isResetValues(), ajaxBehavior.isResetValuesSet())
                .ignoreAutoUpdate(ajaxBehavior.isIgnoreAutoUpdate())
                .onstart(ajaxBehavior.getOnstart())
                .onerror(ajaxBehavior.getOnerror())
                .onsuccess(ajaxBehavior.getOnsuccess())
                .oncomplete(ajaxBehavior.getOncomplete())
                .params(component)
                .buildBehavior(renderingMode);
        
        
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        sb.append("window.scxmltask = setTimeout(function(){");
        sb.append("clearTimeout(window.scxmltask);");
        sb.append(request);
        sb.append("},");
        sb.append(String.valueOf(delay));
        sb.append(")");

        sb.append("};");

        PrimeFaces.current().executeScript(sb.toString());
        sb.setLength(0);

    }

    @Override
    public String getOnstart() {
        return null;
    }

    @Override
    public String getOncomplete() {
        return null;
    }

    @Override
    public String getOnsuccess() {
        return null;
    }

    @Override
    public String getOnerror() {
        return null;
    }

    @Override
    public String getUpdate() {
        return null;
    }

    @Override
    public String getProcess() {
        return null;
    }

    @Override
    public boolean isGlobal() {
        return true;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isPartialSubmit() {
        return false;
    }

    @Override
    public boolean isPartialSubmitSet() {
        return false;
    }

    @Override
    public String getPartialSubmitFilter() {
        return null;
    }

    @Override
    public boolean isResetValues() {
        return false;
    }

    @Override
    public boolean isResetValuesSet() {
        return false;
    }

    @Override
    public boolean isIgnoreAutoUpdate() {
        return true;
    }

    @Override
    public boolean isAjaxified() {
        return false;
    }

    @Override
    public String getDelay() {
        return null;
    }

    @Override
    public int getTimeout() {
        return 0;
    }

    @Override
    public String getForm() {
        return null;
    }
}
