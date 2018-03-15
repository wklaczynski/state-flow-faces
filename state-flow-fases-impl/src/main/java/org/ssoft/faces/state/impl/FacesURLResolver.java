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
package org.ssoft.faces.state.impl;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import javax.faces.context.FacesContext;
import javax.faces.state.PathResolver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Waldemar Kłaczyński
 */
public class FacesURLResolver implements PathResolver, Serializable {

    private final String root;
    private transient String base;
    private transient URL contextURL;

    public FacesURLResolver(String root) {
        this.root = root;
    }

    @Override
    public String resolvePath(FacesContext context, final String path) {
        try {
            URL resource;
            if (path.startsWith("/")) {
                resource = context.getExternalContext().getResource(path);
            } else {
                if (contextURL == null) {
                    contextURL = context.getExternalContext().getResource(root);
                }
                resource = new URL(contextURL, path);
            }
            return resolvePath(context, resource);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public PathResolver getResolver(FacesContext context, String path) {
        path = resolvePath(context, path);
        try {
            URL url = context.getExternalContext().getResource(path);
            File file = new File(url.getPath());
            if (file.isFile()) {
                String parent = file.getParent();
                if (!parent.endsWith("/")) {
                    parent += "/";
                }
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), parent);
            }
            path = resolvePath(context, url);

            return new FacesURLResolver(path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    private String resolvePath(FacesContext context, URL url) {
        if (base == null) {
            base = context.getExternalContext().getRealPath("/");
        }
        String result = url.getFile().replaceFirst(base, "");
        return result;
    }

}
