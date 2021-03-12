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
package org.ssoft.faces.impl.state.el.xpath;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import org.ssoft.faces.impl.state.log.FlowLogger;
import org.ssoft.faces.impl.state.log.WebMessage;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class JSTLErrorHandler implements ErrorHandler {

    /**
     * Log.
     */
    public static final Logger log = FlowLogger.EL.getLogger();

    @Override
    public void warning(SAXParseException exception) throws SAXException {
        if (log.isLoggable(Level.WARNING)) {
            log.log(Level.WARNING, "XPath JSTL Warning", exception);
        }
        WebMessage.warn(FacesContext.getCurrentInstance(), exception.getMessage());
    }

    @Override
    public void error(SAXParseException exception) throws SAXException {
        if (log.isLoggable(Level.SEVERE)) {
            log.log(Level.SEVERE, "XPath JSTL Error", exception);
        }
        WebMessage.error(FacesContext.getCurrentInstance(), exception.getMessage());
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
        if (log.isLoggable(Level.SEVERE)) {
            log.log(Level.SEVERE, "XPath JSTL Error", exception);
        }
        WebMessage.fatal(FacesContext.getCurrentInstance(), exception.getMessage());
    }

}
