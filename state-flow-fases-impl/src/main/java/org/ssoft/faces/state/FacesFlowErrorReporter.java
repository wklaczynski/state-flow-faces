/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ssoft.faces.state;

import org.ssoft.faces.state.log.LogUtils;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.scxml.ErrorReporter;
import javax.scxml.model.Data;
import javax.scxml.model.EnterableState;
import javax.scxml.model.Executable;
import javax.scxml.model.SCXML;
import javax.scxml.model.State;
import javax.scxml.model.TransitionTarget;
import javax.scxml.semantics.ErrorConstants;
import org.ssoft.faces.state.log.WebMessage;

/**
 * Custom error reporter that log execution errors.
 */
public class FacesFlowErrorReporter implements ErrorReporter, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Log.
     */
    protected static final Logger log = Logger.getLogger("javax.faces.state");

    /**
     * Constructor.
     */
    public FacesFlowErrorReporter() {
        super();
    }

    /**
     * @param errorCode
     * @see ErrorReporter#onError(String, String, Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onError(final String errorCode, final String errDetail,
            final Object errCtx) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes
        String errCode = errorCode.intern();
        StringBuffer msg = new StringBuffer();
        msg.append(errCode).append(" (");
        msg.append(errDetail).append("): ");
        if (null != errCode) switch (errCode) {
            case ErrorConstants.NO_INITIAL:
                if (errCtx instanceof SCXML) {
                    //determineInitialStates
                    msg.append("<SCXML>");
                } else if (errCtx instanceof State) {
                    //determineInitialStates
                    //determineTargetStates
                    msg.append("State ").append(LogUtils.getTTPath((State) errCtx));
                }   break;
            case ErrorConstants.UNKNOWN_ACTION:
                //executeActionList
                msg.append("Action: ").append(errCtx.getClass().getName());
                break;
            case ErrorConstants.ILLEGAL_CONFIG:
                //isLegalConfig
                if (errCtx instanceof Map.Entry) { //unchecked cast below
                    Map.Entry<EnterableState, Set<EnterableState>> badConfigMap
                            = (Map.Entry<EnterableState, Set<EnterableState>>) errCtx;
                    EnterableState es = badConfigMap.getKey();
                    Set<EnterableState> vals = badConfigMap.getValue();
                    msg.append(LogUtils.getTTPath(es)).append(" : [");
                    for (Iterator<EnterableState> i = vals.iterator(); i.hasNext();) {
                        EnterableState ex = i.next();
                        msg.append(LogUtils.getTTPath(ex));
                        if (i.hasNext()) { // reason for iterator usage
                            msg.append(", ");
                        }
                    }
                    msg.append(']');
                } else if (errCtx instanceof Set) { //unchecked cast below
                    Set<EnterableState> vals = (Set<EnterableState>) errCtx;
                    msg.append("<SCXML> : [");
                    for (Iterator<EnterableState> i = vals.iterator(); i.hasNext();) {
                        EnterableState ex = i.next();
                        msg.append(LogUtils.getTTPath(ex));
                        if (i.hasNext()) {
                            msg.append(", ");
                        }
                    }
                    msg.append(']');
                }   break;
            case ErrorConstants.EXPRESSION_ERROR:
                if (errCtx instanceof Executable) {
                    TransitionTarget parent = ((Executable) errCtx).getParent();
                    msg.append("Expression error inside ").append(LogUtils.getTTPath(parent));
                } else if (errCtx instanceof Data) {
                    // Data expression error
                    msg.append("Expression error for data element with id ").append(((Data) errCtx).getId());
                } else if (errCtx instanceof SCXML) {
                    // Global Script
                    msg.append("Expression error inside the global script");
                }   break;
            default:
                break;
        }
        handleErrorMessage(errorCode, errDetail, errCtx, msg);
    }

    /**
     * Final handling of the resulting errorMessage build by
     * {@link #onError(String, String, Object)} onError}.
     * <p>
     * The default implementation write the errorMessage as a warning to the
     * log.</p>
     *
     * @param errorCode one of the ErrorReporter's constants
     * @param errDetail human readable description
     * @param errCtx typically an SCXML element which caused an error, may be
     * accompanied by additional information
     * @param errorMessage human readable detail of the error including the
     * state, transition and data
     */
    protected void handleErrorMessage(final String errorCode, final String errDetail,
            final Object errCtx, final CharSequence errorMessage) {

        FacesContext fc = FacesContext.getCurrentInstance();
        if (fc.getApplication().getProjectStage() == ProjectStage.Production) {
            if (log.isLoggable(Level.WARNING)) {
                log.log(Level.WARNING, errorMessage.toString());
            }
            WebMessage.error(fc, errorMessage.toString());
        } else {
            throw new FacesException(errorMessage.toString());
        }
    }
}
