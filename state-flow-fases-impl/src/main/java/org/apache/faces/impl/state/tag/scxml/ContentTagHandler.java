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
package org.apache.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.scxml.model.ParsedValue;
import org.apache.scxml.model.SCXML;
import org.apache.scxml.model.Content;
import org.apache.scxml.model.ContentContainer;
import org.apache.scxml.model.DoneData;
import org.apache.scxml.model.Invoke;
import org.apache.scxml.model.Send;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ContentTagHandler extends AbstractFlowTagHandler<Content> {

    protected final TagAttribute expr;

    private ParsedValue staticValue;

    public ContentTagHandler(TagConfig config) {
        super(config, Content.class);

        in("donedata", DoneData.class);
        in("send", Send.class);
        in("invoke", Invoke.class);

        this.expr = this.getAttribute("expr");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        ContentContainer continer = (ContentContainer) parentElement;
        if(continer.getContent() != null) {
            throw new TagException(this.tag, "already defined in this element!");
        }
        
        Content data = new Content();
        decorate(ctx, parent, data);

        data.setExpr(expr != null ? expr.getValue() : null);

        applyNext(ctx, parent, data);

        continer.setContent(data);

    }

}
