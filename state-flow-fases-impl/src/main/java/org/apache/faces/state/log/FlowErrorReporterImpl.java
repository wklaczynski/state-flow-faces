/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.log;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.faces.state.FlowErrorReporter;
import javax.faces.state.model.State;
import javax.faces.state.model.StateChart;
import javax.faces.state.model.TransitionTarget;
import javax.faces.state.semantics.ErrorConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.faces.state.utils.LogUtils;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowErrorReporterImpl implements FlowErrorReporter, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Log. */
    private final Log log = LogFactory.getLog(getClass());

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
        }
        if (log.isWarnEnabled()) {
            log.warn(msg.toString());
        }
    }

}

