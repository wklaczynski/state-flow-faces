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
package org.ssoft.faces.impl.state.facelets;

import com.sun.faces.renderkit.RenderKitUtils;
import static com.sun.faces.util.RequestStateManager.FACES_VIEW_STATE;
import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.FacesException;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.render.ResponseStateManager;
import static javax.faces.state.StateFlow.AFTER_BUILD_VIEW;
import static javax.faces.state.StateFlow.BEFORE_BUILD_VIEW;
import static javax.faces.state.StateFlow.BEFORE_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.ENCODE_DISPATCHER_EVENTS;
import static javax.faces.state.StateFlow.FACES_EXECUTOR_VIEW_ROOT_ID;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.execute.ExecuteContextManager;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.EventDispatcher;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.task.FacesProcessHolder;
import javax.faces.view.StateManagementStrategy;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewDeclarationLanguageWrapper;
import javax.faces.view.ViewMetadata;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORYGINAL_SCXML_DEFAULT_SUFIX;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ORGINAL_SCXML_SUFIX;
import org.ssoft.faces.impl.state.el.ExecuteExpressionFactory;
import org.ssoft.faces.impl.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowViewDeclarationLanguage extends ViewDeclarationLanguageWrapper {

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();

    /**
     *
     */
    public final ViewDeclarationLanguage wrapped;
    private final StateWebConfiguration webConfig;

    /**
     *
     * @param wrapped
     */
    public StateFlowViewDeclarationLanguage(ViewDeclarationLanguage wrapped) {
        super();
        this.wrapped = wrapped;
        webConfig = StateWebConfiguration.getInstance();
    }

    @Override
    public ViewDeclarationLanguage getWrapped() {
        return wrapped;
    }

    @Override
    public ViewMetadata getViewMetadata(FacesContext context, String viewId) {
        if (handlesByOryginal(viewId)) {
            return new ScxmlViewMetadataImpl(this, viewId);
        } else {
            ViewMetadata viewMetadata = wrapped.getViewMetadata(context, viewId);
            
            return  new StateFlowViewMetadata(this, viewMetadata, viewId);
        }
    }

    private boolean handlesByOryginal(String viewId) {
        return isMatchedWithOryginalSuffix(viewId) ? true : viewId.endsWith(ORYGINAL_SCXML_DEFAULT_SUFIX);
    }

    @Override
    public UIViewRoot createView(FacesContext fc, String viewId) {
        String executorId = (String) fc.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
        }

        UIViewRoot viewRoot = super.createView(fc, viewId);

        if (executorId != null) {
            viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        return viewRoot;
    }

    @Override
    public void buildView(FacesContext fc, UIViewRoot viewRoot) throws IOException {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        String executorId = (String) fc.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
        if (executorId == null) {
            executorId = UUID.randomUUID().toString();
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        ExecuteContextManager manager = ExecuteContextManager.getManager(fc);
        String path = executorId + ":" + viewRoot.getViewId();
        ExecuteExpressionFactory.getBuildPathStack(fc).push(path);

        boolean pushed = false;

        SCXMLExecutor rootexecutor = handler.getRootExecutor(fc, executorId);

        if (rootexecutor != null) {
            String executePath = executorId;
            Context ectx = rootexecutor.getGlobalContext();
            ExecuteContext executeContext = new ExecuteContext(
                    executePath, rootexecutor, ectx);

            manager.initExecuteContext(fc, executePath, executeContext);
            pushed = manager.push(executeContext);
            
            SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);
            try {
                EventDispatcher ed = executor.getEventdispatcher();
                if (ed instanceof FacesProcessHolder) {
                    EventBuilder deb = new EventBuilder(BEFORE_BUILD_VIEW,
                            TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    executor.triggerEvent(deb.build());
                }
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        super.buildView(fc, viewRoot);

        ExecuteExpressionFactory.getBuildPathStack(fc).pop();

        if (executorId != null) {
            fc.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
        }

        if (pushed) {
            manager.pop();
        }
        
        if (!fc.getResponseComplete() && handler.hasViewRoot(fc)) {
            SCXMLExecutor executor = handler.getRootExecutor(fc, executorId);
            try {
                EventDispatcher ed = executor.getEventdispatcher();
                if (ed instanceof FacesProcessHolder) {
                    EventBuilder deb = new EventBuilder(AFTER_BUILD_VIEW,
                            TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    executor.triggerEvent(deb.build());
                }
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        List<String> clientIds = handler.getControllerClientIds(fc);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(fc, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartExecutor) {
                    UIStateChartExecutor controller = (UIStateChartExecutor) target;
                    String controllerId = controller.getClientId(fc);

                    EventBuilder veb = new EventBuilder(AFTER_BUILD_VIEW, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = null;
                    String cexecutorId = controller.getExecutorId();
                    if (cexecutorId != null) {
                        executor = handler.getRootExecutor(fc, cexecutorId);
                    }

                    if (executor != null) {
                        try {
                            EventDispatcher ed = executor.getEventdispatcher();
                            if (ed instanceof FacesProcessHolder) {
                                executor.triggerEvent(veb.build());
                                ((FacesProcessHolder) ed).encodeBegin(fc);
                                ((FacesProcessHolder) ed).encodeEnd(fc);
                            }
                        } catch (ModelException | IOException ex) {
                            throw new FacesException(ex);
                        }
                    }

                }
                return VisitResult.ACCEPT;
            });
        }
    }

    @Override
    @SuppressWarnings("null")
    public void renderView(FacesContext fc, UIViewRoot viewRoot) throws IOException {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        if (!fc.getResponseComplete() && viewRoot != null && handler.hasViewRoot(fc)) {
            SCXMLExecutor executor = handler.getViewExecutor(fc);
            try {
                EventDispatcher ed = executor.getEventdispatcher();
                if (ed instanceof FacesProcessHolder) {
                    EventBuilder deb = new EventBuilder(ENCODE_DISPATCHER_EVENTS,
                            TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    executor.triggerEvent(deb.build());
                    ((FacesProcessHolder) ed).encodeBegin(fc);
                    ((FacesProcessHolder) ed).encodeEnd(fc);
                }
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        List<String> clientIds = handler.getControllerClientIds(fc);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(fc, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartExecutor) {
                    UIStateChartExecutor controller = (UIStateChartExecutor) target;
                    String controllerId = controller.getClientId(fc);

                    EventBuilder veb = new EventBuilder(ENCODE_DISPATCHER_EVENTS, TriggerEvent.CALL_EVENT)
                            .sendId(viewRoot.getViewId());

                    SCXMLExecutor executor = null;
                    String executorId = controller.getExecutorId();
                    if (executorId != null) {
                        executor = handler.getRootExecutor(fc, executorId);
                    }

                    if (executor != null) {
                        try {
                            EventDispatcher ed = executor.getEventdispatcher();
                            if (ed instanceof FacesProcessHolder) {
                                executor.triggerEvent(veb.build());
                                ((FacesProcessHolder) ed).encodeBegin(fc);
                                ((FacesProcessHolder) ed).encodeEnd(fc);
                            }
                        } catch (ModelException | IOException ex) {
                            throw new FacesException(ex);
                        }
                    }

                }
                return VisitResult.ACCEPT;
            });
        }
        super.renderView(fc, viewRoot);
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

    private boolean isMatchedWithOryginalSuffix(String viewId) {
        String[] defaultsuffixes = webConfig.getOptionValues(ORGINAL_SCXML_SUFIX, " ");
        for (String suffix : defaultsuffixes) {
            if (viewId.endsWith(suffix)) {
                return true;
            }
        }

        return false;
    }

    private String getMatchedWithOryginalSuffix(String viewId) {
        String[] defaultsuffixes = webConfig.getOptionValues(ORGINAL_SCXML_SUFIX, " ");
        for (String suffix : defaultsuffixes) {
            if (viewId.endsWith(suffix)) {
                return suffix;
            }
        }
        return null;
    }

    @Override
    public StateManagementStrategy getStateManagementStrategy(FacesContext context, String viewId) {
        StateManagementStrategy parent = super.getStateManagementStrategy(context, viewId);

        return new StateManagementStrategy() {

            @Override
            public Object saveView(FacesContext context) {
                Object[] rawState = (Object[]) parent.saveView(context);

                UIViewRoot viewRoot = context.getViewRoot();
                String executorId = null;
                String viewId = null;
                if (viewRoot != null) {
                    viewId = viewRoot.getViewId();
                    executorId = (String) viewRoot.getAttributes().get(FACES_EXECUTOR_VIEW_ROOT_ID);
                }

                if (executorId != null) {
                    if (rawState != null) {
                        Map<String, Object> state = (Map<String, Object>) rawState[1];
                        if (state != null) {
                            state.put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
                        }
                    }
                }
                return rawState;
            }

            @Override
            public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
                String executorId = null;

                ResponseStateManager rsm = RenderKitUtils.getResponseStateManager(context, renderKitId);
                Object[] rawState = (Object[]) rsm.getState(context, viewId);
                if (rawState != null) {
                    Map<String, Object> state = (Map<String, Object>) rawState[1];
                    if (state != null) {
                        executorId = (String) state.get(FACES_EXECUTOR_VIEW_ROOT_ID);
                    }
                }
                if (executorId == null) {
                    executorId = UUID.randomUUID().toString();
                }
                UIViewRoot viewRoot = parent.restoreView(context, viewId, renderKitId);

                if (executorId != null) {
                    viewRoot.getAttributes().put(FACES_EXECUTOR_VIEW_ROOT_ID, executorId);
                }

                return viewRoot;
            }
        };
    }

}
