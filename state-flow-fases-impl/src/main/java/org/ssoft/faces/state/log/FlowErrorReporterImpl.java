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
import java.util.Iterator;
import java.util.Map;
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

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Log. */
    public static final Logger log = FlowLogger.FLOW.getLogger();

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
    @SuppressWarnings({"ConvertToStringSwitch", "StringEquality"})
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
                TransitionTarget tt = (TransitionTarget)
                    (((Map.Entry) errCtx).getKey());
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
        FacesMessage message = new FacesMessage(msg.toString());
        message.setSeverity(FacesMessage.SEVERITY_ERROR);
        fc.getMessageList().add(message);
        
    }

}

