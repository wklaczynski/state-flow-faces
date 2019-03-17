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
package org.ssoft.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.ssoft.faces.impl.state.tag.AbstractFlowTagHandler;
import javax.faces.state.scxml.model.ParsedValue;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.state.scxml.model.Content;
import javax.faces.state.scxml.model.ContentContainer;
import javax.faces.state.scxml.model.DoneData;
import javax.faces.state.scxml.model.Invoke;
import javax.faces.state.scxml.model.Send;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ContentTagHandler extends AbstractFlowTagHandler<Content> {

    /**
     *
     */
    protected final TagAttribute expr;

    private ParsedValue staticValue;
    private boolean resolved;

    /**
     *
     * @param config
     */
    public ContentTagHandler(TagConfig config) {
        super(config, Content.class);

        in("donedata", DoneData.class);
        in("send", Send.class);
        in("invoke", Invoke.class);

        this.expr = this.getAttribute("expr");
    }

    /**
     *
     * @param ctx
     * @param parent
     * @param chart
     * @param parentElement
     * @throws IOException
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        ContentContainer continer = (ContentContainer) parentElement;
        if (continer.getContent() != null) {
            throw new TagException(this.tag, "already defined in this element!");
        }

        Content data = new Content();
        decorate(ctx, parent, data);

        if (expr != null) {
            data.setExpr(expr.getValue());
        } else if (!resolved || !isProductionMode(ctx)) {
            staticValue = getParsedBodyValue(ctx, parent);
            data.setParsedValue(staticValue);
        }
        resolved = true;

        continer.setContent(data);

    }

}
