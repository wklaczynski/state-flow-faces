/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.jsf;

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
            return new OryginalViewMetadataImpl(viewId);
        } else {
            return new BasicViewMetadataImpl(wrapped.getViewMetadata(context, viewId));
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
