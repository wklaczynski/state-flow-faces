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
package org.ssoft.faces.impl.state.tag;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagException;
import static javax.faces.state.scxml.SCXMLConstants.META_ELEMENT_IDMAP;
import javax.faces.state.scxml.model.Action;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.Finalize;
import javax.faces.state.scxml.model.History;
import javax.faces.state.scxml.model.Initial;
import javax.faces.state.scxml.model.Invoke;
import javax.faces.state.scxml.model.OnEntry;
import javax.faces.state.scxml.model.OnExit;
import javax.faces.state.scxml.model.Parallel;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.state.scxml.model.SimpleTransition;
import javax.faces.state.scxml.model.State;
import javax.faces.state.scxml.model.Transition;
import javax.faces.state.scxml.model.TransitionTarget;
import javax.faces.state.scxml.model.TransitionalState;

/**
 * The ModelUpdater provides the utility methods to check the Commons SCXML
 * model for inconsistencies, detect errors, and wire the Commons SCXML model
 * appropriately post document parsing by the SCXMLReader to make it executor
 * ready.
 */
public final class ModelUpdater {

    //// Error messages
    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_SCXML_NO_INIT = "no SCXML child state "
            + "with id \"{0}\" found; illegal initial state for SCXML document.";

    /**
     * Error message when SCXML document specifies an illegal initial state.
     */
    private static final String ERR_UNSUPPORTED_INIT = "initial attribute or element not supported for "
            + "atomic {0}.";

    /**
     * Error message when a state element specifies an initial state which is
     * not a direct descendent.
     */
    private static final String ERR_STATE_BAD_INIT = "initial state "
            + "null or not a descendant of {0}";

    /**
     * Error message when a referenced history state cannot be found.
     */
    private static final String ERR_STATE_NO_HIST = "referenced history state"
            + " null for {0}.";

    /**
     * Error message when a shallow history state is not a child state.
     */
    private static final String ERR_STATE_BAD_SHALLOW_HIST = "History state"
            + " for shallow history is not child for {0}.";

    /**
     * Error message when a deep history state is not a descendent state.
     */
    private static final String ERR_STATE_BAD_DEEP_HIST = "History state"
            + " for deep history is not descendant for {0}.";

    /**
     * Transition target is not a legal IDREF (not found).
     */
    private static final String ERR_TARGET_NOT_FOUND
            = "transition target with id \"{0}\" not found.";

    /**
     * Transition targets do not form a legal configuration.
     */
    private static final String ERR_ILLEGAL_TARGETS
            = "transition targets \"{0}\" do not satisfy the requirements for"
            + " target regions belonging to a <parallel>.";

    /**
     * Simple states should not contain a history.
     */
    private static final String ERR_HISTORY_SIMPLE_STATE
            = "simple {0} contains history elements.";

    /**
     * History does not specify a default transition target.
     */
    private static final String ERR_HISTORY_NO_DEFAULT
            = "no default target specified for history with id \"{0}\""
            + " belonging to {1}.";

    /**
     * Error message when an &lt;invoke&gt; specifies both "src" and "srcexpr"
     * attributes.
     */
    private static final String ERR_INVOKE_AMBIGUOUS_SRC = "{0} contains "
            + "<invoke> with both \"src\" and \"srcexpr\" attributes specified,"
            + " must specify either one, but not both.";

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
     * part of post-read processing, and sets up the necessary object references
     * throughtout the SCXML object model for the parsed document.</p>
     *
     * @param scxml The SCXML object (output from SCXMLReader)
     * @throws IOException If the object model is flawed
     */
    public void updateSCXML(final SCXML scxml) throws IOException {
        initDocumentOrder(scxml.getChildren(), 1);

        String initial = scxml.getInitial();
        SimpleTransition initialTransition = new SimpleTransition();

        if (initial != null) {

            initialTransition.setNext(scxml.getInitial());
            updateTransition(initialTransition, scxml.getTargets());

            if (initialTransition.getTargets().isEmpty()) {
                logAndThrowModelError(scxml, ERR_SCXML_NO_INIT, new Object[]{
                    initial});
            }
        } else {
            // If 'initial' is not specified, the default initial state is
            // the first child state in document order.
            initialTransition.getTargets().add(scxml.getFirstChild());
        }

        scxml.setInitialTransition(initialTransition);
        Map<String, TransitionTarget> targets = scxml.getTargets();
        updateEnterableStates(scxml.getChildren(), targets);

        scxml.getInitialTransition().setObservableId(1);

        Map idmap = (Map) scxml.getMetadata().get(META_ELEMENT_IDMAP);

        initObservables(scxml.getChildren(), 2);
        updateClientId(scxml, idmap);
    }

