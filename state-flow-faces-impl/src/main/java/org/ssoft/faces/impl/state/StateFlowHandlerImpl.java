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
package org.ssoft.faces.impl.state;

import com.sun.faces.RIConstants;
import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.facelets.impl.DefaultFaceletFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.ssoft.faces.impl.state.evaluator.StateFlowEvaluator;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Logger;
import javax.el.ELContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.ProjectStage;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitHint;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextWrapper;
import javax.faces.lifecycle.ClientWindow;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.events.OnFinishEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.model.CustomAction;
import javax.servlet.ServletContext;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.ANNOTATED_CLASSES;
import javax.faces.state.component.UIStateChartMachine;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.scxml.model.Action;
import javax.faces.state.scxml.model.ModelException;
import javax.faces.state.scxml.model.SCXML;
import javax.faces.view.ViewDeclarationLanguage;
import javax.faces.view.ViewMetadata;
import javax.faces.view.facelets.Facelet;
import javax.faces.view.facelets.FaceletException;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_LOGSTEP_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_CHART_SERIALIZED_PARAM_NAME;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.STATE_FLOW_STACK;
import org.ssoft.faces.impl.state.cdi.CdiUtil;
import org.ssoft.faces.impl.state.cdi.StateFlowCDIHelper;
import org.ssoft.faces.impl.state.cdi.StateFlowCDIListener;
import org.ssoft.faces.impl.state.config.StateWebConfiguration;
import org.ssoft.faces.impl.state.invokers.SubInvoker;
import org.ssoft.faces.impl.state.invokers.ViewInvoker;
import org.ssoft.faces.impl.state.tag.faces.SetVariable;
import static javax.faces.state.StateFlow.SKIP_START_STATE_MACHINE_HINT;
import javax.faces.state.annotation.StateChartInvoker;
import javax.faces.state.annotation.StateChartAction;
import javax.faces.state.annotation.StateChartActions;
import javax.faces.state.annotation.StateChartInvokers;
import javax.faces.state.scxml.env.AbstractSCXMLListener;
import javax.faces.state.scxml.env.SimpleContext;
import javax.faces.state.scxml.env.SimpleSCXMLListener;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.FXSCXML_DATA_MODEL;
import static org.ssoft.faces.impl.state.StateFlowImplConstants.SCXML_DATA_MODEL;
import org.ssoft.faces.impl.state.invokers.FacetInvoker;
import org.ssoft.faces.impl.state.tag.faces.MethodCall;
import org.ssoft.faces.impl.state.tag.faces.Redirect;
import static org.ssoft.faces.impl.state.utils.Util.toViewId;
import javax.faces.state.execute.ExecuteContext;
import static javax.faces.state.StateFlow.BUILD_STATE_CONTINER_HINT;
import static javax.faces.state.StateFlow.BUILD_STATE_MACHINE_HINT;
import static javax.faces.state.StateFlow.CONTROLLER_SET_HINT;
import static javax.faces.state.StateFlow.PORTLET_EVENT_PREFIX;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.task.TimerEventProducer;
import javax.faces.state.scxml.ParentSCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import static javax.faces.state.scxml.io.StateHolderSaver.restoreContext;
import static javax.faces.state.scxml.io.StateHolderSaver.saveContext;
import static javax.faces.state.StateFlow.FACES_CHART_EXECUTOR_VIEW_ID;
import static javax.faces.state.StateFlow.STATE_CHART_FACET_NAME;
import javax.faces.state.component.UIStateChartExecutor;
import javax.faces.state.scxml.SCXMLSystemContext;
import javax.faces.state.execute.ExecuteContextManager;
import static javax.faces.state.StateFlow.FACES_VIEW_ROOT_EXECUTOR_ID;
import javax.faces.state.events.OnExecuteEvent;
import javax.faces.state.events.PostExecuteEvent;
import javax.faces.state.events.PreExecutorExitedEvent;

/**
 *
 * @author Waldemar Kłaczyński
 */
public final class StateFlowHandlerImpl extends StateFlowHandler {

    private static final Logger log = Logger.getLogger(StateFlowHandler.class.getName());
    private final List<CustomAction> customActions = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, Class<? extends Invoker>> customInvokers = Collections.synchronizedMap(new HashMap<>());

    public static final String FIRST_FLOW_CONTEXT = StateFlowHandlerImpl.class.getName() + ":FACES_CHART_FLOW_CONTEXT";
    public static final String VIEW_UUID_KEY = "javax.faces.state.StateFlowHandler:VIEW_UUID_KEY";

    /**
     *
     */
    public static final String LOGICAL_FLOW_MAP = StateFlowHandlerImpl.class.getName() + ".LogicalFlowMap";

    private Boolean logstep;
    private Boolean alwaysSerialized;

    private TimerEventProducer eventProducer;
    private DefaultFaceletFactory faceletFactory;

