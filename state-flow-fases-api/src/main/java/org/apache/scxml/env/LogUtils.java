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
package org.apache.scxml.env;

import org.apache.scxml.model.Transition;
import org.apache.scxml.model.TransitionTarget;

/**
 * Helper methods for Commons SCXML logging.
 *
 */
public final class LogUtils {

    /**
     * Create a human readable log view of this transition.
     *
     * @param from The source TransitionTarget
     * @param to The destination TransitionTarget
     * @param transition The Transition that is taken
     * @param event The event name triggering the transition
     * @return String The human readable log entry
     */
    public static String transToString(final TransitionTarget from,
            final TransitionTarget to, final Transition transition, String event) {
        return "(" + "event = " + event +
                ", cond = " + transition.getCond() +
                ", from = " + getTTPath(from) +
                ", to = " + getTTPath(to) +
                ')';
    }

    /**
     * Write out this TransitionTarget location in a XPath style format.
     *
     * @param tt The TransitionTarget whose &quot;path&quot; is to needed
     * @return String The XPath style location of the TransitionTarget within
     *                the SCXML document
     */
    public static String getTTPath(final TransitionTarget tt) {
        StringBuilder sb = new StringBuilder("/");
        for (int i = 0; i < tt.getNumberOfAncestors(); i++) {
            sb.append(tt.getAncestor(i).getId());
            sb.append("/");
        }
        sb.append(tt.getId());
        return sb.toString();
    }

    /**
     * Discourage instantiation since this is a utility class.
     */
    private LogUtils() {
        super();
    }

}
