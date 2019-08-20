package org.ssoft.faces.impl.state.facelets;

/*
 * SCXMLViewHandler.java
 *
 * Created on 6 listopad 2007, 21:38
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
import com.sun.faces.renderkit.RenderKitUtils;
import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
import javax.faces.render.ResponseStateManager;
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
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.Context;

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
    public UIViewRoot createView(FacesContext fc, String viewId) {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = (String) fc.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        boolean pushed = false;

        SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);

        if (executor != null) {
            String executePath = executorId;
            Context ectx = executor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, executor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);
        }

        UIViewRoot viewRoot = super.createView(fc, viewId);

        if (executorId != null) {
            viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        if (pushed) {
            manager.pop();
        }

        return viewRoot;
    }

    @Override
    public UIViewRoot restoreView(FacesContext fc, String viewId) {

        String executorId = null;
        Object[] rawState = (Object[]) fc.getAttributes().get(FACES_VIEW_STATE);

        if (rawState == null) {
            ViewHandler vh = fc.getApplication().getViewHandler();
            String renderKitId = vh.calculateRenderKitId(fc);
            ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(fc, renderKitId);
            rawState = (Object[]) rsm.getState(fc, viewId);
        }

        if (rawState != null) {
            Map<String, Object> state = (Map<String, Object>) rawState[1];
            if (state != null) {
                executorId = (String) state.get(FACES_EXECUTOR_VIEW_ROOT_ID);
            }
        }
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
        }

        fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);

        SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        boolean pushed = false;

        if (executor != null) {
            String executePath = executorId;
            Context ectx = executor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, executor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);

            String name = BEFORE_PHASE_EVENT_PREFIX
                    + PhaseId.RESTORE_VIEW.getName().toLowerCase();

            EventBuilder eb = new EventBuilder(name, TriggerEvent.CALL_EVENT)
                    .sendId(viewId);

            try {
                executor.triggerEvent(eb.build());
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        UIViewRoot viewRoot = super.restoreView(fc, viewId);

        if (executorId != null) {
            viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        if (pushed) {
            manager.pop();
        }
        return viewRoot;

    }

    @Override
    public void initView(FacesContext context) throws FacesException {
        super.initView(context);
    }

    @Override
    public void renderView(FacesContext facesContext, UIViewRoot viewRoot) throws IOException, FacesException {

        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (!facesContext.getResponseComplete() && viewRoot != null && handler.hasViewRoot(facesContext)) {
            SCXMLExecutor executor = handler.getViewExecutor(facesContext);
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

                    SCXMLExecutor executor = null;
                    String executorId = controller.getExecutorId();
                    if (executorId != null) {
                        executor = handler.getRootExecutor(facesContext, executorId);
                    }

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