    /**
     * Initialize all {@link org.apache.commons.scxml2.model.DocumentOrder}
     * instances (EnterableState or Transition) by iterating them in document
     * order setting their document order value.
     *
     * @param states The list of children states of a parent TransitionalState
     * or the SCXML document itself
     * @param nextOrder The next to be used order value
     * @return Returns the next to be used order value
     */
    private int initDocumentOrder(final List<EnterableState> states, int nextOrder) {
        for (EnterableState state : states) {
            state.setOrder(nextOrder++);
            if (state instanceof TransitionalState) {
                TransitionalState ts = (TransitionalState) state;
                for (Transition t : ts.getTransitionsList()) {
                    t.setOrder(nextOrder++);
                }
                nextOrder = initDocumentOrder(ts.getChildren(), nextOrder);
            }
        }
        return nextOrder;
    }

    /**
     * Initialize all {@link org.apache.commons.scxml2.model.Observable}
     * instances in the SCXML document by iterating them in document order and
     * seeding them with a unique obeservable id.
     *
     * @param states The list of children states of a parent TransitionalState
     * or the SCXML document itself
     * @param nextObservableId The next observable id sequence value to be used
     * @return Returns the next to be used observable id sequence value
     */
    private int initObservables(final List<EnterableState> states, int nextObservableId) {
        for (EnterableState es : states) {
            es.setObservableId(nextObservableId++);
            if (es instanceof TransitionalState) {
                TransitionalState ts = (TransitionalState) es;
                if (ts instanceof State) {
                    State s = (State) ts;
                    if (s.getInitial() != null && s.getInitial().getTransition() != null) {
                        s.getInitial().getTransition().setObservableId(nextObservableId++);
                    }
                }
                for (Transition t : ts.getTransitionsList()) {
                    t.setObservableId(nextObservableId++);
                }
                for (History h : ts.getHistory()) {
                    h.setObservableId(nextObservableId++);
                    if (h.getTransition() != null) {
                        h.getTransition().setObservableId(nextObservableId++);
                    }
                }
                nextObservableId = initObservables(ts.getChildren(), nextObservableId);
            }
        }
        return nextObservableId;
    }

    /**
     * Create unique clientId
     *
     * @return Returns unique id
     */
    private String uid(String nonce) {
        return uid(null, nonce);
    }

    /**
     * Create unique clientId
     *
     * @return Returns unique id
     */
    private String uid(String id, String nonce) {
        if (id != null) {
            return id;
        }
        //return String.valueOf(System.identityHashCode(nonce));
        return nonce;
    }


    /**
     * Initialize all unique clientId
     *
     * @param states The list of children states of a parent TransitionalState
     * or the SCXML document itself
     * @param nextObservableId The next observable id sequence value to be used
     * @return Returns the next to be used observable id sequence value
     */
    private void updateClientId(final SCXML scxml, final Map idmap) {
        String prefix = "scxml:";

        scxml.getInitialTransition().setClientId(prefix + "initial:transition");
        idmap.put(scxml.getInitialTransition().getClientId(), scxml.getInitialTransition());

        List<EnterableState> children = scxml.getChildren();
        updateClientId(scxml, idmap, prefix, children);
    }
    
