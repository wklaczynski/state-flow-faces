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
package org.apache.faces.impl.state.log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import org.apache.faces.impl.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public enum FlowLogger {

    FACES("faces"),
    FLOW("flow"),
    CDI("cdi"),
    PARSER("parser"),
    APPLICATION("apps"),
    TAGLIB("taglib");

    private static final String LOGGER_RESOURCES
            = "org.apache.faces.impl.state.LogStrings";
    public static final String FACES_LOGGER_NAME_PREFIX
            = "javax.enterprise.resource.webcontainer.sfjsf.";
    private final String loggerName;

    FlowLogger(String loggerName) {
        this.loggerName = FACES_LOGGER_NAME_PREFIX + loggerName;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public String getResourcesName() {
        return LOGGER_RESOURCES;
    }

    public Logger getLogger() {
        return Logger.getLogger(loggerName, LOGGER_RESOURCES);
    }

    public String interpolateMessage(FacesContext context,
            String messageId,
            Object[] params) {
        String result;
        ResourceBundle rb;
        UIViewRoot root = context.getViewRoot();
        Locale curLocale;
        ClassLoader loader = Util.getCurrentLoader(this);
        if (null == root) {
            curLocale = Locale.getDefault();
        } else {
            curLocale = root.getLocale();
        }
        try {
            rb = ResourceBundle.getBundle(getResourcesName(), curLocale, loader);
            String message = rb.getString(messageId);
            if (params != null) {
                result = MessageFormat.format(message, params);
            } else {
                result = message;
            }
        } catch (MissingResourceException mre) {
            result = messageId;
        }

        return result;
    }

}
