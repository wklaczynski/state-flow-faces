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
package org.ssoft.faces.impl.state.log;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class WebMessage {
    
    /**
     * Log.
     */
    public static final Logger log = FlowLogger.APPLICATION.getLogger();

    private static final String FACES_MESSAGES_BUNDLE = "org.ssoft.faces.impl.state.messages";
    private static final String DETAIL_SUFFIX = "_detail";
    private static final int SUMMARYID = 0;
    private static final int DETAILID = 1;
    
    /**
     *
     * @param fc
     * @param message
     * @param params
     */
    public static void info(FacesContext fc, String message, Object... params) {
        FacesMessage facesMessage = getFacesMessage(fc, message, params);
        facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
        fc.addMessage(null, facesMessage);
    }

    /**
     *
     * @param fc
     * @param message
     * @param params
     */
    public static void error(FacesContext fc, String message, Object... params) {
        FacesMessage facesMessage = getFacesMessage(fc, message, params);
        facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
        fc.addMessage(null, facesMessage);
    }

    /**
     *
     * @param fc
     * @param message
     * @param params
     */
    public static void warn(FacesContext fc, String message, Object... params) {
        FacesMessage facesMessage = getFacesMessage(fc, message, params);
        facesMessage.setSeverity(FacesMessage.SEVERITY_WARN);
        fc.addMessage(null, facesMessage);
    }

    /**
     *
     * @param fc
     * @param message
     * @param params
     */
    public static void fatal(FacesContext fc, String message, Object... params) {
        FacesMessage facesMessage = getFacesMessage(fc, message, params);
        facesMessage.setSeverity(FacesMessage.SEVERITY_FATAL);
        fc.addMessage(null, facesMessage);
    }
    
    /**
     *
     * @param fc
     * @param message
     * @param params
     * @return
     */
    public static FacesMessage getFacesMessage(FacesContext fc, String message, Object... params) {
        return getFacesMessage(fc, message, null, params);
    }

    /**
     *
     * @param fc
     * @param summary
     * @param detail
     * @param params
     * @return
     */
    public static FacesMessage getFacesMessage(FacesContext fc, String summary, String detail, Object... params) {
        String messageInfo[] = new String[2];
        Locale locale;

        String summaryId = summary;
        String detailId = detail;

        if (detailId == null) {
            detailId = summaryId + DETAIL_SUFFIX;
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext != null && facesContext.getViewRoot() != null) {
            locale = facesContext.getViewRoot().getLocale();
            if (locale == null) {
                locale = Locale.getDefault();
            }
        } else {
            locale = Locale.getDefault();
        }
        String bundleName = fc.getApplication().getMessageBundle();
        if (bundleName != null) {
            try {
                loadMessageInfo(bundleName, locale, summaryId, detailId, messageInfo);
            } catch (Exception e) {
                log.log(Level.WARNING, FACES_MESSAGES_BUNDLE, e);
            }
        }
        if (messageInfo[SUMMARYID] == null && messageInfo[DETAILID] == null) {
            loadMessageInfo(FACES_MESSAGES_BUNDLE, locale, summaryId, detailId, messageInfo);
        }
        if (messageInfo[SUMMARYID] == null && messageInfo[DETAILID] == null) {
            loadMessageInfo(FacesMessage.FACES_MESSAGES, locale, summaryId, detailId, messageInfo);
        }
        if (messageInfo[SUMMARYID] == null && messageInfo[DETAILID] == null) {
            messageInfo[SUMMARYID] = summary;
            messageInfo[DETAILID] = detail;
        }
        if (params != null) {
            for (int i = 0; i < messageInfo.length; i++) {
                if (messageInfo[i] != null) {
                    messageInfo[i] = new MessageFormat(messageInfo[i], locale).format(params);
                }
            }
        }

        if (messageInfo[DETAILID] == null) {
            messageInfo[DETAILID] = messageInfo[SUMMARYID];
        }

        return new FacesMessage(messageInfo[SUMMARYID], messageInfo[DETAILID]);
    }

    private static void loadMessageInfo(String bundleName, Locale locale, String summaryId, String detailId, String[] messageInfo) {
        ResourceBundle bundle = ResourceBundle.getBundle(bundleName, locale, getCurrentLoader(bundleName));
        try {
            messageInfo[SUMMARYID] = bundle.getString(summaryId);
        } catch (MissingResourceException e) {
            //messageInfo[SUMMARYID] = summaryId;
            messageInfo[SUMMARYID] = null;
        }
        try {
            messageInfo[DETAILID] = bundle.getString(detailId);
        } catch (MissingResourceException e) {
            messageInfo[DETAILID] = null;
        }
    }

    /**
     *
     * @param o
     * @return
     */
    public static ClassLoader getCurrentLoader(Object o) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null ? loader : o.getClass().getClassLoader();
    }
    
    
}
