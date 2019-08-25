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
package javax.faces.state.scxml.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.el.ValueExpression;
import javax.faces.state.scxml.ActionExecutionContext;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.Evaluator;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.SCXMLExpressionException;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.scxml.SendContext;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.semantics.ErrorConstants;

/**
 * The class in this SCXML object model that corresponds to the &lt;send&gt;
 * SCXML element.
 *
 */
public class Send extends Action implements ContentContainer, ParamsContainer {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The suffix in the delay string for milliseconds.
     */
    private static final String MILLIS = "ms";

    /**
     * The suffix in the delay string for seconds.
     */
    private static final String SECONDS = "s";

    /**
     * The suffix in the delay string for minutes.
     */
    private static final String MINUTES = "m";

    /**
     * The number of milliseconds in a second.
     */
    private static final long MILLIS_IN_A_SECOND = 1000L;

    /**
     * The number of milliseconds in a minute.
     */
    private static final long MILLIS_IN_A_MINUTE = 60000L;

    /**
     * The ID of the send message.
     */
    private String id;

    /**
     * Path expression evaluating to a location within a previously defined XML
     * data tree.
     */
    private ValueExpression idlocation;

    /**
     * The target location of the event.
     */
    private String target;

    /**
     * An expression specifying the target location of the event.
     */
    private ValueExpression targetexpr;

    /**
     * The type of the Event I/O Processor that the event should be dispatched
     * to.
     */
    private String type;

    /**
     * An expression defining the type of the Event I/O Processor that the event
     * should be dispatched to.
     */
    private ValueExpression typeexpr;

    /**
     * The delay the event is dispatched after.
     */
    private String delay;

    /**
     * An expression defining the delay the event is dispatched after.
     */
    private ValueExpression delayexpr;

    /**
     * The data containing information which may be used by the implementing
     * platform to configure the event processor.
     */
    private ValueExpression hints;

    /**
     * The type of event being generated.
     */
    private String event;

    /**
     * An expression defining the type of event being generated.
     */
    private ValueExpression eventexpr;

    /**
     * The &lt;content/&gt; of this send
     */
    private Content content;

    /**
     * The List of the params to be sent
     */
    private final List<Param> paramsList = new ArrayList<>();

    /**
     * The namelist.
     */
    private String namelist;

