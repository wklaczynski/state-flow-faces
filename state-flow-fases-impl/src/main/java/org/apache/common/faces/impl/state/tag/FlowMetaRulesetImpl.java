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
package org.apache.common.faces.impl.state.tag;

import java.beans.IntrospectionException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRule;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.Metadata;
import javax.faces.view.facelets.MetadataTarget;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.impl.state.log.FlowLogger;
import org.apache.common.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowMetaRulesetImpl extends MetaRuleset {

    private final static Logger LOGGER = FlowLogger.FLOW.getLogger();
    private final static Map<Class, WeakReference<MetadataTarget>> metadata
            = Collections.synchronizedMap(new WeakHashMap<Class, WeakReference<MetadataTarget>>());

    private final Tag tag;
    private final Class type;
    private final Map<String, TagAttribute> attributes;
    private final List<Metadata> mappers;
    private final List<MetaRule> rules;

    // ------------------------------------------------------------ Constructors
    public FlowMetaRulesetImpl(Tag tag, Class<?> type) {

        this.tag = tag;
        this.type = type;
        this.attributes = new HashMap<>();
        this.mappers = new ArrayList<>();
        this.rules = new ArrayList<>();

        // setup attributes
        TagAttribute[] attrs = this.tag.getAttributes().getAll();
        for (TagAttribute attr : attrs) {
            if (attr.getLocalName().equals("class")) {
                attributes.put("styleClass", attr);
            } else {
                attributes.put(attr.getLocalName(), attr);
            }
        }

        // add default rules
        this.rules.add(BeanPropertyTagRule.Instance);

    }

    // ---------------------------------------------------------- Public Methods
    @Override
    public MetaRuleset ignore(String attribute) {

        Util.notNull("attribute", attribute);
        this.attributes.remove(attribute);
        return this;

    }

    @Override
    public MetaRuleset alias(String attribute, String property) {

        Util.notNull("attribute", attribute);
        Util.notNull("property", property);
        TagAttribute attr = this.attributes.remove(attribute);
        if (attr != null) {
            this.attributes.put(property, attr);
        }
        return this;

    }

    @Override
    public MetaRuleset add(Metadata mapper) {

        Util.notNull("mapper", mapper);
        if (!this.mappers.contains(mapper)) {
            this.mappers.add(mapper);
        }
        return this;

    }

    @Override
    public MetaRuleset addRule(MetaRule rule) {

        Util.notNull("rule", rule);
        this.rules.add(rule);
        return this;

    }

    @Override
    public Metadata finish() {

        if (!this.attributes.isEmpty()) {
            if (this.rules.isEmpty()) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    for (Iterator<TagAttribute> itr = this.attributes.values().iterator(); itr.hasNext();) {
                        LOGGER.log(Level.SEVERE, "{0} Unhandled by MetaTagHandler for type {1}", new Object[]{itr.next(), this.type.getName()});
                    }
                }
            } else {
                MetadataTarget target = this.getMetadataTarget();
                // now iterate over attributes
                int ruleEnd = this.rules.size() - 1;
                for (Map.Entry<String, TagAttribute> entry : attributes.entrySet()) {
                    Metadata data = null;
                    int i = ruleEnd;
                    while (data == null && i >= 0) {
                        MetaRule rule = this.rules.get(i);
                        data = rule.applyRule(entry.getKey(), entry.getValue(), target);
                        i--;
                    }
                    if (data == null) {
                        if (LOGGER.isLoggable(Level.SEVERE)) {
                            LOGGER.log(Level.SEVERE, "{0} Unhandled by MetaTagHandler for type {1}", new Object[]{entry.getValue(), this.type.getName()});
                        }
                    } else {
                        this.mappers.add(data);
                    }
                }
            }
        }

        if (this.mappers.isEmpty()) {
            return NONE;
        } else {
            return new FlowMetadataImpl(this.mappers.toArray(new Metadata[this.mappers.size()]));
        }

    }

    @Override
    public MetaRuleset ignoreAll() {

        this.attributes.clear();
        return this;

    }

    // ------------------------------------------------------- Protected Methods
    protected MetadataTarget getMetadataTarget() {
        WeakReference<MetadataTarget> metaRef = metadata.get(type);
        MetadataTarget meta = metaRef == null ? null : metaRef.get();
        if (meta == null) {
            try {
                meta = new FlowMetadataTargetImpl(type);
            } catch (IntrospectionException e) {
                throw new TagException(this.tag,
                        "Error Creating TargetMetadata", e);
            }
            metadata.put(type, new WeakReference<>(meta));
        }
        return meta;

    }

    // --------------------------------------------------------- Private Methods
    private final static Metadata NONE = new Metadata() {

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            // do nothing
        }

    };

    public static class FlowMetadataImpl extends Metadata {

        private final Metadata[] mappers;
        private final int size;

        public FlowMetadataImpl(Metadata[] mappers) {
            this.mappers = mappers;
            this.size = mappers.length;
        }

        @Override
        public void applyMetadata(FaceletContext ctx, Object instance) {
            for (int i = 0; i < size; i++) {
                this.mappers[i].applyMetadata(ctx, instance);
            }
        }

    }
}
