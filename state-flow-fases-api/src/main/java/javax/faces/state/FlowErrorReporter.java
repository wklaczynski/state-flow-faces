/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

/**
 *
 * @author Waldemar Kłaczyński
 */
public interface FlowErrorReporter {

    /**
     * Handler for reporting an error.
     *
     * @param errCode
     *            one of the ErrorReporter's constants
     * @param errDetail
     *            human readable description
     * @param errCtx
     *            typically an SCXML element which caused an error,
     *            may be accompanied by additional information
     */
    void onError(String errCode, String errDetail, Object errCtx);

    /**
     * Missing initial state for a composite state or for the scxml root.
     *
     * @see org.apache.commons.scxml.model.SCXML#getInitialState()
     * @see org.apache.commons.scxml.model.State#getInitial()
     *
     * @deprecated Use {@link ErrorConstants#NO_INITIAL} instead.
     */
    String NO_INITIAL = "NO_INITIAL";

    /**
     * An initial state for a composite state whose Transition does not.
     * Map to a descendant of the composite state.
     *
     * @deprecated Use {@link ErrorConstants#ILLEGAL_INITIAL} instead.
     */
    String ILLEGAL_INITIAL = "ILLEGAL_INITIAL";

    /**
     * Unknown action - unsupported executable content. List of supported.
     * actions: assign, cancel, elseif, else, if, log, send, var
     *
     * @deprecated Use {@link ErrorConstants#UNKNOWN_ACTION} instead.
     */
    String UNKNOWN_ACTION = "UNKNOWN_ACTION";

    /**
     * Illegal state machine configuration.
     * Either a parallel exists which does not have all its AND sub-states
     * active or there are multiple enabled OR states on the same level.
     *
     * @deprecated Use {@link ErrorConstants#ILLEGAL_CONFIG} instead.
     */
    String ILLEGAL_CONFIG = "ILLEGAL_CONFIG";

    /**
     * Non-deterministic situation has occured - there are more than
     * one enabled transitions in conflict.
     *
     * @deprecated No longer necessary, non determinism is solved based
     *             on state heirarchy and document order priorities.
     */
    String NON_DETERMINISTIC = "NON_DETERMINISTIC";

    /**
     * A variable referred to by assign name attribute is undefined.
     *
     * @deprecated Use {@link ErrorConstants#UNDEFINED_VARIABLE} instead.
     */
    String UNDEFINED_VARIABLE = "UNDEFINED_VARIABLE";

    /**
     * An expression language error.
     *
     * @deprecated Use {@link ErrorConstants#EXPRESSION_ERROR} instead.
     */
    String EXPRESSION_ERROR = "EXPRESSION_ERROR";

}
