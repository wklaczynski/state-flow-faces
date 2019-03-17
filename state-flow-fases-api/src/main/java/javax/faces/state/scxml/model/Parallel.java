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
package javax.faces.state.scxml.model;

/**
 * The class in this SCXML object model that corresponds to the
 * &lt;parallel&gt; SCXML element, which is a wrapper element to
 * encapsulate parallel state machines. For the &lt;parallel&gt; element
 * to be useful, each of its &lt;state&gt; substates must itself be
 * complex, that is, one with either &lt;state&gt; or &lt;parallel&gt;
 * children.
 *
 */
public class Parallel extends TransitionalState {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 2L;

    /**
     * Constructor.
     */
    public Parallel() {
    }

    /**
     * {@inheritDoc}
     * @return Returns always false (a state of type Parallel is never atomic)
     */
    @Override
    public final boolean isAtomicState() {
        return false;
    }
    /**
     * Add a TransitionalState (State or Parallel) child
     * @param ts the child to add
     */
    public final void addChild(final TransitionalState ts) {
        super.addChild(ts);
    }
}

