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
package org.apache.common.faces.impl.state.invokers;

import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewParameter;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import org.apache.common.faces.state.component.UIStateChartDefinition;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagException;
import org.apache.common.faces.impl.state.StateFlowParams;
import org.apache.common.faces.state.StateFlow;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import org.apache.common.scxml.model.SCXML;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static org.apache.common.faces.state.StateFlow.BEFORE_APPLY_REQUEST_VALUES;
import static org.apache.common.faces.state.StateFlow.CURRENT_INVOKED_VIEW_ID;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.STATECHART_FACET_NAME;
import org.apache.common.faces.state.StateFlowViewContext;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.InvokeContext;
import org.apache.common.scxml.model.ModelException;
import static org.apache.common.faces.state.StateFlow.VIEW_INVOKE_CONTEXT;
import org.apache.common.faces.state.component.UIStateChartController;

/**
 * A simple {@link Invoker} for SCXML documents. Invoked SCXML document may not
 * contain external namespace elements, further invokes etc.
 */
public class FacetInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(FacetInvoker.class.getName());

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * invokeId ID.
     */
    private transient String invokeId;
    /**
     * Invoking parent SCXMLExecutor
     */
    private transient SCXMLExecutor executor;

    /**
     * Cancellation status.
     */
    private boolean cancelled;

    private String facetId;
    private Map<String, Object> facetparams;

    /**
     * {@inheritDoc}.
     */
    @Override
    public String getInvokeId() {
        return invokeId;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor executor) {
        this.executor = executor;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        // not used
        return null;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invoke(final InvokeContext ictx, String source, final Map<String, Object> params) throws InvokerException {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            String machineViewId = (String) executor.getStateMachine().getMetadata().get("faces-viewid");

            UIComponent current = UIComponent.getCurrentComponent(context);
            UIStateChartController controller = (UIStateChartController) current;

            if (source.startsWith("@this.")) {
                String name = source.substring(6);
                UIComponent facet = controller.getParent().getFacet(name);
                if (facet == null) {
                    throwRequiredInRootException(context, name, facet);
                }
                facetId = facet.getClientId(context);
            }

            Map<String, Object> options = new HashMap();

            facetparams = new HashMap();
            for (String key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (value instanceof String) {
                    if (containsOnlyDigits((String) value)) {
                        value = NumberFormat.getInstance().parse((String) value);
                    } else if ("true".equals(value)) {
                        value = true;
                    } else if ("false".equals(value)) {
                        value = false;
                    }
                }
                if (skey.startsWith("@facet.param.")) {
                    skey = skey.substring(12);
                    options.put(skey, value.toString());
                } else if (value != null) {
                    facetparams.put(skey, value.toString());
                }
            }

            if (controller.getFacetId() != null) {
                String currentFacetId = controller.getFacetId();
                if (currentFacetId.equals(facetId)) {
                    return;
                }
            }

            controller.setFacetId(facetId);

        } catch (Throwable ex) {
            logger.log(Level.SEVERE, "Invoke failed", ex);
            throw new InvokerException(ex);
        }
    }

    private boolean containsOnlyDigits(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void parentEvent(final InvokeContext ictx, final TriggerEvent event) throws InvokerException {
        if (cancelled) {
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        //filter all multicast call event from started viewId by this invoker
        if (event.getType() == TriggerEvent.CALL_EVENT) {

            if (facetId.equals(event.getSendId())) {

                if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
                    ExternalContext ec = context.getExternalContext();

                    Map<String, String> params = new HashMap<>();
                    params.putAll(ec.getRequestParameterMap());

                    String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                    EventBuilder evb = new EventBuilder("view.action." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);

                    evb.data(params);
                    evb.sendId(invokeId);
                    executor.addEvent(evb.build());
                }
            }
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent current = UIComponent.getCurrentComponent(context);
        UIStateChartController controller = (UIStateChartController) current;
        controller.setFacetId(facetId);

    }

    private void throwRequiredCompositeException(FacesContext ctx,
            String name,
            UIComponent compositeParent) throws InvokerException {

        throw new InvokerException(
                "Unable to find facet named '"
                + name
                + "' in parent composite component with id '"
                + compositeParent.getClientId(ctx)
                + '\'');

    }

    private void throwRequiredThisException(FacesContext ctx,
            String name,
            UIComponent parent) throws InvokerException {

        throw new InvokerException(
                "Unable to find facet named '"
                + name
                + "' in component with id '"
                + parent.getClientId(ctx)
                + '\'');

    }

    private void throwRequiredInRootException(FacesContext ctx,
            String name,
            UIComponent root) throws InvokerException {

        throw new InvokerException(
                "Unable to find facet named '"
                + name
                + "' in view component with id '"
                + root.getClientId(ctx)
                + '\'');

    }

}
