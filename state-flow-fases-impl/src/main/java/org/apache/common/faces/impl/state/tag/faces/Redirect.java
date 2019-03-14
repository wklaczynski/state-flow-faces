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
package org.apache.common.faces.impl.state.tag.faces;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.scxml.ActionExecutionContext;
import org.apache.common.faces.state.scxml.Context;
import org.apache.common.faces.state.scxml.Evaluator;
import org.apache.common.faces.state.scxml.SCXMLConstants;
import org.apache.common.faces.state.scxml.SCXMLExpressionException;
import org.apache.common.faces.state.scxml.model.Action;
import org.apache.common.faces.state.scxml.model.CommonsSCXML;
import org.apache.common.faces.state.scxml.model.CustomAction;
import org.apache.common.faces.state.scxml.model.CustomActionWrapper;
import org.apache.common.faces.state.scxml.model.ModelException;
import org.apache.common.faces.state.scxml.model.Param;
import org.apache.common.faces.state.scxml.model.ParamsContainer;
import org.apache.common.faces.state.scxml.model.Var;

/**
 * The class in this SCXML object model that corresponds to the
 * {@link CustomAction} &lt;var&gt; SCXML element.
 * <p>
 * When manually constructing or modifying a SCXML model using this custom
 * action, either:
 * <ul>
 * <li>derive from {@link CommonsSCXML}, or</li>
 * <li>make sure to add the {@link SCXMLConstants#XMLNS_COMMONS_SCXML} namespace
 * with the {@link SCXMLConstants#XMLNS_COMMONS_SCXML_PREFIX} prefix to the
 * SCXML object, or</li>
 * <li>wrap the {@link Var} instance in a {@link CustomActionWrapper} (for which
 * the {@link #CUSTOM_ACTION} can be useful) before adding it to the object
 * model</li>
 * </ul>
 * before write the SCXML model with {@link SCXMLWriter}. The writing will fail
 * otherwise!
 * </p>
 */
public class Redirect extends Action implements ParamsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The url to redirect.
     */
    private String url;

    /**
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * The List of the params getString be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    @Override
    public List<Param> getParams() {
        return paramsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        Context ctx = exctx.getContext(getParentEnterableState());
        Evaluator eval = exctx.getEvaluator();
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            ExternalContext ec = fc.getExternalContext();

            Map<String, List<String>> params = new LinkedHashMap<>();
            for (int i = 0; i < paramsList.size(); i++) {
                Param param = paramsList.get(i);
                Object value = eval.eval(ctx, param.getExpr());
                Object name = eval.eval(ctx, param.getName());
                params.put(name.toString(), Collections.singletonList(value.toString()));
            }

            StateFlowHandler handler = StateFlowHandler.getInstance();
            
            NavigationCase navCase = findNavigationCase(fc, url);
            if (navCase != null) {
                String action = navCase.getToViewId(fc);
                String actionURL = fc.getApplication().
                        getViewHandler().getActionURL(fc, action);
                
                
                ec.getClientWindow().disableClientWindowRenderMode(fc);
                String redirectPath = ec.encodeRedirectURL(actionURL, params);
                ec.redirect(redirectPath);
                handler.closeAll(fc);
                fc.responseComplete();
            } else {
                ec.getClientWindow().disableClientWindowRenderMode(fc);
                String redirectPath = ec.encodeRedirectURL(url, params);
                ec.redirect(redirectPath);
                handler.closeAll(fc);
                fc.responseComplete();
            }

        } catch (SCXMLExpressionException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new ModelException(ex);
        }
    }

    /**
     *
     * @param context
     * @param outcome
     * @return
     */
    protected NavigationCase findNavigationCase(FacesContext context, String outcome) {
        ConfigurableNavigationHandler navigationHandler = (ConfigurableNavigationHandler) context.getApplication().getNavigationHandler();
        return navigationHandler.getNavigationCase(context, null, outcome);
    }

}
