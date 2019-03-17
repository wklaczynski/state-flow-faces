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
package org.ssoft.faces.impl.state.facelets;

import java.util.Map;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORYGINAL_SCXML_DEFAULT_SUFIX;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORYGINAL_SCXML_SUFIX;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguage extends ViewDeclarationLanguageWrapper {

    /**
     *
     */
    public final ViewDeclarationLanguage wrapped;
    private final StateWebConfiguration webConfig;

    /**
     *
     * @param wrapped
     */
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
            return wrapped.getViewMetadata(context, viewId);
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
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        StateManagementStrategy parent = super.getStateManagementStrategy(context, viewId);

        return new StateManagementStrategy() {
            @Override
            public Object saveView(FacesContext context) {
                String uuid = (String) context.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
                Object[] rawState = (Object[]) parent.saveView(context);
                if (uuid != null) {
                    if (rawState != null) {
                        Map<String, Object> state = (Map<String, Object>) rawState[1];
                        if (state != null) {
                            state.put(FACES_EXECUTOR_VIEW_ROOT_ID, uuid);
                        }
                    }
                }
                return rawState;
            }

            @Override
            public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
                return parent.restoreView(context, viewId, renderKitId);
            }
        };
    }

}
