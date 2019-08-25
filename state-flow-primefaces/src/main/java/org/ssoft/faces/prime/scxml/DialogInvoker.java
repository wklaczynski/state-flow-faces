/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.prime.scxml;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.StateManager;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.PartialViewContext;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import static org.ssoft.faces.prime.PrimeFacesFlowUtils.applyParams;
import static javax.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static javax.faces.state.StateFlow.AFTER_RENDER_VIEW;
import static javax.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import static javax.faces.state.StateFlow.VIEW_EVENT_PREFIX;
import javax.faces.state.StateFlowHandler;
import javax.faces.state.execute.ExecuteContext;
import javax.faces.state.annotation.StateChartInvoker;
import javax.faces.state.scxml.Context;
import javax.faces.state.scxml.EventBuilder;
import javax.faces.state.scxml.InvokeContext;
import javax.faces.state.scxml.SCXMLExecutor;
import javax.faces.state.scxml.SCXMLIOProcessor;
import javax.faces.state.scxml.TriggerEvent;
import javax.faces.state.scxml.invoke.Invoker;
import javax.faces.state.scxml.invoke.InvokerException;
import javax.faces.state.scxml.model.ModelException;
import org.primefaces.PrimeFaces;
import org.primefaces.component.api.ClientBehaviorRenderingMode;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.Constants;
import static javax.faces.state.StateFlow.EXECUTOR_CONTEXT_VIEW_PATH;
import static javax.faces.state.StateFlow.FACES_CHART_VIEW_STATE;
import static javax.faces.state.StateFlow.VIEW_RESTORED_HINT;
import javax.faces.state.execute.ExecuteContextManager;
import org.primefaces.context.PrimeRequestContext;
import org.primefaces.util.SharedStringBuilder;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateChartInvoker("dialog")
@ResourceDependencies({
    @ResourceDependency(library = "primefaces", name = "components.css")
    ,@ResourceDependency(library = "primefaces", name = "jquery/jquery.js")
    ,@ResourceDependency(library = "primefaces", name = "jquery/jquery-plugins.js")
    ,@ResourceDependency(library = "primefaces", name = "core.js")
    ,@ResourceDependency(library = "primefaces", name = "components.js")
//    ,@ResourceDependency(library = "primeflow", name = "primescxml.js")
})
public class DialogInvoker implements Invoker, Serializable {

    public static final String DIALOG_CLOSE = DialogInvoker.class.getName() + "#close_dialog";
    public static final String DIALOG_OPEN = DialogInvoker.class.getName() + "#open_dialog";

    private static final String SB_ESCAPE = ComponentUtils.class.getName() + "#escape";

    private final static Logger logger = Logger.getLogger(DialogInvoker.class.getName());

    /**
     *
     */
    public static final String VIEW_PARAM = "@@@SubDialogInvoker@@@";

    private static final long serialVersionUID = 1L;

    private transient SCXMLExecutor executor;
    private transient String invokeId;
    private transient boolean cancelled;
    private String viewId;
    private String pfdlgcid;
    private Map<String, Object> vieparams;
    private boolean resolved;
    private String prevExecutorId;

    private String lastStateKey;
    private String lastViewId;
    private Object lastViewState;

    private String prevStateKey;
    private String prevViewId;
    private String prevcId;
    private Object prevViewState;
    private String path;
    private String sourceId;

    @Override
    public String getInvokeId() {
        return invokeId;
    }

    @Override
    public void setInvokeId(final String invokeId) {
        this.invokeId = invokeId;
        this.cancelled = false;
    }

    @Override
    public void setParentSCXMLExecutor(SCXMLExecutor parentSCXMLExecutor) {
        this.executor = parentSCXMLExecutor;
    }

    @Override
    public SCXMLIOProcessor getChildIOProcessor() {
        return null;
    }

