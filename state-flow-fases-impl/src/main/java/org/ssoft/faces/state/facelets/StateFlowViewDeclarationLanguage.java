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
package org.ssoft.faces.state.facelets;

import java.io.IOException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.state.FlowConstants.ORYGINAL_SCXML_DEFAULT_SUFIX;
import static org.ssoft.faces.state.FlowConstants.ORYGINAL_SCXML_SUFIX;
import org.ssoft.faces.state.config.StateWebConfiguration;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguage extends ViewDeclarationLanguageWrapper {

    public final ViewDeclarationLanguage wrapped;
    private final StateWebConfiguration webConfig;

    public StateFlowViewDeclarationLanguage(ViewDeclarationLanguage wrapped) {
        super();
        this.wrapped = wrapped;
        webConfig = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewDeclarationLanguage getWrapped() {
        return wrapped;
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        if (handlesByOryginal(viewId)) {
            return new ScxmlViewMetadataImpl(this, viewId);
        } else {
            return new FaceletViewMetadataImpl(this, wrapped.getViewMetadata(context, viewId));
        }
    }

    private boolean handlesByOryginal(String viewId) {
        return isMatchedWithOryginalSuffix(viewId) ? true : viewId.endsWith(ORYGINAL_SCXML_DEFAULT_SUFIX);
    }

    private boolean isMatchedWithOryginalSuffix(String viewId) {
        String[] defaultsuffixes = webConfig.getOptionValues(ORYGINAL_SCXML_SUFIX, " ");
        for (String suffix : defaultsuffixes) {
            if (viewId.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    private String getMatchedWithOryginalSuffix(String viewId) {
        String[] defaultsuffixes = webConfig.getOptionValues(ORYGINAL_SCXML_SUFIX, " ");
        for (String suffix : defaultsuffixes) {
            if (viewId.endsWith(suffix)) {
                return suffix;
            }
        }

        return null;
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        return wrapped.createView(context, viewId);
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return wrapped.restoreView(context, viewId);
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot root) throws IOException {
        wrapped.buildView(context, root);

    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException {
        wrapped.renderView(context, view);
    }

}