    /**
     *
     * @param ctx
     */
    public StateFlowHandlerImpl(ServletContext ctx) {
        super();
        customInvokers.put("view", ViewInvoker.class);
        customInvokers.put("scxml", SubInvoker.class);
        customInvokers.put("http://www.w3.org/TR/scxml/", SubInvoker.class);
        customInvokers.put("facet", FacetInvoker.class);

        customActions.add(new CustomAction(SCXML_DATA_MODEL, "var", SetVariable.class));

        customActions.add(new CustomAction(FXSCXML_DATA_MODEL, "call", MethodCall.class));
        customActions.add(new CustomAction(FXSCXML_DATA_MODEL, "redirect", Redirect.class));

        Set<Class<?>> annotatedClasses = (Set<Class<?>>) ctx.getAttribute(ANNOTATED_CLASSES);
        for (Class<?> type : annotatedClasses) {
            if (type.isAnnotationPresent(StateChartAction.class)) {
                StateChartAction def = type.getAnnotation(StateChartAction.class);
                Class<?> javaClass = type;
                CustomAction action = new CustomAction(def.namespaceURI(), def.value(), (Class<? extends Action>) javaClass);
                customActions.add(action);
            } else if (type.isAnnotationPresent(StateChartActions.class)) {
                StateChartActions defs = type.getAnnotation(StateChartActions.class);
                for (StateChartAction def : defs.value()) {
                    Class<?> javaClass = type;
                    CustomAction action = new CustomAction(def.namespaceURI(), def.value(), (Class<? extends Action>) javaClass);
                    customActions.add(action);
                }
            } else if (type.isAnnotationPresent(StateChartInvoker.class)) {
                StateChartInvoker def = type.getAnnotation(StateChartInvoker.class);
                Class<?> javaClass = type;
                customInvokers.put(def.value(), (Class<Invoker>) javaClass);
            } else if (type.isAnnotationPresent(StateChartInvokers.class)) {
                StateChartInvokers defs = type.getAnnotation(StateChartInvokers.class);
                for (StateChartInvoker def : defs.value()) {
                    Class<?> javaClass = type;
                    customInvokers.put(def.value(), (Class<Invoker>) javaClass);
                }
            }
        }
    }

    @Override
    public TimerEventProducer getTimerEventProducer() {
        if (eventProducer == null) {
            eventProducer = new TimerEventProducerImpl();
            ServiceLoader<TimerEventProducer> loader = ServiceLoader.load(TimerEventProducer.class);
            Iterator<TimerEventProducer> iterator = loader.iterator();
            while (iterator.hasNext()) {
                TimerEventProducer next = iterator.next();
                next.setWrapped(eventProducer);
                eventProducer = next;
            }
        }
        return eventProducer;
    }

