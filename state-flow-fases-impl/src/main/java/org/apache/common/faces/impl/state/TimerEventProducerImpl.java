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
package org.apache.common.faces.impl.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import static org.apache.common.faces.impl.state.StateFlowImplConstants.STATE_FLOW_DISPATCH_TASK;
import javax.faces.state.task.DelayedEventTask;
import javax.faces.state.task.TimerEventProducer;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class TimerEventProducerImpl extends TimerEventProducer {

    @Override
    public void encodeBegin(List<DelayedEventTask> taskList) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<Object, Object> attrs = context.getAttributes();
        if (!taskList.isEmpty()) {
            DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
            taskList = new ArrayList<>(taskList);

            Collections.sort(taskList, (o1, o2) -> {
                return (int) (o1.getTime() - o2.getTime());
            });
            DelayedEventTask newTask = taskList.get(0);

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
        PartialViewContext partial = context.getPartialViewContext();
        DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
        if (curTask != null && !partial.isAjaxRequest()) {
            Application application = context.getApplication();

            UIComponent componentResource = application.createComponent(UIOutput.COMPONENT_TYPE);
            componentResource.setRendererType("org.apache.common.faces.StateFlowScriptRenderer");
            componentResource.getAttributes().put("target", "head");
            componentResource.setId("stateFlowDispatcher");
            componentResource.setRendered(true);

            context.getViewRoot().addComponentResource(context, componentResource, "head");
        }
    }

}
