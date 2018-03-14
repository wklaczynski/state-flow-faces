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
