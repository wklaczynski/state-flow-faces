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
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private transient String contextPath;

    public FacesURLResolver(String root) {
        this.root = root;
    }

    @Override
    public String resolvePath(FacesContext context, String path) {
        if (path.startsWith("/")) {
            path = context.getExternalContext().getRealPath(path);
        } else {
            if (contextPath == null) {
                contextPath = context.getExternalContext().getRealPath(root);
            }
            Path pth = Paths.get(contextPath);
            path = pth.resolve(path).normalize().toString();
        }
        return localPath(context, path);
    }

    @Override
    public PathResolver getResolver(FacesContext context, String path) {
        path = resolvePath(context, path);
        path = context.getExternalContext().getRealPath(path);
        
        Path pth = Paths.get(path);
        File file = pth.toFile();
        if (file.isFile()) {
            String parent = file.getParent();
            if (!parent.endsWith("/")) {
                parent += "/";
            }
            path = parent;
        }
        path = localPath(context, path);
        return new FacesURLResolver(path);
    }

    private String localPath(FacesContext context, String path) {
        if (base == null) {
            base = context.getExternalContext().getRealPath("/");
        }
        String result = path.replaceFirst(base, "");
        return result;
    }

}
