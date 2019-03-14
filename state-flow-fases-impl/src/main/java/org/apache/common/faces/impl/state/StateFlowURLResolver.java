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
package org.apache.common.faces.impl.state;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.faces.FacesException;
import javax.faces.application.Resource;
import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import org.apache.common.faces.state.scxml.PathResolver;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowURLResolver implements PathResolver, Serializable {

    private final String root;
    private transient String base;
    private transient String contextPath;

    /**
     *
     * @param root
     */
    public StateFlowURLResolver(String root) {
        this.root = root;
    }

    @Override
    @SuppressWarnings("MalformedRegexp")
    public String resolvePath(String path) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (path.startsWith("@")) {
            return path;
        } else if (path.startsWith("#")) {
            return path;
        } else if (path.startsWith("//")) {
            path = path.replace("\\","/").substring(1);
        } else if (path.startsWith("/")) {
            path = context.getExternalContext().getRealPath(path).replace("\\","/");
        } else {
            if (path.contains(":")) {
                String resourceId = (String) path;
                String libraryName = null;
                String resourceName = null;

                int end = 0, start = 0;
                if (-1 != (end = resourceId.lastIndexOf(":"))) {
                    resourceName = resourceId.substring(end + 1);
                    if (-1 != (start = resourceId.lastIndexOf(":", end - 1))) {
                        libraryName = resourceId.substring(start + 1, end);
                    } else {
                        libraryName = resourceId.substring(0, end);
                    }
                }

                Resource res = null;
                if (libraryName != null) {
                    ResourceHandler rh = context.getApplication().getResourceHandler();
                    res = rh.createResource(resourceName, libraryName);
                }
                if (res == null) {
                    throw new FacesException(String.format("(resource not found %s)", path));
                }
                path = res.getURL().getFile().replace("\\","/");
            } else {
                if (contextPath == null) {
                    contextPath = context.getExternalContext().getRealPath(root);
                }
                Path pth = Paths.get(contextPath);
                path = pth.resolve(path).normalize().toString().replace("\\","/");
            }
        }
        return localPath(context, path);
    }

    @Override
    public PathResolver getResolver(String path) {
        path = resolvePath(path);

        FacesContext context = FacesContext.getCurrentInstance();
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
        path = localPath(context, path.replace("\\","/"));
        return new StateFlowURLResolver(path);
    }

    @SuppressWarnings("MalformedRegexp")
    private String localPath(FacesContext context, String path) {
        if (base == null) {
            base = context.getExternalContext().getRealPath("/").replace("\\","/");
        }
        String result = path.replaceFirst(base, "");
        int sep = result.lastIndexOf("/META-INF/resources");
        if(sep > -1) {
            result = result.substring(sep+19);
        }
        return result;
    }

    @Override
    public URL getResource(String path) throws IOException {
        FacesContext context = FacesContext.getCurrentInstance();
        path = resolvePath(path);
        return context.getExternalContext().getResource(path);
    }

}