    /**
     * Initialize all unique clientId
     *
     * @param states The list of children states of a parent TransitionalState
     * or the SCXML document itself
     * @param nextObservableId The next observable id sequence value to be used
     * @return Returns the next to be used observable id sequence value
     */
    private void updateClientId(final SCXML scxml, final Map<String, Object> idmap,
            String prefix, final List<EnterableState> states) {

        int snonce = 1;
        for (EnterableState es : states) {
            es.setClientId(prefix + uid(es.getId(), "state_" + snonce++));
            idmap.put(es.getClientId(), es);
            String tprefix = es.getClientId() + ":";

            if (es instanceof TransitionalState) {
                TransitionalState ts = (TransitionalState) es;

                int ennonce = 1;
                for (OnEntry on : ts.getOnEntries()) {
                    on.setClientId(tprefix + uid("entry_" + ennonce++));
                    idmap.put(on.getClientId(), on);

                    String aprefix = on.getClientId() + ":";
                    int anonce = 1;
                    for (Action at : on.getActions()) {
                        at.setClientId(aprefix + uid("action_" + anonce++));
                        idmap.put(at.getClientId(), at);
                    }
                }

                int exnonce = 1;
                for (OnExit on : ts.getOnExits()) {
                    on.setClientId(tprefix + uid("exit_" + exnonce++));
                    idmap.put(on.getClientId(), on);

                    String aprefix = on.getClientId() + ":";
                    int anonce = 1;
                    for (Action at : on.getActions()) {
                        at.setClientId(aprefix + uid("action_" + anonce++));
                        idmap.put(at.getClientId(), at);
                    }
                }

                int innonce = 1;
                for (Invoke iv : ts.getInvokes()) {
                    iv.setClientId(tprefix + uid(iv.getId(), "exit_" + innonce++));
                    idmap.put(iv.getClientId(), iv);

                    Finalize fi = iv.getFinalize();
                    if (fi != null) {
                        String cprefix = iv.getClientId() + ":";
                        fi.setClientId(cprefix + uid("finalize"));
                        idmap.put(fi.getClientId(), fi);

                        String aprefix = fi.getClientId() + ":";
                        int anonce = 1;
                        for (Action at : fi.getActions()) {
                            at.setClientId(aprefix + uid("action_" + anonce++));
                            idmap.put(at.getClientId(), at);
                        }
                    }
                }

                if (ts instanceof State) {
                    State s = (State) ts;
                    if (s.getInitial() != null) {
                        Initial in = s.getInitial();
                        in.setClientId(tprefix + uid("initial"));
                        idmap.put(in.getClientId(), in);

                        if (in.getTransition() != null) {
                            SimpleTransition tr = in.getTransition();
                            tr.setClientId(in.getClientId() + ":" + uid("transition"));
                            idmap.put(tr.getClientId(), tr);
                        }
                    }

                }

                int tnonce = 1;
                for (Transition t : ts.getTransitionsList()) {
                    t.setClientId(tprefix + uid("transition_" + tnonce++));
                    idmap.put(t.getClientId(), t);

                    String aprefix = t.getClientId() + ":";
                    int anonce = 1;
                    for (Action at : t.getActions()) {
                        at.setClientId(aprefix + uid("action_" + anonce++));
                        idmap.put(at.getClientId(), at);
                    }
                }

                int hnonce = 1;
                for (History h : ts.getHistory()) {
                    h.setClientId(tprefix + uid("history_" + hnonce++));
                    idmap.put(h.getClientId(), h);

                    if (h.getTransition() != null) {
                        SimpleTransition tr = h.getTransition();
                        tr.setClientId(h.getClientId() + ":" + uid("transition"));
                        idmap.put(tr.getClientId(), tr);
                    }
                }
                
                updateClientId(scxml, idmap, tprefix, ts.getChildren());
            }
        }

    }

