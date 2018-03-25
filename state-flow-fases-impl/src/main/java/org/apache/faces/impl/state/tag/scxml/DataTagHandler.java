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

import com.sun.faces.facelets.compiler.UIInstructions;
import java.io.IOException;
import java.net.URL;
import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.faces.impl.state.utils.Util;
import org.apache.scxml.PathResolver;
import org.apache.scxml.io.ContentParser;
import org.apache.scxml.model.Data;
import org.apache.scxml.model.Datamodel;
import org.apache.scxml.model.ParsedValue;
import org.apache.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataTagHandler extends AbstractFlowTagHandler<Data> {

    protected final TagAttribute id;
    protected final TagAttribute src;
    protected final TagAttribute expr;

    private ParsedValue staticValue;
    private boolean resolved;

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

        if (isProductionMode(ctx) && staticValue != null) {
            data.setParsedValue(staticValue);
        } else if (expr != null) {
            data.setExpr(expr.getValue());
        } else if (src != null && !src.isLiteral()) {
            data.setSrc(src.getValue());
        } else if (src != null && src.isLiteral()) {
            String resolvedSrc = src.getValue();
            final PathResolver pr = chart.getPathResolver();
            if (pr != null) {
                resolvedSrc = pr.resolvePath(resolvedSrc);
            }
            staticValue = getParsedValue(ctx, parent, resolvedSrc);
            data.setParsedValue(staticValue);
        } else if (!resolved || !isProductionMode(ctx)) {
            staticValue = getBodyValue(ctx, parent);
            data.setParsedValue(staticValue);
        }
        resolved = true;

        Datamodel datamodel = (Datamodel) parentElement;
        datamodel.addData(data);

    }
    
    private ParsedValue getParsedValue(FaceletContext ctx, UIComponent parent, String url) throws IOException {
        ParsedValue result = null;
        try {
            FacesContext fc = ctx.getFacesContext();
            URL resource = fc.getExternalContext().getResource(url);
            result = ContentParser.parseResource(resource);
        } catch (IOException e) {
            throw new TagException(this.tag,
                    String.format("can not build data %s.", Util.getErrorMessage(e)));
        }
        return result;
    }

    private ParsedValue getBodyValue(FaceletContext ctx, UIComponent parent) throws IOException {
        ParsedValue result = null;
        UIPanel panel = new UIPanel();
        try {
            parent.getChildren().add(panel);
            nextHandler.apply(ctx, panel);

            String body = null;
            for (UIComponent child : panel.getChildren()) {
                if (child instanceof UIInstructions) {
                    UIInstructions uii = (UIInstructions) child;
                    String sbody = ContentParser.trimContent(uii.toString().trim());
                    boolean script = false;
                    int ind = sbody.indexOf("<script");
                    if (ind >= 0) {
                        ind = sbody.indexOf(">", ind);
                        if (ind >= 0) {
                            script = true;
                            sbody = sbody.substring(ind + 1).trim();
                        }
                        ind = sbody.lastIndexOf("</script");
                        if (ind >= 0) {
                            sbody = sbody.substring(0, ind).trim();
                        }
                    }

                    if (script) {
                        if (!(sbody.startsWith("{") || sbody.startsWith("["))) {
                            sbody = "{" + sbody + "}";
                        }
                    }

                    if (sbody.startsWith("<xml") && sbody.endsWith("</xml>")) {
                        sbody = "<?xml version=\"1.0\"?>" + sbody;
                    }

                    body = sbody;
                    break;
                }
            }

            if (body != null) {
                result = ContentParser.parseContent(body);
            }
        } catch (IOException e) {
            throw new TagException(this.tag,
                    String.format("can not build body. (%s)", Util.getErrorMessage(e)));
        } finally {
            parent.getChildren().remove(panel);
        }
        return result;
    }

}