    @Override
    public void invoke(final InvokeContext ictx, String source, final Map params) throws InvokerException {
        StateFlowHandler handler = StateFlowHandler.getInstance();
        try {
            FacesContext fc = FacesContext.getCurrentInstance();
            Map<String, String> requestParams = fc.getExternalContext().getRequestParameterMap();
            Map<Object, Object> attrs = fc.getAttributes();

            if (source.equals("@this")) {
                String machineViewId = (String) executor
                        .getStateMachine().getMetadata().get("faces-viewid");

                source = machineViewId;
            }
            prevExecutorId = handler.getExecutorViewRootId(fc);

            viewId = source;
            String oldInvokeViewId = (String) executor.getRootContext().get(EXECUTOR_CONTEXT_VIEW_PATH);
            if (oldInvokeViewId != null) {
                throw new InvokerException(String.format(
                        "Can not start invoke new view: \"%s\", in other view: \"%s\".",
                        viewId, oldInvokeViewId));
            }

            Map<String, Object> options = new HashMap<>();
            options.put("resizable", false);
            options.put("responsive", false);

            Map<String, Object> ajax = new HashMap<>();
            Map<String, List<String>> query = new HashMap<>();
            query.put("exid", Arrays.asList(executor.getRootId()));

            vieparams = new HashMap();
            for (Object key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (value instanceof String) {
                    if (containsOnlyDigits((String) value)) {
                        value = NumberFormat.getInstance().parse((String) value);
                    } else if ("true".equals(value)) {
                        value = true;
                    } else if ("false".equals(value)) {
                        value = false;
                    }
                }

                if (skey.startsWith("@query.param.")) {
                    skey = skey.substring(13);
                    query.put(skey, Collections.singletonList(value.toString()));
                } else if (skey.startsWith("@dialog.param.")) {
                    skey = skey.substring(14);
                    options.put(skey, value);
                } else if (skey.startsWith("@ajax.")) {
                    skey = skey.substring(6);
                    ajax.put(skey, value.toString());
                } else {
                    vieparams.put(skey, value);
                }
            }

            boolean transientState = true;
            if (options.containsKey("transient")) {
                Object val = options.get("transient");
                if (val instanceof String) {
                    transientState = Boolean.valueOf((String) val);
                } else if (val instanceof Boolean) {
                    transientState = (Boolean) val;
                }
            }

            Context rctx = executor.getRootContext();

            if (!transientState) {
                lastStateKey = "__@@Invoke:last:" + invokeId + ":";
            }

            if (lastStateKey != null) {
                lastStateKey = "__@@Invoke:last:" + invokeId + ":";
                if (rctx.hasLocal(lastStateKey + "ViewState")) {
                    lastViewState = rctx.get(lastStateKey + "ViewState");
                    lastViewId = (String) rctx.get(lastStateKey + "ViewId");
                    if (lastViewId != null) {
                        viewId = lastViewId;
                    }
                }
            } else {
                lastViewId = null;
                lastViewState = null;
            }

            UIViewRoot currentViewRoot = fc.getViewRoot();

            prevStateKey = "__@@Invoke:prev:" + invokeId + ":";
            prevViewId = currentViewRoot.getViewId();

            StateManager sm = fc.getApplication().getStateManager();
            prevViewState = sm.saveView(fc);

            UIComponent cc = UIComponent.getCurrentComponent(fc);
            if (cc != null) {
                prevcId = cc.getClientId();
            }

            rctx.setLocal(prevStateKey + "ViewState", prevViewState);
            rctx.setLocal(prevStateKey + "ViewId", prevViewId);

            String url = fc.getApplication().getViewHandler().getBookmarkableURL(fc, viewId, query, false);
            url = escapeEcmaScriptText(url);

            pfdlgcid = UUID.randomUUID().toString();
            String sourceComponentId = (String) attrs.get(CURRENT_COMPONENT_HINT);

            pfdlgcid = requestParams.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
            if (pfdlgcid == null) {
                pfdlgcid = UUID.randomUUID().toString();
            }

            path = executor.getRootId() + ":" + viewId;

            String widgetVar = "widget_" + invokeId;

            options.put("modal", true);
            options.put("closable", false);
            options.put("invokeId", invokeId);

            String update = (String) ajax.get("update");
            String process = (String) ajax.get("update");
            String global = (String) ajax.get("global");

            PrimeRequestContext requestContext = PrimeRequestContext.getCurrentInstance();
            AjaxRequestBuilder builder = requestContext.getAjaxRequestBuilder();
            ClientBehaviorRenderingMode renderingMode = ClientBehaviorRenderingMode.OBSTRUSIVE;

            String formId = null;
            UIComponent form;
            sourceId = currentViewRoot.getId();
            UIComponent component;

            if (sourceComponentId != null) {
                component = fc.getViewRoot().findComponent(sourceComponentId);
                if (component != null) {
                    form = ComponentTraversalUtils.closestForm(fc, component);
                    if (form != null) {
                        formId = form.getClientId(fc);
                    }
                    sourceId = component.getClientId();
                }
            } else {
                sourceId = currentViewRoot.getId();
                component = currentViewRoot;
                update = "@all";
            }

            boolean firstDialog = true;
            if (fc.getAttributes().containsKey(DIALOG_CLOSE)) {
                firstDialog = !(boolean) fc.getAttributes().get(DIALOG_CLOSE);
            }

            StringBuilder sb = new StringBuilder();

            sb.append("{");

//            sb.append("PrimeFaces.cw(\"ScxmlDialogInvoker\",\"")
//                    .append(widgetVar)
//                    .append("\",{id:\"").append(invokeId).append("\"");

//            sb.append(",behaviors:{");
//            sb.append("dialogReturn:").append("function(ext) {")
//                    .append(ajaxscript)
//                    .append("}");
//            sb.append("});");

            sb.append("PrimeFaces.scxml.openScxmlDialog({")
                    .append("url:'").append(url).append("'")
                    .append(",pfdlgcid:'").append(pfdlgcid).append("'")
                    .append(",sourceComponentId:'").append(sourceId).append("'")
//                    .append(",sourceWidgetVar:'").append(widgetVar).append("'")
                    .append(",invokeId:'").append(invokeId).append("'")
                    .append(",executorId:'").append(executor.getRootId()).append("'");

            sb.append(",options:{");
            if (options != null && options.size() > 0) {
                for (Iterator<String> it = options.keySet().iterator(); it.hasNext();) {
                    String optionName = it.next();
                    Object optionValue = options.get(optionName);

                    sb.append(optionName).append(":");
                    if (optionValue instanceof String) {
                        sb.append("'").append(escapeEcmaScriptText((String) optionValue)).append("'");
                    } else {
                        sb.append(optionValue);
                    }
                    if (it.hasNext()) {
                        sb.append(",");
                    }
                }
            }
            sb.append("}");

            if (firstDialog) {
                String reloadroot = builder.init()
                        .source(sourceId)
                        .form(formId)
                        .event("scxmlhide")
                        .update(component, update != null ? update : "@all")
                        .process(component, process != null ? process : "@none")
                        .async(false)
                        .global(global != null ? Boolean.parseBoolean(global) : true)
                        .delay(null)
                        .timeout(0)
                        .partialSubmit(false, false, null)
                        .resetValues(false, false)
                        .ignoreAutoUpdate(true)
                        .onstart(null)
                        .onerror(null)
                        .onsuccess(null)
                        .oncomplete(null)
                        .buildBehavior(renderingMode);
                
                sb.append(",behaviors:{");
                sb.append("parentRefresh:").append("function(ext) {")
                        .append(reloadroot)
                        .append("}");
                sb.append("}");
            }

            sb.append("});");

            sb.append("};");
            PrimeFaces.current().executeScript(sb.toString());
            sb.setLength(0);

            Context fctx = handler.getFlowContext(fc, executor.getRootId());
            if (lastViewState != null) {
                fctx.setLocal(FACES_CHART_VIEW_STATE, lastViewState);
            }

            resolved = false;
            executor.getRootContext().setLocal(EXECUTOR_CONTEXT_VIEW_PATH, viewId);
            fc.getAttributes().put(DIALOG_OPEN, true);

        } catch (InvokerException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new InvokerException(ex);
        }
    }

