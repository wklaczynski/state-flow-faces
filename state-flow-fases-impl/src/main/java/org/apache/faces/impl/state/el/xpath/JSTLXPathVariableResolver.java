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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.context.FacesContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class JSTLXPathVariableResolver implements XPathVariableResolver {

    private final ELContext context;

    public JSTLXPathVariableResolver(ELContext context) {
        this.context = context;
    }

    @Override
    public Object resolveVariable(QName qname) throws NullPointerException {

        Object varObject = null;

        if (qname == null) {
            throw new NullPointerException("Cannot resolve null variable");
        }

        String namespace = qname.getNamespaceURI();
        String prefix = qname.getPrefix();
        String localName = qname.getLocalPart();

        try {
            varObject = getVariableValue(namespace, prefix, localName);
        } catch (UnresolvableException ue) {
            System.out.println("JSTLXpathVariableResolver.resolveVariable threw UnresolvableException: " + ue);
        }

        return varObject;
    }

    protected Object getVariableValue(String namespace, String prefix, String localName) throws UnresolvableException {
        if (namespace == null || namespace.equals("")) {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExpressionFactory ef = fc.getApplication().getExpressionFactory();
            ValueExpression ve = ef.createValueExpression(context, localName, Object.class);
            Object value = ve.getValue(context);
            return notNull(value, namespace, localName);
        } else {
            throw new UnresolvableException("$" + namespace + ":" + localName);
        }
    }

    private Object notNull(Object o, String namespace, String localName) throws UnresolvableException {
        if (o == null) {
            throw new UnresolvableException("$" + (namespace == null ? "" : namespace + ":") + localName);
        }
        return o;
    }

}
