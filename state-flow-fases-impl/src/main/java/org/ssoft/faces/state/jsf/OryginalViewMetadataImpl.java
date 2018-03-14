/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.jsf;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.state.ModelException;
import javax.faces.state.ModelFileNotFoundException;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.model.StateChart;
import javax.faces.view.ViewMetadata;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class OryginalViewMetadataImpl extends ViewMetadata {
    
    private final String viewId;

    public OryginalViewMetadataImpl(String viewId) {
        this.viewId = viewId;
    }

    @Override
    public String getViewId() {
        return viewId;
    }

    @Override
    public UIViewRoot createMetadataView(FacesContext context) {
        UIViewRoot result = null;
        UIViewRoot currentViewRoot = context.getViewRoot();
        Map<String, Object> currentViewMapShallowCopy = Collections.emptyMap();
        
        try {
            context.setProcessingEvents(false);
            
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
            result = context.getViewRoot();
            
            if (null != currentViewRoot) {
                Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                if (null != currentViewMap && !currentViewMap.isEmpty()) {
                    currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                    Map<String, Object> resultViewMap = result.getViewMap(true);
                    resultViewMap.putAll(currentViewMapShallowCopy);
                }
            }
            
            // Only replace the current context's UIViewRoot if there is 
            // one to replace.
            if (null != currentViewRoot) {
                // This clear's the ViewMap of the current UIViewRoot before
                // setting the argument as the new UIViewRoot.
                context.setViewRoot(result);
            }
            
            
        
        } catch (ModelFileNotFoundException ffnfe) {
            try {
                context.getExternalContext().responseSendError(404, ffnfe.getMessage());
            } catch(IOException ioe) {}
            context.responseComplete();
        } catch (ModelException ioe) {
            throw new FacesException(ioe);
        } finally {
            context.setProcessingEvents(true);
            if (null != currentViewRoot) {
                context.setViewRoot(currentViewRoot);
                if (!currentViewMapShallowCopy.isEmpty()) {
                    currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                    currentViewMapShallowCopy.clear();
                }
            }
            
        }
        
        return result;
    }
    
}
