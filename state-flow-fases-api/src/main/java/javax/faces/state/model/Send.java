/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.model;

import javax.faces.state.ExternalContent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import javax.faces.state.FlowContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.FlowEvaluator;
import javax.faces.state.FlowEventDispatcher;
import javax.faces.state.FlowExpressionException;
import javax.faces.state.FlowInstance;
import javax.faces.state.FlowTriggerEvent;
import javax.faces.state.ModelException;
import javax.faces.state.semantics.ErrorConstants;
import javax.faces.state.utils.StateFlowHelper;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class Send extends Action implements ExternalContent {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The default targettype.
     */
    private static final String TARGETTYPE_SCXML = "scxml";

    /**
     * The spec mandated derived event when target cannot be reached for
     * TARGETTYPE_SCXML.
     */
    private static final String EVENT_ERR_SEND_TARGETUNAVAILABLE
            = "error.send.targetunavailable";

    /**
     * The ID of the send message.
     */
    private String sendid;

    /**
     * An expression returning the target location of the event.
     */
    private String target;

    /**
     * The type of the Event I/O Processor that the event. should be dispatched
     * to
     */
    private String targettype;

    /**
     * The event is dispatched after the delay interval elapses.
     */
    private String delay;

    /**
     * The data containing information which may be used by the implementing
     * platform to configure the event processor.
     */
    private String hints;

    /**
     * The namelist to the sent.
     */
    private String namelist;

    /**
     * The list of external nodes associated with this &lt;send&gt; element.
     */
    private List externalNodes;

    /**
     * The type of event being generated.
     */
    private String event;

    /**
     * The List of the params to be sent to the invoked process.
     */
    private final List<Param> paramsList;
    
    /**
     * OutputFormat used to serialize external nodes.
     *
     * private static final OutputFormat format; static { format = new
     * OutputFormat(); format.setOmitXMLDeclaration(true); }
     */
    /**
     * Constructor.
     */
    public Send() {
        super();
        paramsList = new ArrayList();
        this.externalNodes = new ArrayList();
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
     * Get the list of external namespaced child nodes.
     *
     * @return List Returns the list of externalnodes.
     */
    @Override
    public final List getExternalNodes() {
        return externalNodes;
    }

    /**
     * Set the list of external namespaced child nodes.
     *
     * @param externalNodes The externalnode to set.
     */
    public final void setExternalNodes(final List externalNodes) {
        this.externalNodes = externalNodes;
    }

    /**
     * Get the hints for this &lt;send&gt; element.
     *
     * @return String Returns the hints.
     */
    public final String getHints() {
        return hints;
    }

    /**
     * Set the hints for this &lt;send&gt; element.
     *
     * @param hints The hints to set.
     */
    public final void setHints(final String hints) {
        this.hints = hints;
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
     * Get the identifier for this &lt;send&gt; element.
     *
     * @return String Returns the sendid.
     */
    public final String getSendid() {
        return sendid;
    }

    /**
     * Set the identifier for this &lt;send&gt; element.
     *
     * @param sendid The sendid to set.
     */
    public final void setSendid(final String sendid) {
        this.sendid = sendid;
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
     * Get the target type for this &lt;send&gt; element.
     *
     * @return String Returns the targettype.
     */
    public final String getTargettype() {
        return targettype;
    }

    /**
     * Set the target type for this &lt;send&gt; element.
     *
     * @param targettype The targettype to set.
     */
    public final void setTargettype(final String targettype) {
        this.targettype = targettype;
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
     * Get the list of {@link Param}s.
     *
     * @return List The params list.
     */
    public final List<Param> params() {
        return paramsList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final FlowEventDispatcher evtDispatcher,
            final FlowErrorReporter errRep, final FlowInstance scInstance,
            final Collection derivedEvents)
            throws ModelException, FlowExpressionException {
        // Send attributes evaluation
        TransitionTarget parentTarget = getParentTransitionTarget();
        FlowContext ctx = scInstance.getContext(parentTarget);
        ctx.setLocal(getNamespacesKey(), getNamespaces());
        FlowEvaluator eval = scInstance.getEvaluator();
        // Most attributes of <send> are expressions so need to be
        // evaluated before the EventDispatcher callback
        Object hintsValue = null;
        if (!StateFlowHelper.isStringEmpty(hints)) {
            hintsValue = eval.eval(ctx, hints);
        }
        String targetValue = target;
        if (!StateFlowHelper.isStringEmpty(target)) {
            targetValue = (String) eval.eval(ctx, target);
            if (StateFlowHelper.isStringEmpty(targetValue) && log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, "<send>: target expression \"{0}\" evaluated to null or empty String", target);
            }
        }
        @SuppressWarnings("UnusedAssignment")
        String targettypeValue = targettype;
        if (!StateFlowHelper.isStringEmpty(targettype)) {
            targettypeValue = (String) eval.eval(ctx, targettype);
            if (StateFlowHelper.isStringEmpty(targettypeValue) && log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, "<send>: targettype expression \"{0}\" evaluated to null or empty String", targettype);
            }
        } else {
            // must default to 'flow' when unspecified
            targettypeValue = TARGETTYPE_SCXML;
        }
        Map params = null;
        if (!StateFlowHelper.isStringEmpty(namelist)) {
            StringTokenizer tkn = new StringTokenizer(namelist);
            params = new HashMap(tkn.countTokens());
            while (tkn.hasMoreTokens()) {
                String varName = tkn.nextToken();
                Object varObj = ctx.get(varName);
                if (varObj == null) {
                    //considered as a warning here
                    errRep.onError(ErrorConstants.UNDEFINED_VARIABLE, varName + " = null", parentTarget);
                }
                params.put(varName, varObj);
            }
        }
        long wait = 0L;
        if (!StateFlowHelper.isStringEmpty(delay)) {
            Object delayValue = eval.eval(ctx, delay);
            if (delayValue != null) {
                String delayString = delayValue.toString();
                wait = parseDelay(delayString);
            }
        }
        String eventValue = event;
        if (!StateFlowHelper.isStringEmpty(event)) {
            eventValue = (String) eval.eval(ctx, event);
            if (StateFlowHelper.isStringEmpty(eventValue) && log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, "<send>: event expression \"{0}\" evaluated to null or empty String", event);
            }
        }
        // Lets see if we should handle it ourselves
        if (targettypeValue != null && targettypeValue.trim().equalsIgnoreCase(TARGETTYPE_SCXML)) {
            if (StateFlowHelper.isStringEmpty(targetValue)) {
                // TODO: Remove both short-circuit passes in v1.0
                if (wait == 0L) {
                    if (log.isLoggable(Level.FINE)) {
                        log.log(Level.FINE, "<send>: Enqueued event ''{0}'' with no delay", eventValue);
                    }
                    derivedEvents.add(new FlowTriggerEvent(eventValue, FlowTriggerEvent.SIGNAL_EVENT, params));
                    return;
                }
            } else {
                // We know of no other
                if (log.isLoggable(Level.WARNING)) {
                    log.log(Level.WARNING, "<send>: Unavailable target - {0}", targetValue);
                }
                derivedEvents.add(new FlowTriggerEvent(
                        EVENT_ERR_SEND_TARGETUNAVAILABLE,
                        FlowTriggerEvent.ERROR_EVENT));
                // short-circuit the EventDispatcher
                return;
            }
        }
        ctx.setLocal(getNamespacesKey(), null);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "<send>: Dispatching event ''{0}'' to target ''{1}'' of target type ''{2}'' with suggested delay of {3}ms", new Object[]{eventValue, targetValue, targettypeValue, wait});
        }
        // Else, let the EventDispatcher take care of it
        evtDispatcher.send(sendid, targetValue, targettypeValue, eventValue, params, hintsValue, wait, externalNodes);
    }

    /**
     * Parse delay.
     *
     * @param delayString The String value of the delay, in CSS2 format
     * @param appLog The application log
     * @return The parsed delay in milliseconds
     * @throws SCXMLExpressionException If the delay cannot be parsed
     */
    private long parseDelay(final String delayString)
            throws FlowExpressionException {

        long wait = 0L;
        long multiplier = 1L;

        if (!StateFlowHelper.isStringEmpty(delayString)) {

            String trimDelay = delayString.trim();
            String numericDelay = trimDelay;
            if (trimDelay.endsWith(MILLIS)) {
                numericDelay = trimDelay.substring(0, trimDelay.length() - 2);
            } else if (trimDelay.endsWith(SECONDS)) {
                multiplier = MILLIS_IN_A_SECOND;
                numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
            } else if (trimDelay.endsWith(MINUTES)) { // Not CSS2
                multiplier = MILLIS_IN_A_MINUTE;
                numericDelay = trimDelay.substring(0, trimDelay.length() - 1);
            }

            try {
                wait = Long.parseLong(numericDelay);
            } catch (NumberFormatException nfe) {
                log.log(Level.SEVERE, nfe.getMessage(), nfe);
                throw new FlowExpressionException(nfe.getMessage(), nfe);
            }
            wait *= multiplier;

        }

        return wait;

    }

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

}
