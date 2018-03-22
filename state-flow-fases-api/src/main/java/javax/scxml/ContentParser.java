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
package javax.scxml;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import javax.scxml.model.ParsedValue;
import org.w3c.dom.Node;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class ContentParser {

    private static final Map<ContentParser, ContentParser> parsers = new ConcurrentHashMap<>();
    private static boolean isinit = false;
    
    
    /**
     * Load a resource (URL) as an UTF-8 encoded content string to be parsed into a ParsedValue content object through {@link #parseContent(String)}
     * @param path Resource URL to load content from
     * @return the parsed content object
     * @throws IOException In case of loading or parsing exceptions
     */
    public static ParsedValue parse(final String path) throws IOException {
        ParsedValue value = null;
        if(!isinit) {
            init();
        }
        
        for (ContentParser provider : parsers.keySet()) {
            if(provider.suppord(path)) {
                value = provider.parseResource(path);
                break;
            }
        }
        return value;
    }

    /**
     * Load a resource (URL) as an UTF-8 encoded content string to be parsed into a ParsedValue content object through {@link #parseContent(String)}
     * @param node
     * @return the parsed content object
     * @throws IOException In case of loading or parsing exceptions
     */
    public static String changeToXml(Node node) throws IOException {
        String value = null;
        if(!isinit) {
            init();
        }
        
        for (ContentParser provider : parsers.keySet()) {
            if(provider.suppord(node)) {
                value = provider.toXml(node);
                break;
            }
        }
        return value;
    }
    
    
    private static void init() {
        parsers.clear();
        ServiceLoader<ContentParser> loader = ServiceLoader.load(ContentParser.class);
        Iterator<ContentParser> iterator = loader.iterator();
        while (iterator.hasNext()) {
            ContentParser next = iterator.next();
            parsers.put(next, next);
        }
        isinit = true;
    }
    
    
    
    /**
     * Load a resource (URL) as an UTF-8 encoded content string to be parsed into a ParsedValue content object through {@link #parseContent(String)}
     * @param path Resource URL to load content from
     * @return the parsed content object
     * @throws IOException In case of loading or parsing exceptions
     */
    public ParsedValue parseResource(final String path) throws IOException {
        return null;
    }
    
    /**
     * Transforms a XML Node to XML
     * @param node node to transform
     * @return XML string
     * @throws IOException
     */
    public String toXml(final Node node) throws IOException {
        return null;
    }
    
    
    public boolean suppord(final String path) {
        return false;
    }

    public boolean suppord(final Node content) {
        return false;
    }

    
}
