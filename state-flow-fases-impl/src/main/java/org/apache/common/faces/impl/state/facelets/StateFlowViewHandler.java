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
import org.apache.common.faces.impl.state.config.StateWebConfiguration;
import static org.apache.common.faces.impl.state.listener.StateFlowControllerListener.getControllerClientIds;
import static org.apache.common.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.task.FacesProcessHolder;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.EventDispatcher;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.model.ModelException;
import static org.apache.common.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import org.apache.common.faces.state.component.UIStateChartController;

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

        if (handler.isActive(facesContext)) {

            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                    .sendId(viewId);

            SCXMLExecutor executor = handler.getRootExecutor(facesContext);
            try {
                executor.triggerEvent(eb.build());
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }

            if (!executor.isRunning()) {
                handler.close(facesContext, executor);
            }

        }
        UIViewRoot viewRoot = super.restoreView(facesContext, viewId);

        ArrayList<String> clientIds = getControllerClientIds(facesContext);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartController) {
                    UIStateChartController controller = (UIStateChartController) target;
                    String controllerId = controller.getClientId(facesContext);

                    EventBuilder veb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = controller.getRootExecutor(facesContext);
                    if (executor != null) {
                        try {
                            executor.triggerEvent(veb.build());
                        } catch (ModelException ex) {
                            throw new FacesException(ex);
                        }

                        if (!executor.isRunning()) {
                            handler.close(facesContext, executor);
                        }
                    }

                }
                return VisitResult.ACCEPT;
            });
        }

        SCXMLExecutor executor = handler.getRootExecutor(facesContext);

        if (executor != null && !executor.isRunning()) {
            handler.close(facesContext, executor);
        }

        return viewRoot;

    }

    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewRoot) throws IOException, FacesException {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (!facesContext.getResponseComplete() && viewRoot != null && handler.isActive(facesContext)) {
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

            if (!executor.isRunning()) {
                handler.close(facesContext, executor);
            }

        }

        ArrayList<String> clientIds = getControllerClientIds(facesContext);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(facesContext, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartController) {
                    UIStateChartController controller = (UIStateChartController) target;
                    String controllerId = controller.getClientId(facesContext);

                    EventBuilder veb = new EventBuilder(ENCODE_DISPATCHER_EVENTS, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = controller.getRootExecutor(facesContext);
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

                        if (!executor.isRunning()) {
                            handler.close(facesContext, executor);
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
