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
package org.ssoft.faces.state.el.xpath;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;
import javax.xml.namespace.NamespaceContext;
import org.w3c.dom.Document;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class JSTLXPathNamespaceContext implements NamespaceContext {

    protected Map< String, String> map = new HashMap<>();
    private final Document document;

    public JSTLXPathNamespaceContext(Document document) {
        this.document = document;
    }

    public void addNamespace(String namespaceURI, String prefix) {
        map.put(prefix, namespaceURI);
    }

    public void removeNamespace(String prefix) {
        map.remove(prefix);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException(prefix);
        }
        String result = map.get(prefix);
        if (result != null) {
            return result;
        }
        

        if (DEFAULT_NS_PREFIX.equals(prefix)) {
            if (document != null) {
                result = document.lookupNamespaceURI(null);
            }
        } else if (document != null) {
            result = document.lookupNamespaceURI(prefix);
        }
        if (result != null) {
            return result;
        }
        
        if (XML_NS_PREFIX.equals(prefix)) {
            result = XML_NS_URI;
        }
        if (XMLNS_ATTRIBUTE.equals(prefix)) {
            result = XMLNS_ATTRIBUTE_NS_URI;
        }

        if (result != null) {
            return result;
        } else {
            return NULL_NS_URI;
        }

    }

    protected String commonPrefixCheck(String namespaceURI, boolean defaultNS) {
        if (namespaceURI == null) {
            throw new IllegalArgumentException(namespaceURI);
        }
        if (defaultNS && getNamespaceURI(DEFAULT_NS_PREFIX).equals(namespaceURI)) {
            return DEFAULT_NS_PREFIX;
        }
        if (XML_NS_URI.equals(namespaceURI)) {
            return XML_NS_PREFIX;
        }
        if (XMLNS_ATTRIBUTE_NS_URI.equals(namespaceURI)) {
            return XMLNS_ATTRIBUTE;
        }
        return null;
    }

    @Override
    public String getPrefix(String namespaceURI) {
        String result = commonPrefixCheck(namespaceURI, true);
        if (result == null) {
            Iterator<String> i = prefixIterator(namespaceURI);
            if (i.hasNext()) {
                return i.next();
            }
        }
        return null;
    }

    @Override
    public Iterator< String> getPrefixes(String namespaceURI) {
        String result = commonPrefixCheck(namespaceURI, false);
        if (result == null && getNamespaceURI(DEFAULT_NS_PREFIX).equals(namespaceURI)) {
            result = DEFAULT_NS_PREFIX;
        }
        if (result != null) {
            return Collections.singleton(result).iterator();
        }
        return prefixIterator(namespaceURI);
    }

    protected Iterator< String> prefixIterator(final String namespaceURI) {
        return new Iterator< String>() {
            private final Iterator< Map.Entry< String, String>> iterator = map.entrySet().iterator();

            @Override
            public boolean hasNext() {
                while (iterator.hasNext()) {
                    if (namespaceURI.equals(iterator.next().getValue())) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String next() {
                return iterator.next().getKey();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
