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
package org.ssoft.faces.state.tag;

import java.io.IOException;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;
import org.w3c.dom.Node;
import javax.scxml.model.Data;
import javax.scxml.model.Datamodel;
import javax.scxml.model.NodeValue;
import javax.scxml.model.SCXML;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DataTagHandler extends AbstractFlowTagHandler<Data> {

    protected final TagAttribute id;
    protected final TagAttribute src;
    protected final TagAttribute expr;

    private Node staticNode;

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
        
//        if (isProductionMode(ctx) && staticNode != null ) {
//            data.setParsedValue(new NodeValue(staticNode));
//        } else if (srcobj != null && srcobj instanceof String) {
//            FacesContext fc = ctx.getFacesContext();
//            ResourceHandler rh = fc.getApplication().getResourceHandler();
//
//            Resource res;
//
//            String resourceId = (String) srcobj;
//            String libraryName = null;
//            String resourceName = null;
//
//            int end = 0, start = 0;
//            if (-1 != (end = resourceId.lastIndexOf(":"))) {
//                resourceName = resourceId.substring(end + 1);
//                if (-1 != (start = resourceId.lastIndexOf(":", end - 1))) {
//                    libraryName = resourceId.substring(start + 1, end);
//                } else {
//                    libraryName = resourceId.substring(0, end);
//                }
//            }
//
//            if (libraryName != null) {
//                res = rh.createResource(resourceName, libraryName);
//                if (res == null) {
//                    String errmsg = String.format("%s (resource not found)", src.getValue());
//                    buildex(errmsg);
//                }
//
//                if (!res.getContentType().equals("application/xml")) {
//                    String errmsg = String.format("%s (resource not xml content type)", src.getValue());
//                    buildex(errmsg);
//                }
//                staticNode = StateFlowHelper.buildContentFromStream(fc, res.getInputStream());
//                
//                
//
//            } else {
//                PathResolver pr = getElement(parent, PathResolver.class);
//                if (pr != null) {
//                    resourceId = pr.resolvePath(fc, resourceId);
//                }
//                String mimeType = fc.getExternalContext().getMimeType(resourceId);
//                if (!mimeType.equals("application/xml")) {
//                    String errmsg = String.format("%s (resource not xml content type)", src.getValue());
//                    buildex(errmsg);
//                }
//                staticNode = StateFlowHelper.buildContentFromPath(fc, resourceId);
//            }
//
//            data.setNode(staticNode);
//        }

        Datamodel datamodel = (Datamodel) parentElement;
        datamodel.addData(data);

    }

    private void buildex(String errmsg) {
        throw new TagException(this.tag,
                String.format("can not build data %s.", errmsg));
    }

}
