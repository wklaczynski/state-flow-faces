/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javax.faces.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.faces.state.model.State;
import javax.faces.state.model.Transition;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class FlowStep {

    /**
     * Constructor.
     */
    public FlowStep() {
        this.externalEvents = new ArrayList();
        this.beforeStatus = new FlowStatus();
        this.afterStatus = new FlowStatus();
        this.exitList = new ArrayList();
        this.entryList = new ArrayList();
        this.transitList = new ArrayList();
    }

    /**
     * @param externalEvents The external events received in this unit of
     * progression
     * @param beforeStatus The before status
     */
    public FlowStep(final Collection externalEvents, final FlowStatus beforeStatus) {
        if (externalEvents != null) {
            this.externalEvents = externalEvents;
        } else {
            this.externalEvents = new ArrayList();
        }
        if (beforeStatus != null) {
            this.beforeStatus = beforeStatus;
        } else {
            this.beforeStatus = new FlowStatus();
        }
        this.afterStatus = new FlowStatus();
        this.exitList = new ArrayList();
        this.entryList = new ArrayList();
        this.transitList = new ArrayList();
    }

    /**
     * The external events in this step.
     */
    private final Collection externalEvents;

    /**
     * The status before this step.
     */
    private FlowStatus beforeStatus;

    /**
     * The status after this step.
     */
    private FlowStatus afterStatus;

    /**
     * The list of TransitionTargets that were exited during this step.
     */
    private final List<State> exitList;

    /**
     * The list of TransitionTargets that were entered during this step.
     */
    private final List<State> entryList;

    /**
     * The list of Transitions taken during this step.
     */
    private final List<Transition> transitList;

    /**
     * @return Returns the afterStatus.
     */
    public FlowStatus getAfterStatus() {
        return afterStatus;
    }

    /**
     * @param afterStatus The afterStatus to set.
     */
    public void setAfterStatus(final FlowStatus afterStatus) {
        this.afterStatus = afterStatus;
    }

    /**
     * @return Returns the beforeStatus.
     */
    public FlowStatus getBeforeStatus() {
        return beforeStatus;
    }

    /**
     * @param beforeStatus The beforeStatus to set.
     */
    public void setBeforeStatus(final FlowStatus beforeStatus) {
        this.beforeStatus = beforeStatus;
    }

    /**
     * @return Returns the entryList.
     */
    public List<State> getEntryList() {
        return entryList;
    }

    /**
     * @return Returns the exitList.
     */
    public List<State> getExitList() {
        return exitList;
    }

    /**
     * @return Returns the externalEvents.
     */
    public Collection getExternalEvents() {
        return externalEvents;
    }

    /**
     * @return Returns the transitList.
     */
    public List<Transition> getTransitList() {
        return transitList;
    }

}
