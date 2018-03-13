/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.scxml.io;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class SimpleErrorHandler  implements ErrorHandler, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Message prefix. */
    private static final String MSG_PREFIX = "SCXML SAX Parsing: ";
    /** Message postfix. */
    private static final String MSG_POSTFIX = " Correct the SCXML document.";

    /** Log. */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * Constructor.
     */
    public SimpleErrorHandler() {
        super();
    }

    /**
     * @see ErrorHandler#error(SAXParseException)
     */
    @Override
    public void error(final SAXParseException exception) {
        if (log.isErrorEnabled()) {
            log.error(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX, exception);
        }
    }

    /**
     * @see ErrorHandler#fatalError(SAXParseException)
     */
    @Override
    public void fatalError(final SAXParseException exception) {
        if (log.isFatalEnabled()) {
            log.fatal(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX, exception);
        }
    }

    /**
     * @see ErrorHandler#warning(SAXParseException)
     */
    @Override
    public void warning(final SAXParseException exception) {
        if (log.isWarnEnabled()) {
            log.warn(MSG_PREFIX + exception.getMessage() + MSG_POSTFIX,
                exception);
        }
    }
}

