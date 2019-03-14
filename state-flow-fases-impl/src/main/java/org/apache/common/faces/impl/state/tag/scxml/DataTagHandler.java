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
package org.apache.common.faces.impl.state.tag.scxml;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.common.faces.state.scxml.PathResolver;
import org.apache.common.faces.state.scxml.model.Data;
import org.apache.common.faces.state.scxml.model.Datamodel;
import org.apache.common.faces.state.scxml.model.ParsedValue;
import org.apache.common.faces.state.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataTagHandler extends AbstractFlowTagHandler<Data> {

    /**
     *
     */
    protected final TagAttribute id;

    /**
     *
     */
    protected final TagAttribute src;

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
    public DataTagHandler(TagConfig config) {
        super(config, Data.class);

        in("datamodel", Datamodel.class);

        this.id = this.getRequiredAttribute("id");
        this.src = this.getAttribute("src");
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
        Data data = new Data();
        decorate(ctx, parent, data);

        data.setId(id.getValue());
        
        if (isProductionMode(ctx) && staticValue != null) {
            data.setParsedValue(staticValue);
        } else if (expr != null) {
            String value = expr.getValue(ctx);
            data.setExpr(value);
        } else if (src != null && !src.isLiteral()) {
            data.setSrc(src.getValue());
        } else if (src != null && src.isLiteral()) {
            String resolvedSrc = src.getValue();
            final PathResolver pr = chart.getPathResolver();
            if (pr != null) {
                resolvedSrc = pr.resolvePath(resolvedSrc);
            }
            staticValue = getParsedResorceValue(ctx, parent, resolvedSrc);
            data.setParsedValue(staticValue);
        } else if (!resolved || !isProductionMode(ctx)) {
            staticValue = getParsedBodyValue(ctx, parent);
            data.setParsedValue(staticValue);
        }
        resolved = true;

        Datamodel datamodel = (Datamodel) parentElement;
        datamodel.addData(data);

    }

}