    @Override
    public void invokeContent(final InvokeContext ictx, final String content, final Map<String, Object> params)
            throws InvokerException {

    }

    @Override
    public void parentEvent(final InvokeContext ictx, final TriggerEvent event)
            throws InvokerException {
        if (cancelled) {
            return;
        }
        StateFlowHandler handler = StateFlowHandler.getInstance();

        FacesContext context = FacesContext.getCurrentInstance();
        //filter all multicast call event from started viewId by this invoker
        if (event.getType() == TriggerEvent.CALL_EVENT && viewId.equals(event.getSendId())) {

            if (event.getName().startsWith(AFTER_PHASE_EVENT_PREFIX)) {
                UIViewRoot viewRoot = context.getViewRoot();
                if (viewRoot != null) {
                    try {
                        ExecuteContext executeContext = new ExecuteContext(
                                path, invokeId, executor, ictx.getContext());

                        ExecuteContextManager manager = ExecuteContextManager.getManager(context);
                        manager.initExecuteContext(context, path, executeContext);
                    } catch (ModelException ex) {
                        throw new InvokerException(ex);
                    }
                }

                if (!resolved && !context.getResponseComplete()) {
                    applyParams(context, viewRoot, vieparams);
                    resolved = true;
                }
            }

            if (event.getName().startsWith(AFTER_RENDER_VIEW)) {

                UIViewRoot viewRoot = context.getViewRoot();
                if (viewRoot != null) {
                    lastViewId = viewRoot.getViewId();
                    RenderKit renderKit = context.getRenderKit();
                    ResponseStateManager rsm = renderKit.getResponseStateManager();
                    lastViewState = rsm.getState(context, lastViewId);
                }
            }

            if (event.getName().startsWith(VIEW_EVENT_PREFIX)) {
                ExternalContext ec = context.getExternalContext();

                Map<String, String> params = new HashMap<>();
                params.putAll(ec.getRequestParameterMap());

                String outcome = event.getName().substring(VIEW_EVENT_PREFIX.length());
                EventBuilder evb = new EventBuilder("view." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);

                evb.data(params);
                evb.sendId(invokeId);
                executor.addEvent(evb.build());
            }
        }

    }

