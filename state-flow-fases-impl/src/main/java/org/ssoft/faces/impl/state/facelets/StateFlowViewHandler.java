package org.ssoft.faces.impl.state.facelets;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Set;
import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.application.ViewHandlerWrapper;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;
import static org.ssoft.faces.impl.state.listener.StateFlowControllerListener.getControllerClientIds;
import static javax.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.task.FacesProcessHolder;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;
import static javax.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import javax.faces.state.component.UIStateChartExecutor;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewHandler extends ViewHandlerWrapper {

    private final ViewHandler wrapped;
    private final StateWebConfiguration webcfg;

    /**
     *
     * @param wrapped
     */
    public StateFlowViewHandler(ViewHandler wrapped) {
        this.wrapped = wrapped;
        webcfg = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewHandler getWrapped() {
        return this.wrapped;
    }

    @Override
    public UIViewRoot restoreView(FacesContext facesContext, String viewId) {
        //send multicast call event before view be restored for executors
        StateFlowHandler handler = StateFlowHandler.getInstance();

        String name = BEFORE_PHASE_EVENT_PREFIX
                + PhaseId.RESTORE_VIEW.getName().toLowerCase();

        if (handler.hasViewRoot(facesContext)) {

            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                    .sendId(viewId);

            SCXMLExecutor executor = handler.getRootExecutor(facesContext);
            try {
                executor.triggerEvent(eb.build());
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        UIViewRoot viewRoot = super.restoreView(facesContext, viewId);
        return viewRoot;

    }

    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewRoot) throws IOException, FacesException {

        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (!facesContext.getResponseComplete() && viewRoot != null && handler.hasViewRoot(facesContext)) {
            SCXMLExecutor executor = handler.getRootExecutor(facesContext);
            try {
                EventDispatcher ed = executor.getEventdispatcher();
                if (ed instanceof FacesProcessHolder) {
                    EventBuilder deb = new EventBuilder(ENCODE_DISPATCHER_EVENTS,
                            TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    executor.triggerEvent(deb.build());
                    ((FacesProcessHolder) ed).encodeBegin(facesContext);
                    ((FacesProcessHolder) ed).encodeEnd(facesContext);
                }
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        ArrayList<String> clientIds = getControllerClientIds(facesContext);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartExecutor) {
                    UIStateChartExecutor controller = (UIStateChartExecutor) target;
                    String controllerId = controller.getClientId(facesContext);

                    EventBuilder veb = new EventBuilder(ENCODE_DISPATCHER_EVENTS, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = controller.getExecutor();
                    if (executor != null) {
                        try {
                            EventDispatcher ed = executor.getEventdispatcher();
                            if (ed instanceof FacesProcessHolder) {
                                executor.triggerEvent(veb.build());
                                ((FacesProcessHolder) ed).encodeBegin(facesContext);
                                ((FacesProcessHolder) ed).encodeEnd(facesContext);
                            }
                        } catch (ModelException | IOException ex) {
                            throw new FacesException(ex);
                        }
                    }

                }
                return VisitResult.ACCEPT;
            });
        }

        super.renderView(facesContext, viewRoot);
    }

    @Override
    public void writeState(FacesContext context) throws IOException {
        super.writeState(context);
    }

}
