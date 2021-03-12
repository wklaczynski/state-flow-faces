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
package org.ssoft.faces.impl.state.renderer;

import java.io.IOException;
import java.util.Map;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_FLOW_DISPATCH_TASK;
import javax.faces.state.task.DelayedEventTask;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowScriptRenderer extends Renderer {

    public static final String RENDERER_TYPE = "javax.faces.state.StateFlowScriptRenderer";
    
    
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();

        FacesContext fc = FacesContext.getCurrentInstance();
        Map<Object, Object> attrs = fc.getAttributes();
        DelayedEventTask curTask = (DelayedEventTask) attrs.get(STATE_FLOW_DISPATCH_TASK);
        if (curTask != null) {
            long delay = curTask.getTime() - System.currentTimeMillis();
            writer.startElement("script", component);
            writer.writeAttribute("type", "text/javascript", "type");
            writer.write("window.scxmltask = setTimeout(function(){");
            writer.write("clearTimeout(window.scxmltask);");
            writer.write("jsf.ajax.request(this,'scxmltask',{");
            writer.write("execute:'@none',render:'@all'");
            writer.write("})},");
            writer.write(String.valueOf(delay));
            writer.write(")");
            writer.endElement("script");
        }

    }

    @Override
    public boolean getRendersChildren() {
        return false;
    }

}