    /**
     * Update this State object (part of post-read processing). Also checks for
     * any errors in the document.
     *
     * @param state The State object
     * @param targets The global Map of all transition targets
     * @throws IOException If the object model is flawed
     */
    private void updateState(final State state, final Map<String, TransitionTarget> targets)
            throws IOException {
        List<EnterableState> children = state.getChildren();
        if (state.isComposite()) {
            //initialize next / initial
            Initial ini = state.getInitial();
            if (ini == null) {
                state.setFirst(children.get(0).getId());
                ini = state.getInitial();
            }
            SimpleTransition initialTransition = ini.getTransition();
            updateTransition(initialTransition, targets);
            Set<TransitionTarget> initialStates = initialTransition.getTargets();
            // we have to allow for an indirect descendant initial (targets)
            //check that initialState is a descendant of s
            if (initialStates.isEmpty()) {
                logAndThrowModelError(state, ERR_STATE_BAD_INIT,
                        new Object[]{getName(state)});
            } else {
                for (TransitionTarget initialState : initialStates) {
                    if (!initialState.isDescendantOf(state)) {
                        logAndThrowModelError(state, ERR_STATE_BAD_INIT,
                                new Object[]{getName(state)});
                    }
                }
            }
        } else if (state.getInitial() != null) {
            logAndThrowModelError(state, ERR_UNSUPPORTED_INIT, new Object[]{getName(state)});
        }

        List<History> histories = state.getHistory();
        if (histories.size() > 0 && state.isSimple()) {
            logAndThrowModelError(state, ERR_HISTORY_SIMPLE_STATE,
                    new Object[]{getName(state)});
        }
        for (History history : histories) {
            updateHistory(history, targets, state);
        }
        for (Transition transition : state.getTransitionsList()) {
            updateTransition(transition, targets);
        }

        for (Invoke inv : state.getInvokes()) {
            if (inv.getSrc() != null && inv.getSrcexpr() != null) {
                logAndThrowModelError(state, ERR_INVOKE_AMBIGUOUS_SRC, new Object[]{getName(state)});
            }
        }

        updateEnterableStates(children, targets);
    }

    /**
     * Update this Parallel object (part of post-read processing).
     *
     * @param parallel The Parallel object
     * @param targets The global Map of all transition targets
     * @throws IOException If the object model is flawed
     */
    private void updateParallel(final Parallel parallel, final Map<String, TransitionTarget> targets)
            throws IOException {
        updateEnterableStates(parallel.getChildren(), targets);
        for (Transition transition : parallel.getTransitionsList()) {
            updateTransition(transition, targets);
        }
        List<History> histories = parallel.getHistory();
        for (History history : histories) {
            updateHistory(history, targets, parallel);
        }
        // TODO: parallel must may have invokes too
    }

    /**
     * Update the EnterableState objects (part of post-read processing).
     *
     * @param states The EnterableState objects
     * @param targets The global Map of all transition targets
     * @throws IOException If the object model is flawed
     */
    private void updateEnterableStates(final List<EnterableState> states,
            final Map<String, TransitionTarget> targets)
            throws IOException {
        for (EnterableState es : states) {
            if (es instanceof State) {
                updateState((State) es, targets);
            } else if (es instanceof Parallel) {
                updateParallel((Parallel) es, targets);
            }
        }
    }

    /**
     * Update this History object (part of post-read processing).
     *
     * @param history The History object
     * @param targets The global Map of all transition targets
     * @param parent The parent TransitionalState for this History
     * @throws IOException If the object model is flawed
     */
    @SuppressWarnings("element-type-mismatch")
    private void updateHistory(final History history,
            final Map<String, TransitionTarget> targets,
            final TransitionalState parent)
            throws IOException {
        SimpleTransition transition = history.getTransition();
        if (transition == null || transition.getNext() == null) {
            logAndThrowModelError(history, ERR_HISTORY_NO_DEFAULT,
                    new Object[]{history.getId(), getName(parent)});
        } else {
            updateTransition(transition, targets);
            Set<TransitionTarget> historyStates = transition.getTargets();
            if (historyStates.isEmpty()) {
                logAndThrowModelError(history, ERR_STATE_NO_HIST,
                        new Object[]{getName(parent)});
            }
            for (TransitionTarget historyState : historyStates) {
                if (!history.isDeep()) {
                    // Shallow history
                    if (!parent.getChildren().contains(historyState)) {
                        logAndThrowModelError(history, ERR_STATE_BAD_SHALLOW_HIST,
                                new Object[]{getName(parent)});
                    }
                } else {
                    // Deep history
                    if (!historyState.isDescendantOf(parent)) {
                        logAndThrowModelError(history, ERR_STATE_BAD_DEEP_HIST,
                                new Object[]{getName(parent)});
                    }
                }
            }
        }
    }

