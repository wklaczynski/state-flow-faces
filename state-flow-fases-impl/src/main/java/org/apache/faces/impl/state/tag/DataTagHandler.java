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
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.scxml.ContentParser;
import org.apache.scxml.PathResolver;
import org.apache.scxml.model.Data;
import org.apache.scxml.model.Datamodel;
import org.apache.scxml.model.ParsedValue;
import org.apache.scxml.model.SCXML;
import org.apache.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataTagHandler extends AbstractFlowTagHandler<Data> {

    protected final TagAttribute id;
    protected final TagAttribute src;
    protected final TagAttribute expr;

    private ParsedValue staticValue;

    public DataTagHandler(TagConfig config) {
        super(config, Data.class);

        in("datamodel", Datamodel.class);

        this.id = this.getRequiredAttribute("id");
        this.src = this.getAttribute("src");
        this.expr = this.getAttribute("expr");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        Data data = new Data();
        decorate(ctx, parent, data);

        data.setId(id.getValue());

        data.setExpr(expr != null ? expr.getValue() : null);

        applyNext(ctx, parent, data);

        if (src != null) {
            data.setSrc(src.getValue());
        }

        if (isProductionMode(ctx) && staticValue != null) {
            data.setParsedValue(staticValue);
        } else if (src != null) {
            String resolvedSrc = src.getValue();
            final PathResolver pr = chart.getPathResolver();
            if (pr != null) {
                resolvedSrc = pr.resolvePath(resolvedSrc);
            }
            try {
                staticValue = ContentParser.parse(resolvedSrc);
                data.setParsedValue(staticValue);
            } catch (IOException e) {
                throw new TagException(this.tag,
                        String.format("can not build data %s.", Util.getErrorMessage(e)));
            }

        }

        Datamodel datamodel = (Datamodel) parentElement;
        datamodel.addData(data);

    }

    private void buildex(String errmsg) {
        throw new TagException(this.tag,
                String.format("can not build data %s.", errmsg));
    }

}
