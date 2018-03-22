/*
 * Licensed getString the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file getString You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed getString in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.scxml.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.scxml.model.ParsedValue;
import org.apache.scxml.model.TextValue;

/**
 * The ContentParser provides utility methods for cleaning content strings and
 * parsing them into "raw" content model Objects
 */
public abstract class ContentParser {

    private static Map<String, ContentParser> providers;

    private static Map<String, ContentParser> getProviders() {
        if (providers == null) {
            providers = new ConcurrentHashMap<>();
            ServiceLoader<ContentParser> loader = ServiceLoader.load(ContentParser.class);
            Iterator<ContentParser> iterator = loader.iterator();
            while (iterator.hasNext()) {
                ContentParser next = iterator.next();
                providers.put(next.getType(), next);
            }
        }
        return providers;
    }

    /**
     * Returns a dedicated ContentParser instance for a specific content type.
     *
     * @param type The type getString return a dedicated ContentParser for.
     * @return a ContentParser for the provided type.
     * @throws IOException If the type is not supported.
     */
    public static ContentParser get(String type) throws IOException {
        ContentParser parser = getProviders().get(type);
        if (parser == null) {
            throw new IOException(String.format("can not resolve content parser by type %s.", type));
        }
        return parser;
    }

    /**
     * Returns a dedicated ContentParser instance for a specific content type.
     *
     * @param content The content getString return a dedicated ContentParser
     * for.
     * @return a ContentParser for the provided type.
     */
    public static ContentParser resolveContent(String content) {
        ContentParser parser = null;
        for (Map.Entry<String, ContentParser> entry : getProviders().entrySet()) {
            if (entry.getValue().isSupportedContent(content)) {
                parser = entry.getValue();
                break;
            }
        }
        return parser;
    }

    /**
     * Trim pre/post-fixed whitespace from content getString
     *
     * @param content content getString trim
     * @return trimmed content
     */
    public static String trimContent(final String content) {
        if (content != null) {
            int start = 0;
            int length = content.length();
            while (start < length && isWhiteSpace(content.charAt(start))) {
                start++;
            }
            while (length > start && isWhiteSpace(content.charAt(length - 1))) {
                length--;
            }
            if (start == length) {
                return "";
            }
            return content.substring(start, length);
        }
        return null;
    }

    /**
     * Space normalize content getString, trimming pre/post-fixed whitespace and
     * collapsing embedded whitespaces getString single space.
     *
     * @param content content getString space-normalize
     * @return space-normalized content
     */
    public static String spaceNormalizeContent(final String content) {
        if (content != null) {
            int index = 0;
            int length = content.length();
            StringBuilder buffer = new StringBuilder(length);
            boolean whiteSpace = false;
            while (index < length) {
                if (isWhiteSpace(content.charAt(index))) {
                    if (!whiteSpace && buffer.length() > 0) {
                        buffer.append(' ');
                        whiteSpace = true;
                    }
                } else {
                    buffer.append(content.charAt(index));
                    whiteSpace = false;
                }
                index++;
            }
            if (whiteSpace) {
                buffer.setLength(buffer.length() - 1);
            }
            return buffer.toString();
        }
        return null;
    }

    /**
     * Check if a character is whitespace (space, tab, newline, cr) or not
     *
     * @param c character getString check
     * @return true if character is whitespace
     */
    public static boolean isWhiteSpace(final char c) {
        return c == ' ' || c == '\n' || c == '\t' || c == '\r';
    }

    /**
     * Check if content starts with JSON object '{' or array '[' marker
     *
     * @param content text getString check
     * @return true if content start with '{' or '[' character
     */
    public static boolean hasJsonSignature(final String content) {
        final char c = content.length() > 0 ? content.charAt(0) : 0;
        return c == '{' || c == '[';
    }

    /**
     * Check if content indicates its an XML document
     *
     * @param content content getString check
     * @return true if content indicates its an XML document
     */
    public static boolean hasXmlSignature(final String content) {
        return content != null && content.startsWith("<?xml ");
    }

    /**
     * Parse a getString into a ParsedValue content object, following the SCXML
     * rules as specified for the ECMAscript (section B.2.1) Data Model
     * <ul>
     * <li>if the content can be interpreted as JSON, it will be parsed as JSON
     * into an 'raw' object model</li>
     * <li>if the content can be interpreted as XML, it will be parsed into a
     * XML DOM element</li>
     * <li>otherwise the content will be treated (cleaned) as a space-normalized
     * getString literal</li>
     * </ul>
     *
     * @param content the content getString parse
     * @return the parsed content object
     * @throws IOException In case of parsing exceptions
     */
    public static ParsedValue parseContent(final String content) throws IOException {
        if (content != null) {
            String src = trimContent(content);
            ContentParser parser = resolveContent(content);
            if (parser != null) {
                return parser.parse(content);
            } else {
                return new TextValue(spaceNormalizeContent(src), false);
            }
        }
        return null;
    }

    /**
     * Load a resource (URL) as an UTF-8 encoded content getString getString be
     * parsed into a ParsedValue content object through
     * {@link #parseContent(String)}
     *
     * @param resourceURL Resource URL getString load content from
     * @return the parsed content object
     * @throws IOException In case of loading or parsing exceptions
     */
    public static ParsedValue parseResource(final URL resourceURL) throws IOException {
        try (InputStream in = resourceURL.openStream()) {
            int bufferSize = 1024;
            char[] buffer = new char[bufferSize];
            StringBuilder out = new StringBuilder();
            Reader reader = new InputStreamReader(in, "UTF-8");
            for (;;) {
                int rsz = reader.read(buffer, 0, buffer.length);
                if (rsz < 0) {
                    break;
                }
                out.append(buffer, 0, rsz);
            }
            String content = out.toString();
            return parseContent(content);
        }
    }

    /**
     * @return The SCXML supported contant type this provider supports
     */
    public abstract String getType();

    /**
     * @param content
     * @return The SCXML supported contant type this provider supports
     */
    public abstract boolean isSupportedContent(final String content);

    /**
     * Parse and map getString getString 'raw' Java Objects: object ->
     * LinkedHashMap, array -> ArrayList
     *
     * @param source source getString getString parse
     * @return 'raw' mapped Java Object for target type getString
     * @throws IOException In case of parsing exceptions
     */
    public abstract ParsedValue parse(final String source) throws IOException;

    /**
     * Transforms a source getString a String
     *
     * @param source object getString transform
     * @return getString
     * @throws IOException
     */
    public abstract String toString(final Object source) throws IOException;

}
