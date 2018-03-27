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
package org.apache.common.faces.impl.state.facelets;

import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ScxmlViewMetadataImpl extends ViewMetadata {

    private final ViewDeclarationLanguage vdl;
    private final String viewId;

    public ScxmlViewMetadataImpl(ViewDeclarationLanguage vdl, String viewId) {
        this.viewId = viewId;
        this.vdl = vdl;
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    protected UIViewRoot createView(FacesContext context) throws IOException {
        UIViewRoot viewRoot = vdl.createView(context, viewId);
        vdl.buildView(context, viewRoot);
        return viewRoot;
    }

    @Override
    public UIViewRoot createMetadataView(FacesContext context) {
        try {
            UIViewRoot viewRoot = createView(context);
            return viewRoot;
        } catch (IOException e) {
            throw new FacesException(e);
        }
    }

}
