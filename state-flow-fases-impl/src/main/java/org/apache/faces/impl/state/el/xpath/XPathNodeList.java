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
package org.apache.faces.impl.state.el.xpath;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class XPathNodeList extends AbstractList<Node> {

    List<Node> nodes;

    public XPathNodeList(NodeList nl) {
        nodes = new ArrayList<>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node currNode = nl.item(i);
            nodes.add(i, currNode);
        }
    }

    public XPathNodeList(Node n) {
        nodes = new ArrayList<>();
        nodes.add(n);
    }

    public XPathNodeList() {
        nodes = new ArrayList<>();
    }
    
    public XPathNodeList(Object o) {
        nodes = new ArrayList<>();
        if (o instanceof NodeList) {
            NodeList nl = (NodeList) o;
            for (int i = 0; i < nl.getLength(); i++) {
                Node currNode = nl.item(i);
                nodes.add(i, currNode);
            }
        } else if (o instanceof Node) {
            nodes.add((Node) o);
        }
    }

    @Override
    public Node get(int index) {
        return nodes.get(index);
    }

    @Override
    public int size() {
        return nodes.size();
    }

    @Override
    public void add(int index, Node element) {
        nodes.add(index, element);
    }

    @Override
    public boolean remove(Object o) {
        return nodes.remove(o);
    }

    @Override
    public Node remove(int index) {
        return nodes.remove(index);
    }

    
    

}
