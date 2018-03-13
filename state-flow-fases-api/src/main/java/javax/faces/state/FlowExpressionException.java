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
public class FlowExpressionException extends Exception {

    /**
     * @see java.lang.Exception#Exception()
     */
    public FlowExpressionException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String)
     * @param message The error message
     */
    public FlowExpressionException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     * @param cause The cause
     */
    public FlowExpressionException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception#Exception(String, Throwable)
     * @param message The error message
     * @param cause The cause
     */
    public FlowExpressionException(final String message,
            final Throwable cause) {
        super(message, cause);
    }

}

