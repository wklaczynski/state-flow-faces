package org.ssoft.faces.state;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
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

    private final PathResolver parent;

    protected URL baseURL = null;
    private final String dir;

    public FacesURLResolver(PathResolver parent, final URL baseURL) {
        this.parent = parent;
        this.baseURL = baseURL;
        dir = parent.resolvePath(baseURL);
    }

    @Override
    public String resolvePath(final String path) {
        try {
            if (path.startsWith("/")) {
                String result = dir + path;
                return result;
            } else {
                URL combined = new URL(baseURL, path);
                String escapedBaseURL = baseURL.getFile();
                String result = dir + combined.getFile().replaceFirst(escapedBaseURL, "");
                return result;
            }
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public URL resolveURL(String path) {
        return parent.resolveURL(resolvePath(path));
    }

    @Override
    public String resolvePath(URL url) {
        return parent.resolvePath(url);
    }

    @Override
    public PathResolver getResolver(final String path) {
        try {
            URL url = resolveURL(path);
            File file = new File(url.getPath());
            if (file.isFile()) {
                String ppath = file.getParent();
                if (!ppath.endsWith("/")) {
                    ppath += "/";
                }
                url = new URL(url.getProtocol(), url.getHost(), url.getPort(), ppath);
            }
            return new FacesURLResolver(this, url);
        } catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

}
