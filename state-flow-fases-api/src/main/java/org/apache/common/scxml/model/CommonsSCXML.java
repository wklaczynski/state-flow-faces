/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.common.scxml.model;

import java.util.HashMap;
import org.apache.common.scxml.SCXMLConstants;


/**
 * A convenient SCXML instance with the {@link org.apache.commons.scxml2.SCXMLConstants#XMLNS_COMMONS_SCXML} namespace
 * pre-configured.
 * <p>
 * This custom SCXML instance can be used when constructing SCXML instances through Java which uses Commons SCXML
 * custom actions, like {@link Var}, which then can be written with {@link SCXMLWriter}
 * without needing to wrap them in a {@link CustomActionWrapper},</p>
 */
public class CommonsSCXML extends SCXML {

    public CommonsSCXML() {
        setNamespaces(new HashMap<>());
        getNamespaces().put(SCXMLConstants.XMLNS_COMMONS_SCXML_PREFIX, SCXMLConstants.XMLNS_COMMONS_SCXML);
    }
}