    private boolean containsOnlyAlphaNumeric(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isLetterOrDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean containsOnlyDigits(String s) {
        for (int i = 0, n = s.length(); i < n; i++) {
            if (!Character.isDigit(s.codePointAt(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void cancel() throws InvokerException {
        cancelled = true;

        Context rctx = executor.getRootContext();

        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            if (lastStateKey != null) {
                lastViewId = viewRoot.getViewId();
                StateManager sm = fc.getApplication().getStateManager();
                lastViewState = sm.saveView(fc);

                rctx.setLocal(lastStateKey + "ViewState", lastViewState);
                rctx.setLocal(lastStateKey + "ViewId", lastViewId);
            }
        }

        StringBuilder sb = new StringBuilder();

        sb.append("parent.PrimeFaces.scxml.closeScxmlDialog({")
                .append("pfdlgcid:'").append(pfdlgcid).append("'")
                .append(",invokeId:'").append(invokeId).append("'")
                .append("});");

        PrimeFaces.current().executeScript(sb.toString());
        sb.setLength(0);

        PartialViewContext pvc = fc.getPartialViewContext();
        if ((pvc != null && (pvc.isAjaxRequest() || pvc.isPartialRequest()))) {
            pvc.setRenderAll(false);
        }
        fc.getAttributes().put(DIALOG_CLOSE, true);
        rctx.removeLocal(EXECUTOR_CONTEXT_VIEW_PATH);
        fc.getAttributes().put(VIEW_RESTORED_HINT, false);

        fc.renderResponse();
    }

    public static String escapeEcmaScriptText(String text) {
        if (text == null) {
            return null;
        }

        StringBuilder sb = SharedStringBuilder.get(SB_ESCAPE);

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '/':
                    sb.append("\\/");
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }

        return sb.toString();
    }

}
