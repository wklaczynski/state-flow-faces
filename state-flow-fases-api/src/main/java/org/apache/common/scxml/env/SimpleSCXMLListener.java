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
package org.apache.common.scxml.env;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.common.scxml.SCXMLListener;
import org.apache.common.scxml.SCXMLLogger;
import org.apache.common.scxml.model.EnterableState;
import org.apache.common.scxml.model.Transition;
import org.apache.common.scxml.model.TransitionTarget;



/**
 * Simple SCXML Listener that logs execution.
 */
public class SimpleSCXMLListener implements SCXMLListener, Serializable {

    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** Implementation independent log category. */
    protected static final Logger log = SCXMLLogger.SCXML.getLogger();

    /**
     *
     */
    public SimpleSCXMLListener() {
    }
    
    /**
     * @param state
     * @see SCXMLListener#onEntry(EnterableState)
     */
    @Override
    public void onEntry(final EnterableState state) {
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, "enter {0}", LogUtils.getTTPath(state));
        }
    }

    /**
     * @see SCXMLListener#onExit(EnterableState)
     */
    @Override
    public void onExit(final EnterableState state) {
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, "exit {0}", LogUtils.getTTPath(state));
        }
    }

    /**
* @see SCXMLListener#onTransition(TransitionTarget,TransitionTarget,Transition,String)
     */
    @Override
    public void onTransition(final TransitionTarget from, final TransitionTarget to, final Transition transition, String event) {
        if (log.isLoggable(Level.INFO)) {
            log.log(Level.INFO, "transition {0}", LogUtils.transToString(from, to, transition, event));
        }
    }

}

