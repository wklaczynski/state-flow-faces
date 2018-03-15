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
package javax.faces.state;

import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface PathResolver {

    /**
     * Resolve this context sensitive path to an absolute URL.
     *
     * @param context
     * @param path Context sensitive path, can be a relative URL
     * @return Resolved path (an absolute URL) or <code>null</code>
     */
    String resolvePath(FacesContext context, String path);

    /**
     * Get a PathResolver rooted at this context sensitive path.
     *
     * @param context
     * @param path Context sensitive path, can be a relative URL
     * @return Returns a new resolver rooted at ctxPath
     */
    PathResolver getResolver(FacesContext context, String path);

}