    private Boolean getAlwaysSerialized() {
        if (alwaysSerialized == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc.getApplication().getProjectStage() == ProjectStage.Production) {
                alwaysSerialized = Boolean.FALSE;
            } else {
                StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
                String pname = wcfg.getOptionValue(STATE_CHART_SERIALIZED_PARAM_NAME, "false");
                alwaysSerialized = Boolean.parseBoolean(pname);
            }
        }
        return alwaysSerialized;
    }

    private boolean isLogstep() {
        if (logstep == null) {
            FacesContext fc = FacesContext.getCurrentInstance();
            if (fc.getApplication().getProjectStage() == ProjectStage.Production) {
                logstep = Boolean.FALSE;
            } else {
                StateWebConfiguration wcfg = StateWebConfiguration.getInstance();
                String pname = wcfg.getOptionValue(STATE_CHART_LOGSTEP_PARAM_NAME, "false");
                logstep = Boolean.parseBoolean(pname);
            }
        }
        return logstep;
    }

    @Override
    public SCXML getStateMachine(FacesContext context, String viewId, String continerName, String id) throws ModelException {
        SCXML scxml = null;
        String currentViewId = null;

        UIViewRoot currentViewRoot = context.getViewRoot();
        if (currentViewRoot != null) {
            currentViewId = currentViewRoot.getViewId();
        }

        if (continerName == null) {
            continerName = STATE_CHART_FACET_NAME;
        }

        if ((viewId == null || Objects.equals(currentViewId, viewId))) {
            UIComponent facet = currentViewRoot.getFacet(continerName);
            if (facet != null) {
                UIStateChartMachine uichart = (UIStateChartMachine) facet.findComponent(id);
                if (uichart != null) {
                    scxml = uichart.getStateChart();
                }
            }
            return scxml;
        }

        if (currentViewRoot != null && !STATE_CHART_FACET_NAME.equals(continerName)) {
            if (currentViewRoot.getFacetCount() > 0) {
                UIComponent stateContiner = currentViewRoot.getFacets().get(continerName);
                if (stateContiner != null) {
                    UIStateChartMachine uichart = (UIStateChartMachine) stateContiner.findComponent(id);
                    if (uichart != null) {
                        scxml = uichart.getStateChart();
                        return scxml;
                    }
                }
            }
        }

        context.setProcessingEvents(false);
        try {
            context.getAttributes().put(SKIP_START_STATE_MACHINE_HINT, true);
            context.getAttributes().put(BUILD_STATE_MACHINE_HINT, id);

            ViewHandler vh = context.getApplication().getViewHandler();
            ViewDeclarationLanguage vdl = vh.getViewDeclarationLanguage(context, viewId);

            ViewMetadata viewMetadata = vdl.getViewMetadata(context, viewId);

            UIViewRoot view = viewMetadata.createMetadataView(context);

            UIComponent facet = view.getFacet(continerName);
            if (facet != null) {
                UIStateChartMachine uichart = (UIStateChartMachine) facet.findComponent(id);
                if (uichart != null) {
                    scxml = uichart.getStateChart();
                }

                if (currentViewRoot != null && !STATE_CHART_FACET_NAME.equals(continerName)) {
                    UIComponent current = currentViewRoot.getFacets().get(continerName);
                    if (current == null) {
                        facet.setParent(null);
                        currentViewRoot.getFacets().put(continerName, facet);
                    } else {
                        UIComponent found = facet.findComponent(id);
                        found.setParent(null);
                        current.getChildren().add(found);
                    }
                }
            }

        } finally {
            context.getAttributes().remove(BUILD_STATE_MACHINE_HINT);
            context.getAttributes().remove(SKIP_START_STATE_MACHINE_HINT);
            context.setProcessingEvents(true);
        }

        return scxml;
    }

    @Override
    public SCXML getStateMachine(FacesContext context, URL url, String continerName, String id) throws ModelException {
        SCXML scxml = null;
        UIComponent stateContiner = null;
        UIViewRoot currentViewRoot = context.getViewRoot();

        if (continerName == null) {
            continerName = STATE_CHART_FACET_NAME;
        }

        if (url == null) {
            UIComponent facet = currentViewRoot.getFacet(continerName);
            if (facet != null) {
                UIStateChartMachine uichart = (UIStateChartMachine) facet.findComponent(id);
                if (uichart != null) {
                    scxml = uichart.getStateChart();
                }
            }
            return scxml;
        }

        if (currentViewRoot.getFacetCount() > 0) {
            stateContiner = currentViewRoot.getFacets().get(continerName);
        }

        if (stateContiner == null) {
            Map<String, Object> currentViewMapShallowCopy = Collections.emptyMap();
            try {
                context.getAttributes().put(BUILD_STATE_CONTINER_HINT, continerName);
                context.getAttributes().put(BUILD_STATE_MACHINE_HINT, id);

                String viewId = toViewId(context, url.getPath());

                ViewHandler vh = context.getApplication().getViewHandler();
                UIViewRoot scxmlViewRoot = vh.createView(context, viewId);
                context.getAttributes().put(RIConstants.VIEWID_KEY_NAME, viewId);

                context.setProcessingEvents(false);
                if (faceletFactory == null) {
                    ApplicationAssociate associate = ApplicationAssociate
                            .getInstance(context.getExternalContext());
                    faceletFactory = associate.getFaceletFactory();
                    assert (faceletFactory != null);
                }

                if (null != currentViewRoot) {
                    Map<String, Object> currentViewMap = currentViewRoot.getViewMap(false);

                    if (null != currentViewMap && !currentViewMap.isEmpty()) {
                        currentViewMapShallowCopy = new HashMap<>(currentViewMap);
                        Map<String, Object> resultViewMap = scxmlViewRoot.getViewMap(true);
                        resultViewMap.putAll(currentViewMapShallowCopy);
                    }
                }

                Facelet f = faceletFactory.getMetadataFacelet(context, url);

                f.apply(context, scxmlViewRoot);

                UIComponent facet = scxmlViewRoot.getFacet(continerName);
                if (facet != null) {
                    UIComponent current = currentViewRoot.getFacets().get(continerName);
                    if (current == null) {
                        facet.setParent(null);
                        currentViewRoot.getFacets().put(continerName, facet);
                    } else {
                        UIComponent found = facet.findComponent(id);
                        found.setParent(null);
                        current.getChildren().add(found);
                    }
                }

            } catch (IOException ex) {
                throw new FacesException(ex);
            } finally {
                context.getAttributes().remove(RIConstants.VIEWID_KEY_NAME);
                context.getAttributes().remove(BUILD_STATE_CONTINER_HINT);
                context.setProcessingEvents(true);
                if (null != currentViewRoot) {
                    context.setViewRoot(currentViewRoot);
                    if (!currentViewMapShallowCopy.isEmpty()) {
                        currentViewRoot.getViewMap(true).putAll(currentViewMapShallowCopy);
                        currentViewMapShallowCopy.clear();
                    }
                }
            }

            if (currentViewRoot.getFacetCount() > 0) {
                stateContiner = currentViewRoot.getFacets().get(continerName);
            }

        }

        if (stateContiner != null) {
            if (stateContiner.getChildCount() != 0) {
                UIStateChartMachine uichart = (UIStateChartMachine) stateContiner.findComponent(id);
                if (uichart != null) {
                    scxml = uichart.getStateChart();
                }
            }
        }

        return scxml;
    }

    @Override
    public List<CustomAction> getCustomActions() {
        return Collections.unmodifiableList(customActions);
    }

    @Override
    public Map<String, Class<? extends Invoker>> getCustomInvokers() {
        return Collections.unmodifiableMap(customInvokers);
    }

    @Override
    public String getFlowId(FacesContext context) {
        String executorId = getViewExecutorId(context);
        if (executorId == null) {
            return null;
        }

        int pos = executorId.indexOf(':');
        if (pos >= 0) {
            executorId = executorId.substring(0, pos);
        }
        return executorId;
    }

    @Override
    public Context getFlowContext(FacesContext fc, String executorId) {
        Context context = null;
        if (executorId == null) {
            context = (Context) fc.getAttributes().get(FIRST_FLOW_CONTEXT);
            if (context != null) {
                return context;
            }
        }

        FlowDeque fs = getFlowDeque(fc, executorId, false);
        if (fs != null) {
            context = fs.getFlowContext();
        }

        if (context == null) {
            context = new SimpleContext();
            fc.getAttributes().put(FIRST_FLOW_CONTEXT, context);
            StateFlowCDIHelper.flowEntered();
        }

        return context;
    }

    @Override
    public String getViewExecutorId(FacesContext context) {
        String uuid = null;

        if (uuid == null) {
            UIViewRoot viewRoot = context.getViewRoot();
            if (viewRoot != null) {
                uuid = (String) viewRoot.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
            }
        }

        if (uuid == null) {
            uuid = (String) context.getAttributes().get(FACES_VIEW_ROOT_EXECUTOR_ID);
        }

        return uuid;
    }

    @Override
    public SCXMLExecutor getCurrentExecutor(FacesContext context) {
        SCXMLExecutor executor = null;
        ExecuteContextManager manager = ExecuteContextManager.getManager(context);
        ExecuteContext executeContext = manager.getCurrentExecuteContext(context);
        if (executeContext != null) {
            executor = executeContext.getExecutor();
        }

        return executor;
    }

    @Override
    public SCXMLExecutor getExecutor(FacesContext context, String executorId) {
        SCXMLExecutor executor;
        FlowDeque fs = getFlowDeque(context, executorId, false);
        if (fs == null) {
            return null;
        }
        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        if (executorId != null) {
            executor = executors.get(executorId);
        } else {
            executor = getCurrentExecutor(context);
        }

        return executor;
    }

    @Override
    public SCXMLExecutor getRootExecutor(FacesContext context, String executorId) {
        SCXMLExecutor executor;

        FlowDeque fs = getFlowDeque(context, executorId, false);
        if (fs == null) {
            return null;
        }
        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        if (executorId != null) {
            executor = executors.get(executorId);
        } else {
            executor = getCurrentExecutor(context);
        }

        if (executor != null) {
            while (!executor.isRoot() && executor.getParentSCXMLIOProcessor().getExecutor() != null) {
                executor = executor.getParentSCXMLIOProcessor().getExecutor();
            }
        }

        return executor;
    }

    @Override
    public SCXMLExecutor getViewExecutor(FacesContext context) {
        String executorId = getViewExecutorId(context);
        if (executorId == null) {
            return null;
        }

        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return null;
        }
        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        SCXMLExecutor executor = executors.get(executorId);
        return executor;
    }

    @Override
    public SCXMLExecutor getViewRootExecutor(FacesContext context) {
        SCXMLExecutor executor = getViewExecutor(context);
        if (executor != null) {
            while (!executor.isRoot() && executor.getParentSCXMLIOProcessor().getExecutor() != null) {
                executor = executor.getParentSCXMLIOProcessor().getExecutor();
            }
        }
        return executor;
    }

    @Override
    public boolean hasViewRoot(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return false;
        }
        String executorId = getViewExecutorId(context);
        return fs.getExecutors().containsKey(executorId);
    }

