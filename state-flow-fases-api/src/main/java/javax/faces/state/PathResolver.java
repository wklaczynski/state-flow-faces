/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.net.URL;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface PathResolver {

    /**
     * Resolve this context sensitive path to an absolute URL.
     *
     * @param path Context sensitive path, can be a relative URL
     * @return Resolved path (an absolute URL) or <code>null</code>
     */
    String resolvePath(String path);

    /**
     * Get a PathResolver rooted at this context sensitive path.
     *
     * @param path Context sensitive path, can be a relative URL
     * @return Returns a new resolver rooted at ctxPath
     */
    PathResolver getResolver(String path);

    public URL resolveURL(String path);
    
    String resolvePath(URL url);

}
