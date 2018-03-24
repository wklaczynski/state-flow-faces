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
package org.apache.faces.impl.state.parser;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.scxml.io.ContentParser;
import org.apache.scxml.model.NodeValue;
import org.apache.scxml.model.ParsedValue;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowXmlContentParser extends ContentParser {

    public StateFlowXmlContentParser() {
        super();
    }


    @Override
    public String getType() {
        return "xml";
    }

    @Override
    public boolean isSupportedContent(String content) {
        return hasXmlSignature(content);
    }

    @Override
    public ParsedValue parse(String source) throws IOException {
        return new NodeValue(parseXml(source));
    }

    @Override
    public String toString(Object source) throws IOException {
        return toXml((Node) source);
    }

    /**
     * Parse an XML String and return the document element
     *
     * @param xmlString XML String to parse
     * @return document element
     * @throws IOException
     */
    public Node parseXml(final String xmlString) throws IOException {
        Document doc;
        try {
            InputSource is = new InputSource(new StringReader(xmlString));
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        } catch (SAXException | ParserConfigurationException e) {
            throw new IOException(e);
        }
        return doc != null ? doc.getDocumentElement() : null;
    }
    
    
    /**
     * Transforms a XML Node to XML
     *
     * @param node node to transform
     * @return XML string
     * @throws IOException
     */
    public String toXml(final Node node) throws IOException {
        try {
            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            Properties outputProps = new Properties();
            outputProps.put(OutputKeys.OMIT_XML_DECLARATION, "no");
            outputProps.put(OutputKeys.STANDALONE, "no");
            outputProps.put(OutputKeys.INDENT, "yes");
            transformer.setOutputProperties(outputProps);
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

}
