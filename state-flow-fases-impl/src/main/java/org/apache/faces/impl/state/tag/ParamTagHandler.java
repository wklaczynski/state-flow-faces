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
package org.apache.faces.impl.state.tag;

import java.io.IOException;
import java.util.List;
import javax.faces.component.UIComponent;
import org.apache.scxml.model.Invoke;
import org.apache.scxml.model.Param;
import org.apache.scxml.model.Send;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.scxml.model.DoneData;
import org.apache.scxml.model.ParamsContainer;
import org.apache.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParamTagHandler extends AbstractFlowTagHandler<Param> {

    protected final TagAttribute name;
    protected final TagAttribute expr;
    
    public ParamTagHandler(TagConfig config) {
        super(config, Param.class);
        
        in("invoke", Invoke.class);
        in("send", Send.class);
        in("donedata", DoneData.class);
        
        this.name = this.getRequiredAttribute("name");
        this.expr = this.getRequiredAttribute("expr");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        List<Param> params = null;
        if(parentElement instanceof ParamsContainer) {
            ParamsContainer pc = (ParamsContainer) parentElement;
            params = pc.getParams();
        }
        
        Param param = new Param();
        decorate(ctx, parent, param);

        param.setName(name.getValue());
        param.setExpr(expr.getValue());
        
        applyNext(ctx, parent, param);
        
        params.add(param);
    }

}