//    @Override
    public boolean isActive(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        return fs != null && !fs.isClosed();
    }

    @Override
    public SCXMLExecutor createRootExecutor(String id, FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException {

        FlowDeque fs = getFlowDeque(context, true);

        if (fs.isClosed()) {
            throw new FacesException("Can not execute new executor in finished flow istance.");
        }

        StateFlowEvaluator evaluator = new StateFlowEvaluator();

        TimerEventProducer timerEventProducer = getTimerEventProducer();

        StateFlowDispatcher dispatcher = new StateFlowDispatcher(timerEventProducer);
        StateFlowErrorReporter errorReporter = new StateFlowErrorReporter();
        Map tags = (Map) scxml.getMetadata().get("faces-tag-info");
        errorReporter.getTags().putAll(new HashMap<>(tags));

        Context flowContext = fs.getFlowContext();

        Context rootCtx;
        SCXMLExecutor executor;
        if (parent == null) {
            executor = new SCXMLExecutor(id, evaluator, dispatcher, errorReporter);
            rootCtx = executor.getEvaluator().newContext(flowContext);
        } else {
            executor = new SCXMLExecutor(id, parent, invokeId, evaluator, dispatcher, errorReporter);
            rootCtx = executor.getEvaluator().newContext(flowContext);
        }

        executor.setObservableId(fs.observableId++);
        executor.setStateMachine(scxml);
        executor.addListener(scxml, new StateFlowCDIListener(executor));

        executor.setRootContext(rootCtx);

        if (context.getApplication().getProjectStage() == ProjectStage.Production) {
            executor.setCheckLegalConfiguration(false);
        } else {
            executor.setCheckLegalConfiguration(true);
            if (isLogstep()) {
                executor.addListener(scxml, new SimpleSCXMLListener());
            }
        }

        executor.addListener(scxml, new AbstractSCXMLListener() {
            @Override
            public void onClose(SCXMLExecutor executor) {
                if (!executor.isRunning()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    close(fc, executor);
                }
            }
        });

        Context ctx = executor.getRootContext();
        ctx.setLocal("scxml_has_parent", false);

        return executor;
    }

    @Override
    public SCXMLExecutor createChildExecutor(String id, FacesContext context, SCXMLExecutor parent, String invokeId, SCXML scxml) throws ModelException {
        FlowDeque fs = getFlowDeque(context, true);

        StateFlowErrorReporter errorReporter = (StateFlowErrorReporter) parent.getErrorReporter();

        Map tags = (Map) scxml.getMetadata().get("faces-tag-info");
        errorReporter.getTags().putAll(new HashMap<>(tags));

        SCXMLExecutor executor = new SCXMLExecutor(id, parent, invokeId, scxml);
        executor.setObservableId(fs.observableId++);

        executor.setRootContext(executor.getEvaluator().newContext(parent.getRootContext()));

        executor.addListener(scxml, new StateFlowCDIListener(executor));

        if (context.getApplication().getProjectStage() == ProjectStage.Production) {
            executor.setCheckLegalConfiguration(false);
        } else {
            executor.setCheckLegalConfiguration(true);
            if (isLogstep()) {
                executor.addListener(scxml, new SimpleSCXMLListener());
            }
        }

        executor.addListener(scxml, new AbstractSCXMLListener() {
            @Override
            public void onClose(SCXMLExecutor executor) {
                if (!executor.isRunning()) {
                    FacesContext fc = FacesContext.getCurrentInstance();
                    close(fc, executor);
                }
            }
        });

        Context ctx = executor.getRootContext();
        ctx.setLocal("scxml_has_parent", parent != null);

        return executor;
    }

    @Override
    public void execute(FacesContext context, SCXMLExecutor executor, Map<String, Object> params) {
        try {
            boolean root = executor.isRoot();

            FlowDeque fs = getFlowDeque(context, true);

            if (fs.isClosed()) {
                throw new FacesException("Can not execute new executor in finished flow istance.");
            }

            Map<String, List<String>> map = fs.getMap();
            Map<String, String> roots = fs.getRoots();
            Map<String, SCXMLExecutor> executors = fs.getExecutors();

            String executorId = executor.getId();

            executors.put(executorId, executor);
            if (root) {
                SCXMLExecutor viewroot = getViewExecutor(context);
                if (viewroot != null) {
                    String parentId = viewroot.getId();
                    map.computeIfAbsent(parentId, (key) -> new ArrayList<>())
                            .add(executorId);
                    roots.put(executorId, parentId);
                }
            } else {
                ParentSCXMLIOProcessor ioProcessor = executor.getParentSCXMLIOProcessor();
                String parentId = ioProcessor.getId();
                map.computeIfAbsent(parentId, (key) -> new ArrayList<>())
                        .add(executorId);
            }

            SCXMLExecutor currentExecutor = getCurrentExecutor(context);
            if (currentExecutor != null) {
                String parentSessionId = (String) currentExecutor.getSCInstance().getSystemContext().get(SCXMLSystemContext.SESSIONID_KEY);
                Context gctx = executor.getGlobalContext();
                gctx.setLocal("##parent_executor_session_id", parentSessionId);
            }

            SCXMLExecutor rootExecutor = getRootExecutor(context);
            if (rootExecutor != null) {
                String parentSessionId = (String) rootExecutor.getSCInstance().getSystemContext().get(SCXMLSystemContext.SESSIONID_KEY);
                Context gctx = executor.getGlobalContext();
                gctx.setLocal("##root_executor_session_id", parentSessionId);
            }

            executorEntered(executor);
            if (CdiUtil.isCdiAvailable(context)) {
                BeanManager bm = CdiUtil.getCdiBeanManager(context);
                bm.fireEvent(new OnExecuteEvent(executor));
            }

            try {
                executor.go(params);
                executor.triggerEvents();
            } catch (ModelException me) {
                close(context, executor);
                throw new FacesException(me);
            }

            if (!executor.isRunning()) {
                close(context, executor);
            }
        } catch (FaceletException ex) {
            throw ex;
        } catch (FacesException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new FacesException(ex);
        }
    }

    @Override
    public void broadcastEvent(FacesContext fc, TriggerEvent evt) {
        SCXMLExecutor executor = getViewExecutor(fc);
        if (executor != null) {
            try {
                executor.triggerEvent(evt);
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }

        UIViewRoot viewRoot = fc.getViewRoot();
        List<String> clientIds = getControllerClientIds(fc);
        if (clientIds != null && !clientIds.isEmpty()) {
            Set<VisitHint> hints = EnumSet.of(VisitHint.SKIP_ITERATION);
            VisitContext visitContext = VisitContext.createVisitContext(fc, clientIds, hints);
            viewRoot.visitTree(visitContext, (VisitContext context, UIComponent target) -> {
                if (target instanceof UIStateChartExecutor) {
                    UIStateChartExecutor controller = (UIStateChartExecutor) target;
                    String controllerId = controller.getClientId(fc);

                    SCXMLExecutor cexecutor = null;
                    String executorId = controller.getExecutorId();
                    if (executorId != null) {
                        cexecutor = getRootExecutor(fc, executorId);
                    }

                    if (cexecutor != null) {
                        try {
                            cexecutor.triggerEvent(evt);
                        } catch (ModelException ex) {
                            throw new FacesException(ex);
                        }
                    }
                }
                return VisitResult.ACCEPT;
            });
        }

    }

    @Override
    public List<String> getControllerClientIds(FacesContext context) {
        if (context.getViewRoot() == null) {
            return null;
        }
        return (ArrayList<String>) context.getViewRoot().getAttributes().get(CONTROLLER_SET_HINT);
    }

    @Override
    public void closeAll(FacesContext context) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null) {
            return;
        }

        String executorId = getViewExecutorId(context);

        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        SCXMLExecutor executor = executors.get(executorId);

        close(context, executor);

    }

    @Override
    public void close(FacesContext context, SCXMLExecutor executor) {
        FlowDeque fs = getFlowDeque(context, false);
        if (fs == null || fs.isClosed()) {
            return;
        }

        if (executor == null) {
            return;
        }

        Map<String, String> roots = fs.getRoots();
        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        String parentId = null;
        String executorId = executor.getId();

        if (executors.containsKey(executorId)) {
            parentId = roots.get(executorId);
        }

        close(context, fs, executor);

        if (executors.isEmpty()) {
            if (executors.isEmpty()) {
                closeFlowDeque(context);
            }
            if (CdiUtil.isCdiAvailable(context)) {
                BeanManager bm = CdiUtil.getCdiBeanManager(context);
                bm.fireEvent(new OnFinishEvent(executor));
            }
        } else if (parentId != null && executors.containsKey(parentId)) {
            try {
                String outcome = "close";

                String viewId = (String) executor.getRootContext().get(FACES_CHART_EXECUTOR_VIEW_ID);

                SCXMLExecutor parent = executors.get(parentId);

                parent.addEvent(new EventBuilder(
                        PORTLET_EVENT_PREFIX + outcome,
                        TriggerEvent.CALL_EVENT)
                        .sendId(viewId).build());

                parent.triggerEvents();
            } catch (ModelException ex) {
                throw new FacesException(ex);
            }
        }
    }

    private void close(FacesContext context, FlowDeque fs, SCXMLExecutor executor) {
        //Stack<String> stack = fs.getRoots();
        Map<String, SCXMLExecutor> executors = fs.getExecutors();

        if (executor == null) {
            return;
        }

        String executorId = executor.getId();

        if (executors.containsKey(executorId)) {
            Map<String, List<String>> map = fs.getMap();
            if (map.containsKey(executorId)) {
                List<String> children = map.get(executorId);
                for (String childId : children) {
                    if (!executorId.equals(childId) && executors.containsKey(childId)) {
                        SCXMLExecutor child = executors.get(childId);
                        close(context, fs, child);
                    }
                }
            }
        }

        executorExited(executor);
        executors.remove(executorId);

    }

    @Override
    public void executorEntered(SCXMLExecutor executor) {
        StateFlowCDIHelper.executorEntered(executor);
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        application.publishEvent(context, PostExecuteEvent.class, executor);
    }

    @Override
    public void executorExited(SCXMLExecutor executor) {
        StateFlowCDIHelper.executorExited(executor);
        FacesContext context = FacesContext.getCurrentInstance();
        Application application = context.getApplication();
        application.publishEvent(context, PreExecutorExitedEvent.class, executor);
    }

    private FlowDeque getFlowDeque(FacesContext context, boolean create) {
        String executorId = getViewExecutorId(context);
        return getFlowDeque(context, executorId, create);
    }

    private FlowDeque getFlowDeque(FacesContext fc, String executorId, boolean create) {

        FlowDeque result = (FlowDeque) fc.getAttributes()
                .get(STATE_FLOW_STACK);

        if (result != null && !(result.isClosed() && create)) {
            return result;
        }

        ExternalContext ec = fc.getExternalContext();
        Object session = ec.getSession(create);
        if (session == null) {
            return null;
        }

        Map<String, Object> sessionMap = ec.getSessionMap();
        Map<String, Object> flowMap = (Map<String, Object>) sessionMap.get(LOGICAL_FLOW_MAP);
        if (flowMap == null && create) {
            flowMap = new HashMap<>();
            sessionMap.put(LOGICAL_FLOW_MAP, flowMap);
        }

        if (flowMap == null) {
            return null;
        }

        String flowKey = null;

        if (StateFlowParams.isUseWindowMode()) {
            ClientWindow clientWindow = ec.getClientWindow();
            if (clientWindow == null) {
                if (create) {
                    throw new IllegalStateException("Client Window mode not found");
                }
                return null;
            }

            flowKey = clientWindow.getId() + "_stateFlowStack";
        }

        if (flowKey == null) {
            if (executorId == null) {
                if (create) {
                    throw new IllegalStateException("Can not create flow queue can not set executor id.");
                }
                return null;
            }

            int pos = executorId.indexOf(':');
            if (pos >= 0) {
                executorId = executorId.substring(0, pos);
            }

            flowKey = executorId + "_stateFlowStack";
        }

        if (!getAlwaysSerialized()) {
            result = (FlowDeque) flowMap.get(flowKey);
            if (null == result && create) {
                result = new FlowDeque(flowKey);
            }
        } else {
            Object state = flowMap.get(flowKey);
            if (null == state && create) {
                result = new FlowDeque(flowKey);
            } else {
                result = restoreFlowDequeState(fc, state, flowKey);
            }
        }
        if (result != null && result.isClosed() && create) {
            result = new FlowDeque(flowKey);
            flowMap.put(flowKey, result);
        }

        if (result != null) {
            if (fc.getAttributes().containsKey(FIRST_FLOW_CONTEXT)) {
                Context first = (Context) fc.getAttributes().get(FIRST_FLOW_CONTEXT);
                result.getFlowContext().getVars().putAll(first.getVars());
                fc.getAttributes().remove(FIRST_FLOW_CONTEXT);
            }
        }

        fc.getAttributes().put(STATE_FLOW_STACK, result);

        return result;
    }

    private void closeFlowDeque(FacesContext context) {
        ExternalContext ec = context.getExternalContext();
        String executorId = getViewExecutorId(context);

        FlowDeque flowDeque = getFlowDeque(context, executorId, false);
        if (flowDeque != null) {
            StateFlowCDIHelper.flowExited();
            flowDeque.close();
        }

        String flowKey = null;
        if (StateFlowParams.isUseWindowMode()) {
            ClientWindow clientWindow = ec.getClientWindow();
            clientWindow.disableClientWindowRenderMode(context);
        }

    }

    @Override
    public void writeState(FacesContext context) {

        FlowDeque flowStack = getFlowDeque(context, false);
        if (flowStack == null) {
            return;
        }

        ExternalContext ec = context.getExternalContext();
        Map<String, Object> sessionMap = ec.getSessionMap();
        Map<String, Object> flowMap = (Map<String, Object>) sessionMap.get(LOGICAL_FLOW_MAP);
        if (flowMap == null) {
            flowMap = new HashMap<>();
            sessionMap.put(LOGICAL_FLOW_MAP, flowMap);
        }

        String flowKey = null;

        if (StateFlowParams.isUseWindowMode()) {
            ClientWindow clientWindow = ec.getClientWindow();
            if (clientWindow == null) {
                throw new IllegalStateException("Client Window mode not found");
            }
            flowKey = clientWindow.getId() + "_stateFlowStack";
        }

        if (flowKey == null) {
            String executorId = getViewExecutorId(context);
            if (executorId == null) {
                throw new IllegalStateException("Can not create flow queue can not set executor id.");
            }

            int pos = executorId.indexOf(':');
            if (pos >= 0) {
                executorId = executorId.substring(0, pos);
            }

            flowKey = executorId + "_stateFlowStack";
        }

        if (StateFlowParams.isUseWindowMode()) {
            ClientWindow clientWindow = ec.getClientWindow();
            if (clientWindow == null) {
                throw new IllegalStateException("Client Window mode not found");
            }
        }

        if (!getAlwaysSerialized()) {
            flowMap.put(flowKey, flowStack);
        } else {
            Object state = saveFlowDequeState(context, flowStack);
            flowMap.put(flowKey, state);
        }
    }

    private Object saveFlowDequeState(FacesContext fc, FlowDeque flowDeque) {
        if (fc == null) {
            throw new NullPointerException();
        }

        Object states = flowDeque.saveState(fc);
        return states;
    }

    private static FlowDeque restoreFlowDequeState(FacesContext fc, Object state, String sessionKey) {
        FlowDeque result = new FlowDeque(sessionKey);
        result.restoreState(fc, state);
        return result;
    }

    private static class FlowDeque implements Serializable {

        private final Map<String, SCXMLExecutor> executors;
        private final Map<String, String> roots;
        private final Map<String, List<String>> map;
        private final String key;
        private final SimpleContext flowContext;
        private Integer observableId = 0;
        private boolean closed;

        public FlowDeque(final String sessionKey) {
            executors = new LinkedHashMap<>();
            roots = new HashMap<>();
            map = new HashMap<>();
            flowContext = new SimpleContext();
            this.key = sessionKey;
        }

        public String getKey() {
            return key;
        }

        public Map<String, SCXMLExecutor> getExecutors() {
            return executors;
        }

        public Map<String, String> getRoots() {
            return roots;
        }

        public Map<String, List<String>> getMap() {
            return map;
        }

        public boolean isClosed() {
            return closed;
        }

        public void close() {
            closed = true;
        }

        public Context getFlowContext() {
            return flowContext;
        }

        public Integer getObservableId() {
            return observableId;
        }

        public void setObservableId(Integer observableId) {
            this.observableId = observableId;
        }

        // ----------------------------------------------- Serialization Methods
        // This is dependent on serialization occuring with in a
        // a Faces request, however, since SCXMLExecutor.{save,restore}State()
        // doesn't actually serialize the FlowDeque, these methods are here
        // purely to be good citizens.
        private void writeObject(ObjectOutputStream out) throws IOException {
            Object states = saveState(FacesContext.getCurrentInstance());
            //noinspection NonSerializableObjectPassedToObjectStream
            out.writeObject(states);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object state = in.readObject();
            restoreState(FacesContext.getCurrentInstance(), state);
        }

        public Object saveState(FacesContext fc) {

            Object states[] = new Object[6];

            states[0] = closed;
            states[1] = observableId;

            if (null != roots && !roots.isEmpty()) {
                Object[] attached = new Object[roots.size()];
                int i = 0;
                for (Map.Entry<String, String> entry : roots.entrySet()) {
                    Object values[] = new Object[2];
                    values[0] = entry.getKey();
                    values[1] = entry.getValue();
                    attached[i++] = values;

                }
                states[2] = attached;
            }

            if (null != executors && !executors.isEmpty()) {
                Object[] attached = new Object[executors.size()];
                int i = 0;
                for (String executorId : executors.keySet()) {
                    SCXMLExecutor executor = executors.get(executorId);
                    boolean root = executor.isRoot();
                    Object values[] = new Object[7];
                    SCXML stateMachine = executor.getStateMachine();
                    Context context = new SimpleContext();
                    String parentId = null;
                    String invokeId = null;

                    if (executor.getParentSCXMLIOProcessor() != null) {
                        parentId = executor.getParentSCXMLIOProcessor().getId();
                    }

                    values[0] = root;
                    values[1] = executor.getId();
                    values[2] = stateMachine.getMetadata().get("faces-viewid");
                    values[3] = stateMachine.getMetadata().get("faces-chartid");
                    values[4] = parentId;
                    values[5] = invokeId;
                    if (root) {
                        values[6] = executor.saveState(context);
                    }

                    attached[i++] = values;
                }
                states[3] = attached;
            }

            states[4] = saveMapState(map);

            Context context = new SimpleContext();
            states[5] = saveContext(context, flowContext);

            return states;
        }

        public void restoreState(FacesContext fc, Object state) {
            StateFlowHandler handler = StateFlowHandler.getInstance();
            executors.clear();
            roots.clear();
            map.clear();
            flowContext.getVars().clear();

            if (null != state) {
                Object[] blocks = (Object[]) state;

                closed = (boolean) blocks[0];
                observableId = (Integer) blocks[1];

                if (blocks[1] != null) {
                    Object[] values = (Object[]) blocks[1];
                    for (Object value : values) {
                        Object[] entry = (Object[]) value;
                        roots.put(String.valueOf(entry[0]), String.valueOf(entry[1]));
                    }
                }

                if (blocks[3] != null) {
                    Object[] entries = (Object[]) blocks[3];
                    for (Object entry : entries) {
                        Object[] values = (Object[]) entry;

                        Boolean root = (Boolean) values[1];
                        String executorId = (String) values[1];
                        String viewId = (String) values[2];
                        String chartId = (String) values[3];

                        SCXMLExecutor executor;

                        if (root) {
                            Context context = new SimpleContext();
                            SCXML stateMachine = null;
                            try {
                                stateMachine = handler.getStateMachine(fc, viewId, chartId);
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }

                            if (stateMachine == null) {
                                throw new FacesException(String.format("Restored state flow %s in %s not found.", viewId, chartId));
                            }

                            SCXMLExecutor parent = null;
                            String parentId = (String) values[4];
                            String invokeId = (String) values[5];
                            if (parentId != null) {
                                parent = executors.get(parentId);
                            }

                            try {
                                executor = handler.createRootExecutor(executorId, fc, parent, invokeId, stateMachine);
                            } catch (ModelException ex) {
                                throw new FacesException(ex);
                            }
                            executor.restoreState(context, values[5]);
                        } else {
                            String parentId = (String) values[4];
                            executor = executors.get(parentId);
                        }

                        executors.put(executorId, executor);
                    }
                }

                if (blocks[4] != null) {
                    restoreMapState(map, blocks[4]);
                }

                if (blocks[5] != null) {
                    Context context = new SimpleContext();
                    restoreContext(context, flowContext, blocks[5]);
                }

            }
        }

        private Object saveMapState(Map<String, List<String>> map) {
            Object state = null;
            if (null != map && map.size() > 0) {
                Object[] attached = new Object[map.size()];
                int i = 0;
                for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                    List<String> list = entry.getValue();
                    Object values[] = new Object[2];
                    values[0] = entry.getKey();
                    values[1] = saveIdsState(list);
                    attached[i++] = values;
                }
                state = attached;
            }
            return state;
        }

        private void restoreMapState(Map<String, List<String>> map, Object state) {
            map.clear();

            if (null != state) {
                Object[] values = (Object[]) state;
                for (Object value : values) {
                    Object[] entry = (Object[]) value;

                    List<String> ids = new ArrayList<>();
                    restoreIdsState(ids, entry[1]);

                    map.put(String.valueOf(entry[0]), ids);
                }
            }
        }

        private Object saveIdsState(List<String> list) {
            Object state = null;
            if (null != list && list.size() > 0) {
                Object[] attached = new Object[list.size()];
                int i = 0;
                for (Object value : list) {
                    attached[i++] = value;
                }
                state = attached;
            }
            return state;
        }

        private void restoreIdsState(List<String> list, Object state) {
            list.clear();

            if (null != state) {
                Object[] values = (Object[]) state;
                for (Object value : values) {
                    list.add(String.valueOf(value));
                }
            }
        }

    }

    @Override
    public ELContext getELContext(FacesContext context) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private static class WrappedFacesContext extends FacesContextWrapper {

        UIViewRoot vrot;
        private final FacesContext context;

        public WrappedFacesContext(FacesContext context) {
            this.context = context;
            vrot = context.getViewRoot();
        }

        @Override
        public FacesContext getWrapped() {
            return context;
        }

        @Override
        public void setViewRoot(UIViewRoot root) {
            vrot = root;
        }

        @Override
        public UIViewRoot getViewRoot() {
            return vrot;
        }

    };

}
