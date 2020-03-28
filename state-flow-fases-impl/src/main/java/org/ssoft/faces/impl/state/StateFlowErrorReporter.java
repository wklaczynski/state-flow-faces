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
package org.ssoft.faces.impl.state;

import org.ssoft.faces.impl.state.log.LogUtils;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.view.facelets.FaceletException;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagException;
import org.ssoft.faces.impl.state.log.FlowLogger;
import javax.faces.state.scxml.ErrorReporter;
import javax.faces.state.scxml.model.Data;
import javax.faces.state.scxml.model.EnterableState;
import javax.faces.state.scxml.model.Executable;
import javax.faces.state.scxml.model.Final;
import javax.faces.state.scxml.model.Invoke;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.state.scxml.model.Send;
import javax.faces.state.scxml.model.State;
import javax.faces.state.scxml.model.TransitionTarget;
import javax.faces.state.scxml.semantics.ErrorConstants;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.model.SCComponent;

/**
 * Custom error reporter that log execution errors.
 */
public class StateFlowErrorReporter implements ErrorReporter, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;
    /**
     * Log.
     */
    protected static final Logger log = FlowLogger.SCXML.getLogger();
    private final Map<Object, Object> tags;

    /**
     * Constructor.
     */
    public StateFlowErrorReporter() {
        super();
        this.tags = new HashMap<>();
    }

    /**
     *
     * @return
     */
    public Map<Object, Object> getTags() {
        return tags;
    }

    /**
     * @param errorCode
     * @param cause
     * @see ErrorReporter#onError(String, String, Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void onError(final String errorCode, final String errDetail,
            Object errCtx, String parametrName, Throwable cause) {
        //Note: the if-then-else below is based on the actual usage
        // (codebase search), it has to be kept up-to-date as the code changes

        TagAttribute tattr = null;
        Tag tag = (Tag) tags.get(errCtx);

        if (tag == null) {
            if (errCtx instanceof SCComponent) {
                SCComponent component = (SCComponent) errCtx;
                tag = component.getTag();
            }
        }

        if (tag != null && parametrName != null) {
            tattr = tag.getAttributes().get(parametrName);
        }

        if (errCtx instanceof Final) {
            errCtx = ((Final) errCtx).getParent();
        }
        if (errCtx instanceof Invoke) {
            errCtx = ((Invoke) errCtx).getParent();
        }
        if (errCtx instanceof Send) {
            errCtx = ((Send) errCtx).getParent();
        }

        String errCode = errorCode.intern();

        StringBuffer msg = new StringBuffer();
        if (tattr != null) {
            msg.append(tattr).append(" ");
        }

        if (null != errCode) {
            switch (errCode) {
                case ErrorConstants.NO_INITIAL:
                    if (errCtx instanceof SCXML) {
                        //determineInitialStates
                        msg.append("<SCXML>");
                    } else if (errCtx instanceof State) {
                        //determineInitialStates
                        //determineTargetStates
                        msg.append("State ").append(LogUtils.getTTPath((State) errCtx));
                    }
                    break;
                case ErrorConstants.UNKNOWN_ACTION:
                    //executeActionList
                    msg.append("action: ").append(errCtx.getClass().getName());
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
                    }
                    break;
                case ErrorConstants.EXPRESSION_ERROR:
                    if (errCtx instanceof Executable) {
                        TransitionTarget parent = ((Executable) errCtx).getParent();
                        msg.append("expression error inside ").append(LogUtils.getTTPath(parent));
                    } else if (errCtx instanceof Data) {
                        // Data expression error
                        msg.append("expression error for data element with id ").append(((Data) errCtx).getId());
                    } else if (errCtx instanceof SCXML) {
                        // Global Script
                        msg.append("expression error inside the global script");
                    }
                    break;
                default:
                    break;
            }

            msg.append(", ");
            msg.append(errDetail);

        } else {
            msg.append(errCode).append(", ");
            msg.append(errDetail);
        }

        handleErrorMessage(tag, errorCode, errDetail, errCtx, msg, cause);
    }

    /**
     * Final handling of the resulting errorMessage build by
     * {@link #onError(String, String, Object)} onError}.
     * <p>
     * The default implementation write the errorMessage as a warning to the
     * log.</p>
     *
     * @param tag
     * @param errorCode one of the ErrorReporter's constants
     * @param errDetail human readable description
     * @param errCtx typically an SCXML element which caused an error, may be
     * accompanied by additional information
     * @param errorMessage human readable detail of the error including the
     * state, transition and data
     * @param cause
     */
    protected void handleErrorMessage(Tag tag, String errorCode, String errDetail,
            Object errCtx, CharSequence errorMessage, Throwable cause) {

        if (tag != null) {
            if (cause instanceof TagException) {
                throw (TagException) cause;
            } else {
                throw new TagException(tag, errorMessage.toString(), cause);
            }
        } else {
            if (cause instanceof FacesException) {
                throw (FacesException) cause;
            } else if (cause instanceof FaceletException) {
                throw (FaceletException) cause;
            } else if (cause instanceof InvokerException) {
                InvokerException ie = (InvokerException) cause;
                throw new FaceletException(ie.toString(), cause);
            } else {
                throw new FaceletException(errorMessage.toString(), cause);
            }
        }
    }

    /**
     *
     * @param cause
     * @return
     */
    protected boolean isTagExeption(Throwable cause) {
        boolean result = false;

        return result;
    }

}
