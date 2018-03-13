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
public class ModelException extends Exception {

    /**
     * @see java.lang.Exception#Exception()
     */
    public ModelException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String)
     * @param message
     */
    public ModelException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     * @param cause
     */
    public ModelException(final Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception#Exception(String, java.lang.Throwable)
     * @param message
     * @param cause
     */
    public ModelException(final String message, final Throwable cause) {
        super(message, cause);
    }

}

