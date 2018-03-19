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
package org.ssoft.faces.state.log;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.model.Invoke;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.semantics.ErrorConstants;
import org.ssoft.faces.state.utils.LogUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowErrorReporterImpl implements FlowErrorReporter, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Log.
     */
    public static final Logger log = FlowLogger.FLOW.getLogger();

    private static final String FACES_MESSAGES_BUNDLE = "org.ssoft.faces.state.messages";
    private static final String DETAIL_SUFFIX = "_detail";
    private static final int SUMMARYID = 0;
    private static final int DETAILID = 1;

    /**
     * Constructor.
     */
    public FlowErrorReporterImpl() {
        super();
    }

    /**
     * @param errorCode
     * @see ErrorReporter#onError(String, String, Object)
     */
    @Override
    @SuppressWarnings({"StringEquality", "ConvertToStringSwitch"})
    public void onError(final String errorCode, final String errDetail, final Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        String errCode = errorCode.intern();
        StringBuilder msg = new StringBuilder();
        msg.append(errCode).append(" (");
        msg.append(errDetail).append("): ");
        if (errCode == ErrorConstants.NO_INITIAL) {
            if (errCtx instanceof StateChart) {
                //determineInitialStates
                msg.append("<STATE-FLOW>");
            } else if (errCtx instanceof State) {
                //determineInitialStates
                //determineTargetStates
                msg.append("State ").append(LogUtils.getTTPath((State) errCtx));
            }
        } else if (errCode == ErrorConstants.UNKNOWN_ACTION) {
            //executeActionList
            msg.append("Action: ").append(errCtx.getClass().getName());
        } else if (errCode == ErrorConstants.ILLEGAL_CONFIG) {
            //isLegalConfig
            if (errCtx instanceof Map.Entry) {
                TransitionTarget tt = (TransitionTarget) (((Map.Entry) errCtx).getKey());
                Set vals = (Set) (((Map.Entry) errCtx).getValue());
                msg.append(LogUtils.getTTPath(tt)).append(" : [");
                for (Iterator i = vals.iterator(); i.hasNext();) {
                    TransitionTarget tx = (TransitionTarget) i.next();
                    msg.append(LogUtils.getTTPath(tx));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            } else if (errCtx instanceof Set) {
                Set vals = (Set) errCtx;
                msg.append("<STATE-FLOW> : [");
                for (Iterator i = vals.iterator(); i.hasNext();) {
                    TransitionTarget tx = (TransitionTarget) i.next();
                    msg.append(LogUtils.getTTPath(tx));
                    if (i.hasNext()) {
                        msg.append(", ");
                    }
                }
                msg.append(']');
            }
        } else if (errCode == ErrorConstants.INVOKE_ERROR) {
            if (errCtx instanceof Invoke) {
                Invoke invoke = (Invoke) errCtx;

            }
        }
        if (log.isLoggable(Level.WARNING)) {
            log.log(Level.WARNING, msg.toString());
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        error(fc, msg.toString());
        
    }

    public static void error(FacesContext fc, String message, Object... params) {
        FacesMessage facesMessage = getFacesMessage(fc, message, params);
        facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
        fc.addMessage(null, facesMessage);
    }

    public static FacesMessage getFacesMessage(FacesContext fc, String message, Object... params) {
        return getFacesMessage(fc, message, null, params);
    }

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

    public static ClassLoader getCurrentLoader(Object o) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        return loader != null ? loader : o.getClass().getClassLoader();
    }

}
