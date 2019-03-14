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
package org.apache.common.faces.impl.state.parser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.apache.common.faces.state.scxml.io.ContentParser;
import org.apache.common.faces.state.scxml.model.JsonValue;
import org.apache.common.faces.state.scxml.model.ParsedValue;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowJsonContentParser extends ContentParser {

    /**
     * Jackson JSON ObjectMapper
     */
    private final ObjectMapper jsonObjectMapper;

    /**
     *
     */
    public StateFlowJsonContentParser() {
        super();
        this.jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        jsonObjectMapper.configure(JsonParser.Feature.ALLOW_YAML_COMMENTS, true);
    }

    @Override
    public String getType() {
        return "json";
    }

    @Override
    public boolean isSupportedContent(String content) {
        return hasJsonSignature(content);
    }

    @Override
    public ParsedValue parse(String source) throws IOException {
        return new JsonValue(parseJson(source), false);
    }

    @Override
    public String toString(Object source) throws IOException {
        return toJson(source);
    }
    
    
    /**
     * Parse and map JSON string to 'raw' Java Objects: object -> LinkedHashMap,
     * array -> ArrayList
     *
     * @param jsonString JSON string to parse
     * @return 'raw' mapped Java Object for JSON string
     * @throws IOException In case of parsing exceptions
     */
    public Object parseJson(final String jsonString) throws IOException {
        return jsonObjectMapper.readValue(jsonString, Object.class);
    }

    /**
     * Transforms a jsonObject to a json String
     *
     * @param jsonObject object to transform
     * @return json string
     * @throws IOException
     */
    public String toJson(final Object jsonObject) throws IOException {
        return jsonObjectMapper.writeValueAsString(jsonObject);
    }

}