    /**
     * Update this Transition object (part of post-read processing).
     *
     * @param transition The Transition object
     * @param targets The global Map of all transition targets
     * @throws IOException If the object model is flawed
     */
    private void updateTransition(final SimpleTransition transition,
            final Map<String, TransitionTarget> targets) throws IOException {
        String next = transition.getNext();
        if (next == null) { // stay transition
            return;
        }
        Set<TransitionTarget> tts = transition.getTargets();
        if (tts.isEmpty()) {
            // 'next' is a space separated list of transition target IDs
            StringTokenizer ids = new StringTokenizer(next);
            while (ids.hasMoreTokens()) {
                String id = ids.nextToken();
                TransitionTarget tt = targets.get(id);
                if (tt == null) {
                    logAndThrowModelError(transition, ERR_TARGET_NOT_FOUND, new Object[]{
                        id});
                }
                tts.add(tt);
            }
            if (tts.size() > 1) {
                boolean legal = verifyTransitionTargets(tts);
                if (!legal) {
                    logAndThrowModelError(transition, ERR_ILLEGAL_TARGETS, new Object[]{
                        next});
                }
            }
        }
    }

    /**
     * Log an error discovered in post-read processing.
     *
     * @param errType The type of error
     * @param msgArgs The arguments for formatting the error message
     * @throws IOException The model error, always thrown.
     */
    private void logAndThrowModelError(final Object element, final String errType, final Object[] msgArgs) throws IOException {
        MessageFormat msgFormat = new MessageFormat(errType);
        String errMsg = msgFormat.format(msgArgs);

        Tag tag = tags.get(element);
        throw new TagException(tag, errMsg);
    }

    /**
     * Get a transition target identifier for error messages. This method is
     * only called to produce an appropriate log message in some error
     * conditions.
     *
     * @param tt The <code>TransitionTarget</code> object
     * @return The transition target identifier for the error message
     */
    private static String getName(final TransitionTarget tt) {
        String name = "anonymous transition target";
        if (tt instanceof State) {
            name = "anonymous state";
            if (tt.getId() != null) {
                name = "state with id \"" + tt.getId() + "\"";
            }
        } else if (tt instanceof Parallel) {
            name = "anonymous parallel";
            if (tt.getId() != null) {
                name = "parallel with id \"" + tt.getId() + "\"";
            }
        } else {
            if (tt.getId() != null) {
                name = "transition target with id \"" + tt.getId() + "\"";
            }
        }
        return name;
    }

    /**
     * If a transition has multiple targets, then they satisfy the following
     * criteria:
     * <ul>
     * <li>No target is an ancestor of any other target on the list</li>
     * <li>A full legal state configuration results when all ancestors and
     * default initial descendants have been added.
     * <br/>This means that they all must share the same least common parallel
     * ancestor.
     * </li>
     * </ul>
     *
     * @param tts The transition targets
     * @return Whether this is a legal configuration
     * @see <a href=http://www.w3.org/TR/scxml/#LegalStateConfigurations">
     * http://www.w3.org/TR/scxml/#LegalStateConfigurations</a>
     */
    @SuppressWarnings("empty-statement")
    private static boolean verifyTransitionTargets(final Set<TransitionTarget> tts) {
        if (tts.size() < 2) { // No contention
            return true;
        }
        TransitionTarget first = null;
        int i = 0;
        for (TransitionTarget tt : tts) {
            if (first == null) {
                first = tt;
                i = tt.getNumberOfAncestors();
                continue;
            }
            // find least common ancestor
            for (i = Math.min(i, tt.getNumberOfAncestors()); i > 0 && first.getAncestor(i - 1) != tt.getAncestor(i - 1); i--) ;
            if (i == 0) {
                // no common ancestor
                return false;
            }
            // ensure no target is an ancestor of any other target on the list
            for (TransitionTarget other : tts) {
                if (other != tt && other.isDescendantOf(tt) || tt.isDescendantOf(other)) {
                    return false;
                }
            }
        }
        // least common ancestor must be a parallel
        return first != null && i > 0 && first.getAncestor(i - 1) instanceof Parallel;
    }
}
