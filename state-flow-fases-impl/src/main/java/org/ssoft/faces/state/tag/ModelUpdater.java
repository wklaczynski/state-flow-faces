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
package org.ssoft.faces.state.tag;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.faces.state.ModelException;
import javax.faces.state.model.History;
import javax.faces.state.model.Initial;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.OnEntry;
import javax.faces.state.model.OnExit;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.Transition;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.utils.StateFlowHelper;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagAttributeException;
import javax.faces.view.facelets.TagException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ModelUpdater {

    private final Map<Object, Tag> tags;

    /**
     * Discourage instantiation since this is a utility class.
     *
     * @param tags
     */
    public ModelUpdater(Map<Object, Tag> tags) {
        super();
        this.tags = tags;
    }

    /*
     * Post-processing methods to make the SCXML object SCXMLExecutor ready.
     */
    /**
     * <p>
     * Update the SCXML object model and make it SCXMLExecutor ready. This is
     * part of post-digester processing, and sets up the necessary object
     * references throughtout the SCXML object model for the parsed
     * document.</p>
     *
     * @param scxml The SCXML object (output from Digester)
     * @throws java.io.IOException
     */
    public void updateSCXML(final StateChart scxml) throws IOException {
        String initial = scxml.getInitial();
        //we have to use getTargets() here since the initialTarget can be
        //an indirect descendant
        TransitionTarget initialTarget = (TransitionTarget) scxml.getTargets().get(initial);
        if (initialTarget == null) {
            // Where do we, where do we go?
            logAndThrowModelError(scxml, ERR_SCXML_NO_INIT, new Object[]{initial});
        }
        scxml.setInitialTarget(initialTarget);
        
        Map targets = scxml.getTargets();
        Map children = scxml.getChildren();
        Iterator i = children.keySet().iterator();
        while (i.hasNext()) {
            TransitionTarget tt = (TransitionTarget) children.get(i.next());
            if (tt instanceof State) {
                updateState((State) tt, targets);
            } else {
                updateParallel((Parallel) tt, targets);
            }
        }
    }

    /**
     * Update this State object (part of post-digestion processing). Also checks
     * for any errors in the document.
     *
     * @param state The State object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private void updateState(final State state, final Map targets) throws IOException {
        //initialize next / inital
        if (state.getOnEntry() == null) {
            state.setOnEntry(new OnEntry());
        }
        if (state.getOnExit() == null) {
            state.setOnExit(new OnExit());
        }
        Initial initial = state.getInitial();
        Map children = state.getChildren();
        List initialStates = null;
        if (!children.isEmpty()) {
            if (initial == null) {
                logAndThrowModelError(state, ERR_STATE_NO_INIT,
                        new Object[]{getStateName(state)});
            }
            Transition initialTransition = initial.getTransition();
            if (initialTransition == null) {
                logAndThrowModelError(initial, ERR_STATE_BAD_INIT_NULL,
                        new Object[]{getStateName(state)});
            }

            updateTransition(initialTransition, targets);
            initialStates = initialTransition.getTargets();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialStates.isEmpty()) {
                logAndThrowModelError(state, ERR_STATE_BAD_INIT_NULL,
                        new Object[]{getStateName(state)});
            } else {
                for (int i = 0; i < initialStates.size(); i++) {
                    TransitionTarget initialState = (TransitionTarget) initialStates.get(i);
                    if (!StateFlowHelper.isDescendant(initialState, state)) {
                        logAndThrowModelError(state, ERR_STATE_BAD_INIT_DES,
                                new Object[]{getStateName(state)});
                    }
                }
            }
        }
        List histories = state.getHistory();
        Iterator histIter = histories.iterator();
        while (histIter.hasNext()) {
            if (state.isSimple()) {
                logAndThrowModelError(state, ERR_HISTORY_SIMPLE_STATE,
                        new Object[]{getStateName(state)});
            }
            History h = (History) histIter.next();
            Transition historyTransition = h.getTransition();
            if (historyTransition == null) {
                // try to assign initial as default
                if (initialStates != null && initialStates.size() > 0) {
                    for (int i = 0; i < initialStates.size(); i++) {
                        if (initialStates.get(i) instanceof History) {
                            logAndThrowModelError(state, ERR_HISTORY_BAD_DEFAULT,
                                    new Object[]{h.getId(), getStateName(state)});
                        }
                    }
                    historyTransition = new Transition();
                    historyTransition.getTargets().addAll(initialStates);
                    h.setTransition(historyTransition);
                } else {
                    logAndThrowModelError(state, ERR_HISTORY_NO_DEFAULT,
                            new Object[]{h.getId(), getStateName(state)});
                }
            }
            updateTransition(historyTransition, targets);
            List historyStates = historyTransition.getTargets();
            if (historyStates.isEmpty()) {
                logAndThrowModelError(state, ERR_STATE_NO_HIST,
                        new Object[]{getStateName(state)});
            }
            for (int i = 0; i < historyStates.size(); i++) {
                TransitionTarget historyState = (TransitionTarget) historyStates.get(i);
                if (!h.isDeep()) {
                    if (!children.containsValue(historyState)) {
                        logAndThrowModelError(state, ERR_STATE_BAD_SHALLOW_HIST,
                                new Object[]{getStateName(state)});
                    }
                } else {
                    if (!StateFlowHelper.isDescendant(historyState, state)) {
                        logAndThrowModelError(state, ERR_STATE_BAD_DEEP_HIST,
                                new Object[]{getStateName(state)});
                    }
                }
            }
        }

        List t = state.getTransitionsList();
        for (int i = 0; i < t.size(); i++) {
            Transition trn = (Transition) t.get(i);
            updateTransition(trn, targets);
        }
        Parallel parallel = state.getParallel();
        Invoke invoke = state.getInvoke();
        if ((invoke != null && parallel != null)
                || (invoke != null && !children.isEmpty())
                || (parallel != null && !children.isEmpty())) {
            logAndThrowModelError(state, ERR_STATE_BAD_CONTENTS,
                    new Object[]{getStateName(state)});
        }

        if (parallel != null) {
            updateParallel(parallel, targets);
        } else if (invoke != null) {
            String ttype = invoke.getTargettype();
            if (ttype == null || ttype.trim().length() == 0) {
                logAndThrowModelError(state, ERR_INVOKE_NO_TARGETTYPE,
                        new Object[]{getStateName(state)});
            }
            String src = invoke.getSrc();
            boolean noSrc = (src == null || src.trim().length() == 0);
            String srcexpr = invoke.getSrcexpr();
            boolean noSrcexpr = (srcexpr == null
                    || srcexpr.trim().length() == 0);
            if (noSrc && noSrcexpr) {
                logAndThrowModelError(state, ERR_INVOKE_NO_SRC,
                        new Object[]{getStateName(state)});
            }
            if (!noSrc && !noSrcexpr) {
                logAndThrowModelError(state, ERR_INVOKE_AMBIGUOUS_SRC,
                        new Object[]{getStateName(state)});
            }
        } else {
            Iterator j = children.keySet().iterator();
            while (j.hasNext()) {
                TransitionTarget tt = (TransitionTarget) children.get(j.next());
                if (tt instanceof State) {
                    updateState((State) tt, targets);
                } else if (tt instanceof Parallel) {
                    updateParallel((Parallel) tt, targets);
                }
            }
        }
    }

    /**
     * Update this Parallel object (part of post-digestion processing).
     *
     * @param parallel The Parallel object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private void updateParallel(final Parallel parallel, final Map targets) throws IOException {
        if (parallel.getOnEntry() == null) {
            parallel.setOnEntry(new OnEntry());
        }
        if (parallel.getOnExit() == null) {
            parallel.setOnExit(new OnExit());
        }
        Map<String, TransitionTarget> children = parallel.getChildren();
        for (Map.Entry<String, TransitionTarget> entry : children.entrySet()) {
            TransitionTarget tt = entry.getValue();
            if (tt instanceof State) {
                updateState((State) tt, targets);
            } else if (tt instanceof Parallel) {
                updateParallel((Parallel) tt, targets);
            }
        }
        Iterator j = parallel.getTransitionsList().iterator();
        while (j.hasNext()) {
            updateTransition((Transition) j.next(), targets);
        }
    }

    /**
     * Update this Transition object (part of post-digestion processing).
     *
     * @param transition The Transition object
     * @param targets The global Map of all transition targets
     * @throws ModelException If the object model is flawed
     */
    private void updateTransition(final Transition transition, final Map targets) throws IOException {
        String next = transition.getNext();
        if (next == null) { // stay transition
            return;
        }
        List tts = transition.getTargets();
        if (tts.isEmpty()) {
            // 'next' is a space separated list of transition target IDs
            StringTokenizer ids = new StringTokenizer(next);
            while (ids.hasMoreTokens()) {
                String id = ids.nextToken();
                TransitionTarget tt = (TransitionTarget) targets.get(id);
                if (tt == null) {
                    logAndThrowModelError(transition, "target", ERR_TARGET_NOT_FOUND, new Object[]{
                        id});
                }
                tts.add(tt);
            }
            if (tts.size() > 1) {
                boolean legal = verifyTransitionTargets(tts);
                if (!legal) {
                    logAndThrowModelError(transition, "target", ERR_ILLEGAL_TARGETS, new Object[]{
                        next});
                }
            }
        }
        transition.getPaths(); // init paths
    }

    /**
     * Log an error discovered in post-digestion processing.
     *
     * @param errType The type of error
     * @param msgArgs The arguments for formatting the error message
     * @throws ModelException The model error, always thrown.
     */
    private void logAndThrowModelError(Object element, final String errType, final Object[] msgArgs) throws IOException {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);

        Tag tag = tags.get(element);
        throw new TagException(tag, errMsg);
    }

    /**
     * Log an error discovered in post-digestion processing.
     *
     * @param errType The type of error
     * @param msgArgs The arguments for formatting the error message
     * @throws ModelException The model error, always thrown.
     */
    private void logAndThrowModelError(Object element, String parametr, final String errType, final Object[] msgArgs) throws IOException {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);

        Tag tag = tags.get(element);
        TagAttribute attribute = tag.getAttributes().get(parametr);

        throw new TagAttributeException(attribute, errMsg);
    }

    /**
     * Get state identifier for error message. This method is only called to
     * produce an appropriate log message in some error conditions.
     *
     * @param state The <code>State</code> object
     * @return The state identifier for the error message
     */
    private static String getStateName(final State state) {
        String badState = "anonymous state";
        if (!StateFlowHelper.isStringEmpty(state.getId())) {
            badState = "state \"" + state.getId() + "\"";
        }
        return badState;
    }

    /**
     * If a transition has multiple targets, then they satisfy the following
     * criteria.
     * <ul>
     * <li>They must belong to the regions of the same parallel</li>
     * <li>All regions must be represented with exactly one target</li>
     * </ul>
     *
     * @param tts The transition targets
     * @return Whether this is a legal configuration
     */
    private static boolean verifyTransitionTargets(final List tts) {
        if (tts.size() <= 1) { // No contention
            return true;
        }
        TransitionTarget lca = StateFlowHelper.getLCA((TransitionTarget) tts.get(0), (TransitionTarget) tts.get(1));
        if (lca == null || !(lca instanceof Parallel)) {
            return false; // Must have a Parallel LCA
        }
        Parallel p = (Parallel) lca;
        Set regions = new HashSet();
        for (int i = 0; i < tts.size(); i++) {
            TransitionTarget tt = (TransitionTarget) tts.get(i);
            while (tt.getParent() != p) {
                tt = tt.getParent();
            }
            if (!regions.add(tt)) {
                return false; // One per region
            }
        }
        return regions.size() == p.getChildren().size();
    }

    //// Error messages
    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_SCXML_NO_INIT = "no SCXML child state "
            + "\"{0}\" found; illegal initialstate for SCXML document";

    /**
     * Error message when a state element specifies an initial state which
     * cannot be found.
     */
    private static final String ERR_STATE_NO_INIT = "no initial element available for {0}";

    /**
     * Error message when a state element specifies an initial state which is
     * not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT_NULL = "Initial state null of {0}";

    /**
     * Error message when a state element specifies an initial state which is
     * not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "initial state null or not a descendant of {0}";

    /**
     * Error message when a state element specifies an initial state which is
     * not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT_DES = "initial state not a descendant of {0}";

    /**
     * Error message when a state element contains anything other than one
     * &lt;parallel&gt;, one &lt;invoke&gt; or any number of &lt;state&gt;
     * children.
     */
    private static final String ERR_STATE_BAD_CONTENTS = "{0} should "
            + "contain either one <parallel>, one <invoke> or any number of "
            + "<state> children.";

    /**
     * Error message when a referenced history state cannot be found.
     */
    private static final String ERR_STATE_NO_HIST = "Referenced history state"
            + "null for {0}";

    /**
     * Error message when a shallow history state is not a child state.
     */
    private static final String ERR_STATE_BAD_SHALLOW_HIST = "History state"
            + "for shallow history is not child for {0}";

    /**
     * Error message when a deep history state is not a descendent state.
     */
    private static final String ERR_STATE_BAD_DEEP_HIST = "History state"
            + "for deep history is not descendant for {0}";

    /**
     * Transition target is not a legal IDREF (not found).
     */
    private static final String ERR_TARGET_NOT_FOUND
            = "destination target \"{0}\" not found";

    /**
     * Transition targets do not form a legal configuration.
     */
    private static final String ERR_ILLEGAL_TARGETS
            = "destination targets \"{0}\" do not satisfy the requirements"
            + " for target regions belonging to a <parallel>";

    /**
     * Simple states should not contain a history.
     */
    private static final String ERR_HISTORY_SIMPLE_STATE
            = "Simple {0} contains history elements";

    /**
     * History does not specify a default transition target.
     */
    private static final String ERR_HISTORY_NO_DEFAULT
            = "no default target specified for history \"{0}\""
            + " belonging to {1}";

    /**
     * History specifies a bad default transition target.
     */
    private static final String ERR_HISTORY_BAD_DEFAULT
            = "default target specified for history \"{0}\""
            + " belonging to \"{1}\" is also a history";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "targettype"
     * attribute.
     */
    private static final String ERR_INVOKE_NO_TARGETTYPE = "{0} contains "
            + "with no \"targettype\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; does not specify a "src" or a
     * "srcexpr" attribute.
     */
    private static final String ERR_INVOKE_NO_SRC = "{0} contains "
            + "without a \"src\" or \"srcexpr\" attribute specified.";

    /**
     * Error message when an &lt;invoke&gt; specifies both "src" and "srcexpr"
     * attributes.
     */
    private static final String ERR_INVOKE_AMBIGUOUS_SRC = "{0} contains "
            + "with both \"src\" and \"srcexpr\" attributes specified,"
            + " must specify either one, but not both.";

}
