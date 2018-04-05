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
package org.apache.common.faces.state.task;

import java.util.List;
import org.apache.common.scxml.TriggerEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public abstract class TimerEventProducer {
    
    /**
     *
     */
    protected TimerEventProducer wrapped;

    /**
     *
     */
    public TimerEventProducer() {
        this.wrapped = null;
    }

    /**
     *
     * @return
     */
    public TimerEventProducer getWrapped() {
        return wrapped;
    }

    /**
     *
     * @param wrapped
     */
    public void setWrapped(TimerEventProducer wrapped) {
        this.wrapped = wrapped;
    }
    
    /**
     * Execute the specified timer task for execution at the specified time with
     * the specified period, in milliseconds. If period is positive, the task is
     * scheduled for repeated execution; if period is zero, the task is
     * scheduled for one-time execution. Time is specified in Date.getTime()
     * format. This method checks timer state, task state, and initial execution
     * time, but not period.
     *
     * @param task
     * @return true if execute sucess
     * @throws NullPointerException if {@code task} is null
     */
    public boolean execute(DelayedEventTask task) {
        long currentTime = System.currentTimeMillis();
        if (currentTime >= task.getTime()) {
            TriggerEvent event = task.getEvent();
            task.getTarget().addEvent(event);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Cancels this event task. If the event task has been scheduled for
     * one-time execution and has not yet run, or has not yet been scheduled, it
     * will never run. If the task has been scheduled for repeated execution, it
     * will never run again. (If the task is running when this call occurs, the
     * task will run to completion, but will never run again.)
     *
     * <p>
     * Note that calling this method from within the <tt>run</tt> method of a
     * repeating timer task absolutely guarantees that the timer task will not
     * run again.
     *
     * <p>
     * This method may be called repeatedly; the second and subsequent calls
     * have no effect.
     *
     * @param task
     * @return true if cancel sucess
     */
    public boolean cancel(DelayedEventTask task) {
        return true;
    }

    /**
     * The action to be performed by this timer task.
     *
     * @param taskList list of tasks to encodeBegin
     */
    public abstract void encodeBegin(List<DelayedEventTask> taskList);

    /**
     *
     */
    public abstract void encodeEnd();
    
}
