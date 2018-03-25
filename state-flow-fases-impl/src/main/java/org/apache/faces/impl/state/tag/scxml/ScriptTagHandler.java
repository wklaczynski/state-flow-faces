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
import org.apache.scxml.model.Finalize;
import org.apache.scxml.model.If;
import org.apache.scxml.model.OnEntry;
import org.apache.scxml.model.OnExit;
import org.apache.scxml.model.Transition;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.apache.faces.impl.state.tag.AbstractFlowTagHandler;
import org.apache.faces.impl.state.utils.Util;
import org.apache.scxml.PathResolver;
import org.apache.scxml.model.SCXML;
import org.apache.scxml.model.Script;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ScriptTagHandler extends AbstractFlowTagHandler<Script> {

    protected final TagAttribute src;

    private String staticValue;
    private boolean resolved;

    public ScriptTagHandler(TagConfig config) {
        super(config, Script.class);

        in("scxml", SCXML.class);
        in("onentry", OnEntry.class);
        in("onexit", OnExit.class);
        in("transition", Transition.class);
        in("finalize", Finalize.class);
        in("if", If.class);

        top("onentry", OnEntry.class);
        top("onexit", OnExit.class);
        top("transition", Transition.class);
        top("finalize", Finalize.class);
        top("finalize", Finalize.class);

        this.src = this.getAttribute("src");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent, SCXML chart, Object parentElement) throws IOException {
        if (parentElement instanceof SCXML) {
            SCXML scxml = (SCXML) parentElement;
            if (scxml.getGlobalScript() != null) {
                throw new TagException(this.tag, "already defined in this element!");
            }
        }

        Script action = new Script();
        decorate(ctx, parent, action);

        if (isProductionMode(ctx) && staticValue != null) {
            action.setScript(staticValue);
        } else if (src != null && !src.isLiteral()) {
            action.setSrc(src.getValue());
        } else if (src != null && src.isLiteral()) {
            String resolvedSrc = src.getValue();
            final PathResolver pr = chart.getPathResolver();
            if (pr != null) {
                resolvedSrc = pr.resolvePath(resolvedSrc);
            }
            staticValue = getParsedValue(ctx, parent, resolvedSrc);
            action.setScript(staticValue);
        } else if (!resolved || !isProductionMode(ctx)) {
            staticValue = getBodyValue(ctx, parent);
            action.setScript(staticValue);
        }
        resolved = true;

        applyNext(ctx, parent, action);

        if (parentElement instanceof SCXML) {
            SCXML scxml = (SCXML) parentElement;
            action.setGlobalScript(true);
            scxml.setGlobalScript(action);
        } else {
            action.setGlobalScript(false);
            addAction(ctx, parent, action);
        }

    }

    private String getParsedValue(FaceletContext ctx, UIComponent parent, String url) throws IOException {
        String result = null;
        try {
            FacesContext fc = ctx.getFacesContext();
            URL resource = fc.getExternalContext().getResource(url);
            result = Util.readResource(resource);
        } catch (IOException e) {
            throw new TagException(this.tag,
                    String.format("can not build data %s.", Util.getErrorMessage(e)));
        }
        return result;
    }

    private String getBodyValue(FaceletContext ctx, UIComponent parent) throws IOException {
        String result = null;
        UIPanel panel = new UIPanel();
        try {
            parent.getChildren().add(panel);
            nextHandler.apply(ctx, panel);

            String body = null;
            for (UIComponent child : panel.getChildren()) {
                if (child instanceof UIInstructions) {
                    UIInstructions uii = (UIInstructions) child;
                    String sbody = Util.trimContent(uii.toString().trim());
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
                        body = sbody;
                    }
                    break;
                }
            }

            if (body != null) {
                result = body;
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
