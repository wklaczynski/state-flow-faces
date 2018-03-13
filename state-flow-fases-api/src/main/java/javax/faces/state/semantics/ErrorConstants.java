/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state.semantics;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ErrorConstants {

    /**
     * Missing initial state for a composite state or for the root.
     */
    public static final String NO_INITIAL = "NO_INITIAL";

    /**
     * An initial state for a composite state whose Transition does not.
     * Map to a descendant of the composite state.
     *
     */
    public static final String ILLEGAL_INITIAL = "ILLEGAL_INITIAL";

    /**
     * Unknown action - unsupported executable content. List of supported.
     * actions: assign, cancel, elseif, else, if, log, send, var
     */
    public static final String UNKNOWN_ACTION = "UNKNOWN_ACTION";

    /**
     * Illegal state machine configuration.
     * Either a parallel exists which does not have all its AND sub-states
     * active or there are multiple enabled OR states on the same level.
     */
    public static final String ILLEGAL_CONFIG = "ILLEGAL_CONFIG";

    /**
     * A variable referred to by assign name attribute is undefined.
     */
    public static final String UNDEFINED_VARIABLE = "UNDEFINED_VARIABLE";

    /**
     * An expression language error.
     */
    public static final String EXPRESSION_ERROR = "EXPRESSION_ERROR";

    //---------------------------------------------- STATIC CONSTANTS ONLY

    /**
     * Discourage instantiation.
     */
    private ErrorConstants() {
        super(); // humor checkstyle
    }

}
