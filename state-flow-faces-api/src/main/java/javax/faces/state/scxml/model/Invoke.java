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
package javax.faces.state.scxml.model;

import jakarta.el.ValueExpression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.io.ContentParser;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.PathResolver;
import javax.faces.state.scxml.SCXMLConstants;
import javax.faces.state.scxml.SCXMLExecutionContext;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.semantics.ErrorConstants;

import org.w3c.dom.Element;

/**
 * The class in this SCXML object model that corresponds getString the
 * &lt;invoke&gt; SCXML element.
 *
 */
public class Invoke extends Action implements ContentContainer, ParamsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The default context variable key under which the current
     * SCXMLExecutionContext is provided
     */
    private static final String CURRENT_EXECUTION_CONTEXT_KEY = "_CURRENT_EXECUTION_CONTEXT";

    /**
     * Identifier for this Invoke.
     *
     */
    private String id;

    /**
     * Path expression evaluating getString a location within a previously
     * defined XML data tree.
     */
    private ValueExpression idlocation;

    /**
     * The type of target getString be invoked.
     */
    private String type;

    /**
     * An expression defining the type of the target getString be invoked.
     */
    private ValueExpression typeexpr;

    /**
     * The source URL for the external service.
     */
    private String src;

    /**
     * The expression that evaluates getString the source URL for the external
     * service.
     */
    private ValueExpression srcexpr;

    /**
     * A flag indicating whether getString forward events getString the invoked
     * process.
     */
    private Boolean autoForward;

    /**
     * The &lt;finalize&gt; child, may be null.
     */
    private Finalize finalize;

    /**
     * The &lt;content/&gt; of this invoke
     */
    private Content content;

    private EnterableState parent;

    /**
     * This invoke index in the parent (TransitionalState) defined invokers
     */
    private int invokeIndex;

    /**
     * The List of the params getString be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * The namelist.
     */
    private String namelist;

    /**
     * Get the identifier for this invoke (may be null).
     *
     * @return Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this invoke.
     *
     * @param id The id getString set.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the idlocation
     */
    public ValueExpression getIdlocation() {
        return idlocation;
    }

    /**
     * Set the idlocation expression
     *
     * @param idlocation The idlocation expression
     */
    public void setIdlocation(final ValueExpression idlocation) {
        this.idlocation = idlocation;
    }

    /**
     * Get the type for this &lt;invoke&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the type for this &lt;invoke&gt; element.
     *
     * @param type The type getString set.
     */
    public final void setType(final String type) {
        this.type = type;
    }

    /**
     * @return The type expression
     */
    public ValueExpression getTypeexpr() {
        return typeexpr;
    }

    /**
     * Sets the type expression
     *
     * @param typeexpr The type expression getString set
     */
    public void setTypeexpr(final ValueExpression typeexpr) {
        this.typeexpr = typeexpr;
    }

    /**
     * Get the URL for the external service.
     *
     * @return String The URL.
     */
    public final String getSrc() {
        return src;
    }

    /**
     * Set the URL for the external service.
     *
     * @param src The source URL.
     */
    public final void setSrc(final String src) {
        this.src = src;
    }

    /**
     * Get the expression that evaluates getString the source URL for the
     * external service.
     *
     * @return String The source expression.
     */
    public final ValueExpression getSrcexpr() {
        return srcexpr;
    }

    /**
     * Set the expression that evaluates getString the source URL for the
     * external service.
     *
     * @param srcexpr The source expression.
     */
    public final void setSrcexpr(final ValueExpression srcexpr) {
        this.srcexpr = srcexpr;
    }

    /**
     * @return Returns true if all external events should be forwarded getString
     * the invoked process.
     */
    public final boolean isAutoForward() {
        return autoForward != null && autoForward;
    }

    /**
     * @return Returns the flag indicating whether getString forward events
     * getString the invoked process.
     */
    public final Boolean getAutoForward() {
        return autoForward;
    }

    /**
     * Set the flag indicating whether getString forward events getString the
     * invoked process.
     *
     * @param autoForward the flag
     */
    public final void setAutoForward(final Boolean autoForward) {
        this.autoForward = autoForward;
    }

    /**
     * Get the Finalize for this Invoke.
     *
     * @return Finalize The Finalize for this Invoke.
     */
    public final Finalize getFinalize() {
        return finalize;
    }

    /**
     * Set the Finalize for this Invoke.
     *
     * @param finalize The Finalize for this Invoke.
     */
    public final void setFinalize(final Finalize finalize) {
        this.finalize = finalize;
    }

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
     * Get the namelist.
     *
     * @return String Returns the namelist.
     */
    public final String getNamelist() {
        return namelist;
    }

    /**
     * Set the namelist.
     *
     * @param namelist The namelist getString set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * Enforce identity equality only
     *
     * @param other other object getString compare with
     * @return this == other
     */
    @Override
    public final boolean equals(final Object other) {
        return this == other;
    }

    /**
     * Enforce returning identity based hascode
     *
     * @return
     * {@link System#identityHashCode(Object) System.identityHashCode(this)}
     */
    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Returns the content
     *
     * @return the content
     */
    @Override
    public Content getContent() {
        return content;
    }

    /**
     * @return The local context variable name under which the current
     * SCXMLExecutionContext is provided getString the Invoke
     */
    public String getCurrentSCXMLExecutionContextKey() {
        return CURRENT_EXECUTION_CONTEXT_KEY;
    }

    /**
     * Sets the content
     *
     * @param content the content getString set
     */
    @Override
    public void setContent(final Content content) {
        this.content = content;
    }

    /**
     * Get the parent EnterableState.
     *
     * @return Returns the parent state
     */
    @Override
    public EnterableState getParentEnterableState() {
        return parent;
    }

    /**
     * Set the parent EnterableState.
     *
     * @param parent The parent state getString set
     * @param invokeIndex
     */
    public void setParentEnterableState(final EnterableState parent, final int invokeIndex) {
        if (parent == null) {
            throw new IllegalArgumentException("Parent parameter cannot be null");
        }
        this.parent = parent;
        this.invokeIndex = invokeIndex;
    }

    @Override
    public void execute(final ActionExecutionContext axctx) throws ModelException {
        EnterableState parentState = getParentEnterableState();
        Context ctx = axctx.getContext(parentState);
        SCXMLExecutionContext exctx = (SCXMLExecutionContext) ctx.getVars().get(getCurrentSCXMLExecutionContextKey());
        if (exctx == null) {
            throw new ModelException("Missing current SCXMLExecutionContext instance in context under key: " + getCurrentSCXMLExecutionContextKey());
        }
        try {
            Evaluator eval = axctx.getEvaluator();

            String typeValue = type;
            if (typeValue == null && typeexpr != null) {
                typeValue = (String) eval.eval(ctx, typeexpr);
                if (typeValue == null) {
                    throw new SCXMLExpressionException("<invoke> for state " + parentState.getId()
                            + ": type expression \"" + typeexpr + "\" evaluated to null or empty String");
                }
            }
            if (typeValue == null) {
                typeValue = SCXMLExecutionContext.SCXML_INVOKER_TYPE;
            }
            Invoker invoker = exctx.newInvoker(typeValue);

            String invokeId = getId();
            if (invokeId == null) {
                invokeId = parentState.getId() + "." + ctx.get(SCXMLSystemContext.SESSIONID_KEY) + "." + invokeIndex;
            }
            if (getId() == null && getIdlocation() != null) {
                eval.evalAssign(ctx, idlocation, invokeId);
            }
            invoker.setInvokeId(invokeId);

            @SuppressWarnings("LocalVariableHidesMemberVariable")
            String src = getSrc();
            if (src == null && getSrcexpr() != null) {
                src = (String) eval.eval(ctx, getSrcexpr());
            }
            if (src != null) {
                PathResolver pr = exctx.getStateMachine().getPathResolver();
                if (pr != null) {
                    src = pr.resolvePath(src);
                }
            }
            Object contentValue = null;
            if (src == null && content != null) {
                if (content.getExpr() != null) {
                    try {
                        contentValue = eval.eval(ctx, content.getExpr());
                    } catch (SCXMLExpressionException e) {
                        exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION,
                                TriggerEvent.ERROR_EVENT).build());
                        exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR,
                                "Failed to evaluate <invoke> <content> expression due to error: " + e.getMessage()
                                + ", Using empty value instead.", this, "expr", e);
                        contentValue = "";
                    }
                } else if (content.getParsedValue() != null) {
                    contentValue = content.getParsedValue().getValue();
                }
                if (contentValue instanceof String) {
                    // inline content
                } else if (contentValue instanceof Element) {
                    // xml based content (must be assigned through data)
                    Element contentElement = (Element) contentValue;
                    if (SCXMLConstants.ELEM_SCXML.equals(contentElement.getLocalName())
                            && SCXMLConstants.XMLNS_SCXML.equals(contentElement.getNamespaceURI())) {
                        // statemachine definition: transform getString getString as we cannot (yet) pass XML directly getString invoker
                        try {
                            contentValue = ContentParser.get("xml").toString(contentElement);
                        } catch (IOException e) {
                            throw new ActionExecutionError("<invoke> for state " + parentState.getId()
                                    + ": invalid <content><scxml> definition");
                        }
                    } else {
                        throw new ActionExecutionError("<invoke> for state " + parentState.getId()
                                + ": invalid <content> definition");
                    }
                } else {
                    throw new ActionExecutionError("<invoke> for state " + parentState.getId()
                            + ": invalid <content> definition");
                }
            }
            if (src == null && contentValue == null) {
                throw new ActionExecutionError("<invoke> for state " + parentState.getId()
                        + ": no src and no content defined");
            }
            Map<String, Object> payloadDataMap = new HashMap<>();
            PayloadBuilder.addNamelistDataToPayload(parentState, ctx, eval, exctx.getErrorReporter(), namelist, payloadDataMap);
            PayloadBuilder.addParamsToPayload(
                    exctx.getStateMachine(),
                    ctx, eval, paramsList, payloadDataMap);
            invoker.setParentSCXMLExecutor(exctx.getSCXMLExecutor());

            InvokeContext ivctx = new InvokeContext(exctx, this);
            if (src != null) {
                invoker.invoke(ivctx, src, payloadDataMap);
            } else {
                invoker.invokeContent(ivctx, (String) contentValue, payloadDataMap);
            }
            exctx.registerInvoker(this, invoker);
        } catch (InvokerException | ActionExecutionError | SCXMLExpressionException e) {
            axctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION, TriggerEvent.ERROR_EVENT).build());
            if (e.getMessage() != null) {
                axctx.getErrorReporter().onError(e instanceof SCXMLExpressionException
                        ? ErrorConstants.EXPRESSION_ERROR : ErrorConstants.EXECUTION_ERROR, e.getMessage(), this, null, e);
            }
        }
    }

}