    /**
     * Constructor.
     */
    public Send() {
        super();
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
     * Get the delay.
     *
     * @return Returns the delay.
     */
    public final String getDelay() {
        return delay;
    }

    /**
     * Set the delay.
     *
     * @param delay The delay to set.
     */
    public final void setDelay(final String delay) {
        this.delay = delay;
    }

    /**
     * @return The delay expression
     */
    public ValueExpression getDelayexpr() {
        return delayexpr;
    }

    /**
     * Set the delay expression
     *
     * @param delayexpr The delay expression to set
     */
    public void setDelayexpr(final ValueExpression delayexpr) {
        this.delayexpr = delayexpr;
    }

    /**
     * Get the hints for this &lt;send&gt; element.
     *
     * @return String Returns the hints.
     */
    public final ValueExpression getHints() {
        return hints;
    }

    /**
     * Set the hints for this &lt;send&gt; element.
     *
     * @param hints The hints to set.
     */
    public final void setHints(final ValueExpression hints) {
        this.hints = hints;
    }

    /**
     * Get the identifier for this &lt;send&gt; element.
     *
     * @return String Returns the id.
     */
    public final String getId() {
        return id;
    }

    /**
     * Set the identifier for this &lt;send&gt; element.
     *
     * @param id The id to set.
     */
    public final void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the target for this &lt;send&gt; element.
     *
     * @return String Returns the target.
     */
    public final String getTarget() {
        return target;
    }

    /**
     * Set the target for this &lt;send&gt; element.
     *
     * @param target The target to set.
     */
    public final void setTarget(final String target) {
        this.target = target;
    }

    /**
     * @return The target expression
     */
    public ValueExpression getTargetexpr() {
        return targetexpr;
    }

    /**
     * Set the target expression
     *
     * @param targetexpr The target expression to set
     */
    public void setTargetexpr(final ValueExpression targetexpr) {
        this.targetexpr = targetexpr;
    }

    /**
     * Get the type for this &lt;send&gt; element.
     *
     * @return String Returns the type.
     */
    public final String getType() {
        return type;
    }

    /**
     * Set the type for this &lt;send&gt; element.
     *
     * @param type The type to set.
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
     * @param typeexpr The type expression to set
     */
    public void setTypeexpr(final ValueExpression typeexpr) {
        this.typeexpr = typeexpr;
    }

    /**
     * Get the event to send.
     *
     * @param event The event to set.
     */
    public final void setEvent(final String event) {
        this.event = event;
    }

    /**
     * Set the event to send.
     *
     * @return String Returns the event.
     */
    public final String getEvent() {
        return event;
    }

    /**
     * @return The event expression
     */
    public ValueExpression getEventexpr() {
        return eventexpr;
    }

    /**
     * Sets the event expression
     *
     * @param eventexpr The event expression to set
     */
    public void setEventexpr(final ValueExpression eventexpr) {
        this.eventexpr = eventexpr;
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
     * Sets the content
     *
     * @param content the content to set
     */
    @Override
    public void setContent(final Content content) {
        this.content = content;
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
     * @param namelist The namelist to set.
     */
    public final void setNamelist(final String namelist) {
        this.namelist = namelist;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(ActionExecutionContext exctx) throws ModelException, SCXMLExpressionException {
        // Send attributes evaluation
        EnterableState parentState = getParentEnterableState();
        Context ctx = exctx.getContext(parentState);
        Evaluator eval = exctx.getEvaluator();
        // Most attributes of <send> are expressions so need to be
        // evaluated before the EventDispatcher callback
        Object hintsValue = null;
        if (hints != null) {
            hintsValue = eval.eval(ctx, hints);
        }
        if (id == null) {
            id = ctx.getSystemContext().generateSessionId();
        }
        if (idlocation != null) {
            eval.evalAssign(ctx, idlocation, id);
        }
        String targetValue = target;
        if (targetValue == null && targetexpr != null) {
            targetValue = (String) eval.eval(ctx, targetexpr);
            if ((targetValue == null || targetValue.trim().length() == 0)
                    && exctx.getAppLog().isLoggable(Level.WARNING)) {
                //exctx.getAppLog().log(Level.WARNING, "<send>: target expression \"{0}\" evaluated to null or empty String", targetexpr);
            }
        }
        String typeValue = type;
        if (typeValue == null && typeexpr != null) {
            typeValue = (String) eval.eval(ctx, typeexpr);
            if ((typeValue == null || typeValue.trim().length() == 0)
                    && exctx.getAppLog().isLoggable(Level.WARNING)) {
                //exctx.getAppLog().log(Level.WARNING, "<send>: type expression \"{0}\" evaluated to null or empty String", typeexpr);
            }
        }
        if (typeValue == null) {
            // must default to 'scxml' when unspecified
            typeValue = SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR;
        } else if (!SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR.equals(typeValue) && typeValue.trim().equalsIgnoreCase(SCXMLIOProcessor.SCXML_EVENT_PROCESSOR)) {
            typeValue = SCXMLIOProcessor.DEFAULT_EVENT_PROCESSOR;
        }
        Object payload = null;
        Map<String, Object> payloadDataMap = new LinkedHashMap<>();
        PayloadBuilder.addNamelistDataToPayload(parentState, ctx, eval, exctx.getErrorReporter(), namelist, payloadDataMap);
        PayloadBuilder.addParamsToPayload(
                exctx.getStateMachine(),
                ctx, eval, paramsList, payloadDataMap);
        if (!payloadDataMap.isEmpty()) {
            payload = payloadDataMap;
        } else if (content != null) {
            if (content.getExpr() != null) {
                Object evalResult;
                try {
                    evalResult = eval.eval(ctx, content.getExpr());
                } catch (SCXMLExpressionException e) {
                    exctx.getInternalIOProcessor().addEvent(new EventBuilder(TriggerEvent.ERROR_EXECUTION,
                            TriggerEvent.ERROR_EVENT).build());
                    exctx.getErrorReporter().onError(ErrorConstants.EXPRESSION_ERROR,
                            "Failed to evaluate <send> <content> expression due to error: " + e.getMessage()
                            + ", Using empty value instead.", this, "expr", e);
                    evalResult = "";
                }
                payload = eval.cloneData(evalResult);
            } else if (content.getParsedValue() != null) {
                payload = content.getParsedValue().getValue();
            }
        }
        long wait = 0L;
        String delayString = delay;
        if (delayString == null && delayexpr != null) {
            Object delayValue = eval.eval(ctx, delayexpr);
            if (delayValue != null) {
                delayString = delayValue.toString();
            }
        }
        if (delayString != null) {
            wait = parseDelay(delayString, delayexpr != null, delayexpr != null ? delayexpr.toString() : delay);
        }
        String eventValue = event;
        if (eventValue == null && eventexpr != null) {
            eventValue = (String) eval.eval(ctx, eventexpr);
            if ((eventValue == null)) {
                throw new SCXMLExpressionException("<send>: event expression \"" + eventexpr
                        + "\" evaluated to null");
            }
        }
        if (exctx.getAppLog().isLoggable(Level.FINE)) {
            exctx.getAppLog().log(Level.FINE, "<send>: Dispatching event ''{0}'' to target ''{1}'' of target type ''{2}'' with suggested delay of {3}ms", new Object[]{eventValue, targetValue, typeValue, wait});
        }
        SendContext sctx = new SendContext(id, 
                typeValue, 
                targetValue, 
                eventValue, 
                payload, 
                hintsValue, 
                wait, 
                exctx, 
                ctx);
        
        
        exctx.getEventDispatcher().send(sctx);
    }

    /**
     * Parse delay.
     *
     * @param delayString The String value of the delay, in CSS2 format
     * @param expression indicates if this is for a delayexpr or delay attribute
     * @param delayStringSource the original delayString source (delayString
     * might be different in case of a delayexpr)
     * @return The parsed delay in milliseconds
     * @throws SCXMLExpressionException If the delay cannot be parsed
     */
    static long parseDelay(final String delayString, final boolean expression, final String delayStringSource)
            throws SCXMLExpressionException {

        long wait = 0L;
        long multiplier = 1L;

        if (delayString != null && delayString.trim().length() > 0) {

            try {
                String trimDelay = delayString.trim();
                String numericDelay = trimDelay;
                if (trimDelay.endsWith(MILLIS)) {
                    numericDelay = trimDelay.substring(0, trimDelay.length() - 2);
                } else if (trimDelay.endsWith(SECONDS)) {
                    multiplier = multiplier * MILLIS_IN_A_SECOND;
                    numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
                } else if (trimDelay.endsWith(MINUTES)) { // Not CSS2
                    multiplier = multiplier * MILLIS_IN_A_MINUTE;
                    numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
                }
                int fractionIndex = numericDelay.indexOf('.');
                if (fractionIndex > -1) {
                    if (fractionIndex > 0) {
                        wait = Long.parseLong(numericDelay.substring(0, fractionIndex));
                        wait *= multiplier;
                    }
                    numericDelay = numericDelay.substring(fractionIndex + 1);
                    multiplier /= Math.pow(10, numericDelay.length());
                }
                if (numericDelay.length() > 0) {
                    wait += Long.parseLong(numericDelay) * multiplier;
                }
            } catch (NumberFormatException nfe) {
                throw new SCXMLExpressionException("<send>: invalid " + (expression ? "delayexpr=\"" : "delay=\"") + delayStringSource + "\"");
            }
        }
        return wait;
    }
}
