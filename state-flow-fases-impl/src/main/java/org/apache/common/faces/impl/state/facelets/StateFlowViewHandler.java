package org.apache.common.faces.impl.state.facelets;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import java.io.IOException;
import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.apache.common.faces.impl.state.config.StateWebConfiguration;
import static org.apache.common.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.task.FacesProcessHolder;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.EventDispatcher;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;
import static org.apache.common.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import static org.apache.common.faces.state.StateFlow.VIEW_ENCODE_BEGIN;
import static org.apache.common.faces.state.StateFlow.VIEW_ENCODE_END;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewHandler extends ViewHandlerWrapper {

    private final ViewHandler wrapped;
    private final StateWebConfiguration webcfg;

    public StateFlowViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
        webcfg = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public UIViewRoot restoreView(FacesContext context, String viewId) {
        //send multicast call event before view be restored for executors
        StateFlowHandler fh = StateFlowHandler.getInstance();
        if (fh.isActive(context)) {

            String name = BEFORE_PHASE_EVENT_PREFIX
                    + PhaseId.RESTORE_VIEW.getName().toLowerCase();

            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                    .sendId(viewId);

            SCXMLExecutor rootExecutor = fh.getRootExecutor(context);
            try {
                rootExecutor.triggerEvent(eb.build());
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }
        return super.restoreView(context, viewId);
    }

    @Override
    public void renderView(FacesContext context, UIViewRoot viewRoot) throws IOException, FacesException {
        StateFlowHandler fh = StateFlowHandler.getInstance();
        if (!context.getResponseComplete() && viewRoot != null && fh.isActive(context)) {
            SCXMLExecutor rootExecutor = fh.getRootExecutor(context);
            try {
                EventBuilder eb = new EventBuilder(VIEW_ENCODE_BEGIN,
                        TriggerEvent.CALL_EVENT)
                        .sendId(viewRoot.getViewId());
                rootExecutor.triggerEvent(eb.build());

                EventDispatcher ed = rootExecutor.getEventdispatcher();
                if (ed instanceof FacesProcessHolder) {
                    EventBuilder deb = new EventBuilder(ENCODE_DISPATCHER_EVENTS,
                            TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    rootExecutor.triggerEvent(deb.build());
                    ((FacesProcessHolder) ed).encodeBegin(context);
                    ((FacesProcessHolder) ed).encodeEnd(context);
                }
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        super.renderView(context, viewRoot);

        if (!context.getResponseComplete() && viewRoot != null && fh.isActive(context)) {
            SCXMLExecutor rootExecutor = fh.getRootExecutor(context);
            try {
                EventBuilder eb = new EventBuilder(VIEW_ENCODE_END,
                        TriggerEvent.CALL_EVENT)
                        .sendId(viewRoot.getViewId());

                rootExecutor.triggerEvent(eb.build());
                
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

    }

}
