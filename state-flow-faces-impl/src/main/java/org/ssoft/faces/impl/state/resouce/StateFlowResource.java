/*
 * Copyright 2019 waldek.
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
package org.ssoft.faces.impl.state.resouce;

import jakarta.faces.application.Resource;
import jakarta.faces.application.ResourceWrapper;
import jakarta.faces.context.FacesContext;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.SCXMLExecutor;

/**
 *
 * @author waldek
 */
public class StateFlowResource extends ResourceWrapper {

    private final Resource wrapped;
    private String queyParams;


    public StateFlowResource(Resource wrapped) {
        super();
        this.wrapped = wrapped;
        
        queyParams = "";
        SCXMLExecutor executor = getExecutor();
        if(executor != null) {
            queyParams = "&exid=" + executor.getId();
        }
    }

    @Override
    public Resource getWrapped() {
        return wrapped;
    }
    
    @Override
    public String getRequestPath() {
        return super.getRequestPath() + queyParams;
    }

    @Override
    public String getContentType() {
        return getWrapped().getContentType();
    }

    @Override
    public String getLibraryName() {
        return getWrapped().getLibraryName();
    }

    @Override
    public String getResourceName() {
        return getWrapped().getResourceName();
    }

    @Override
    public void setContentType(final String contentType) {
        getWrapped().setContentType(contentType);
    }

    @Override
    public void setLibraryName(final String libraryName) {
        getWrapped().setLibraryName(libraryName);
    }

    @Override
    public void setResourceName(final String resourceName) {
        getWrapped().setResourceName(resourceName);
    }

    @Override
    public String toString() {
        return getWrapped().toString();
    }
    
    private SCXMLExecutor getExecutor() {
        FacesContext context = FacesContext.getCurrentInstance();
        SCXMLExecutor result = getExecutor(context);
        return result;
    }

    private static SCXMLExecutor getExecutor(FacesContext context) {
        StateFlowHandler flowHandler = StateFlowHandler.getInstance();
        if (null == flowHandler) {
            return null;
        }

        SCXMLExecutor result = flowHandler.getCurrentExecutor(context);
        return result;

    }
    
    
}
