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

import com.sun.faces.renderkit.RenderKitUtils;
import java.util.Map;
import java.util.UUID;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.render.ResponseStateManager;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORYGINAL_SCXML_DEFAULT_SUFIX;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORGINAL_SCXML_SUFIX;

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
        String[] defaultsuffixes = webConfig.getOptionValues(ORGINAL_SCXML_SUFIX, " ");
        for (String suffix : defaultsuffixes) {
            if (viewId.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    private String getMatchedWithOryginalSuffix(String viewId) {
        String[] defaultsuffixes = webConfig.getOptionValues(ORGINAL_SCXML_SUFIX, " ");
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
                Object[] rawState = (Object[]) parent.saveView(context);

                UIViewRoot viewRoot = context.getViewRoot();
                String executorId = null;

                if (viewRoot != null) {
                    executorId = (String) viewRoot.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
                }

                if (executorId != null) {
                    if (rawState != null) {
                        Map<String, Object> state = (Map<String, Object>) rawState[1];
                        if (state != null) {
                            state.put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
                        }
                    }
                }
                return rawState;
            }

            @Override
            public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
                String executorId = null;
                
                ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
                Object[] rawState = (Object[]) rsm.getState(context, viewId);
                if (rawState != null) {
                    Map<String, Object> state = (Map<String, Object>) rawState[1];
                    if (state != null) {
                        executorId = (String) state.get(FACES_EXECUTOR_VIEW_ROOT_ID);
                    }
                }
                if (executorId == null) {
                    executorId = UUID.randomUUID().toString();
                }
                UIViewRoot viewRoot = parent.restoreView(context, viewId, renderKitId);

                if (executorId != null) {
                    viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
                }

                return viewRoot;
            }
        };
    }

}
