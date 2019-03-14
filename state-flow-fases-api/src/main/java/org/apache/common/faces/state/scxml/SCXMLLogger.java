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
package org.apache.common.faces.state.scxml;

import java.util.logging.Logger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public enum SCXMLLogger {

    /**
     *
     */
    SCXML("scxml");

    /**
     *
     */
    public static final String FACES_LOGGER_NAME_ROOT
            = "org.apache.faces.";
    private final String loggerName;

    SCXMLLogger(String loggerName) {
        this.loggerName = FACES_LOGGER_NAME_ROOT + loggerName;
    }
    
    /**
     *
     * @return
     */
    public String getLoggerName() {
        return loggerName;
    }

    /**
     *
     * @return
     */
    public Logger getLogger() {
        return Logger.getLogger(loggerName);
    }

}
