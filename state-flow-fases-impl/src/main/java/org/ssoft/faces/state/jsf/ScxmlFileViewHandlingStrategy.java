/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.jsf;

import java.beans.BeanInfo;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.state.ModelException;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.model.StateChart;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import org.ssoft.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ScxmlFileViewHandlingStrategy extends ViewDeclarationLanguage {

    private static final String LOCK = "org.apache.faces.state.ScxmlFileViewHandlingStrategy:Lock";

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();

    public ScxmlFileViewHandlingStrategy() {
        super();
    }

    private boolean isLocked(FacesContext context) {
        if (context.getAttributes().containsKey(LOCK)) {
            return (Boolean) context.getAttributes().get(LOCK);
        } else {
            return false;
        }
    }

    private void lock(FacesContext context) {
        context.getAttributes().put(LOCK, true);
    }

    private void unlock(FacesContext context) {
        context.getAttributes().remove(LOCK);
    }

    @Override
    public UIViewRoot createView(FacesContext context, String viewId) {
        try {
            StateFlowHandler flowHandler = StateFlowHandler.getInstance();

            Flash flash = context.getExternalContext().getFlash();
            Map<String, Object> params = new LinkedHashMap<>();
            Set<String> keySet = flash.keySet();
            for (String key : keySet) {
                params.put(key, flash.get(key));
            }
            Map<String, String> pmap = context.getExternalContext().getRequestParameterMap();
            for (String key : pmap.keySet()) {
                params.put(key, pmap.get(key));
            }

            UIViewRoot scxmlRoot = new UIViewRoot();
            scxmlRoot.setViewId(viewId);
            UIViewRoot oldRoot = context.getViewRoot();
            try {
                if (context.getViewRoot() == null) {
                    context.setViewRoot(scxmlRoot);
                }

                StateChart stateFlow = flowHandler.createStateMachine(context, viewId);

                flowHandler.startExecutor(context, stateFlow, params, false);
            } finally {
                if (oldRoot != null) {
                    context.setViewRoot(oldRoot);
                }
            }
            UIViewRoot viewRoot = context.getViewRoot();
            return viewRoot;
        } catch (ModelException ex) {
            throw new FacesException(ex);
        }
    }

    @Override
    public BeanInfo getComponentMetadata(FacesContext context, Resource componentResource) {
        return null;
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        return new ScxmlViewMetadataImpl(viewId);
    }

    @Override
    public Resource getScriptComponentResource(FacesContext context, Resource componentResource) {
        return null;
    }

    @Override
    public void buildView(FacesContext context, UIViewRoot root) throws IOException {

    }

    @Override
    public void renderView(FacesContext context, UIViewRoot view) throws IOException {

    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        return null;
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        return null;
    }
}
