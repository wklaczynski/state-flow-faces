/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.prime.scxml;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
import static org.apache.common.faces.state.StateFlow.FACES_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.FACES_RENDER_VIEW;
import static org.apache.common.faces.state.StateFlow.OUTCOME_EVENT_PREFIX;
import org.apache.common.faces.state.StateFlowHandler;
import org.apache.common.faces.state.annotation.StateChartInvoker;
import org.apache.common.scxml.Context;
import org.apache.common.scxml.EventBuilder;
import org.apache.common.scxml.InvokeContext;
import org.apache.common.scxml.SCInstance;
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import org.apache.common.scxml.model.ModelException;
import org.primefaces.component.api.ClientBehaviorRenderingMode;
import org.primefaces.context.RequestContext;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.Constants;

/**
 *
 * @author Waldemar Kłaczyński
 */
@StateChartInvoker("dialog")
public class SubDialogInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(SubDialogInvoker.class.getName());

    public static final String VIEW_PARAM = "@@@SubDialogInvoker@@@";

    private static final long serialVersionUID = 1L;
    private transient SCXMLExecutor executor;
    private transient String invokeId;
    private transient boolean cancelled;
    private String sourceId;
    private String viewId;

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
    public void invoke(final InvokeContext ictx, final String source, final Map params) throws InvokerException {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            Flash flash = context.getExternalContext().getFlash();

            String viewId = source;

            RequestContext requestContext = RequestContext.getCurrentInstance();
            Map<Object, Object> attrs = context.getAttributes();

            Map<String, List<String>> dialogParams = (Map<String, List<String>>) attrs.get(Constants.DIALOG_FRAMEWORK.PARAMS);
            Map<String, List<String>> vparams = new LinkedHashMap();
            if (dialogParams != null) {
                vparams.putAll(dialogParams);
            }

            Map<String, Object> options = new HashMap<>();
            options.put("resizable", "false");
            Map<String, Object> ajax = new HashMap<>();

            for (Object key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (skey.startsWith("@dialog.")) {
                    skey = skey.substring(8);
                    options.put(skey, value.toString());
                } else if (skey.startsWith("@ajax.")) {
                    skey = skey.substring(6);
                    ajax.put(skey, value);
                } else {
                    if (value != null) {
                        flash.put(skey, value);
                    }
                }
            }

            vparams = SharedUtils.evaluateExpressions(context, vparams);

            String pfdlgcid = UUID.randomUUID().toString();

            params.put("pfdlgcid", Collections.singletonList(pfdlgcid));
            ViewHandler viewHandler = context.getApplication().getViewHandler();
            context.getExternalContext().getFlash().setRedirect(true);

            String gurl = viewHandler.getRedirectURL(
                    context,
                    viewId,
                    vparams,
                    true);

            if (!gurl.contains("?")) {
                gurl = gurl + "?full=true";
            } else {
                gurl = gurl + "&full=true";
            }

            options.put("modal", "true");

            StringBuilder sb = new StringBuilder();
            UIViewRoot view = context.getViewRoot();

            String sourceComponentId = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_COMPONENT);
            String sourceWidget = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_WIDGET);

            view.getViewMap(true).put(VIEW_PARAM, this);

            String formId = null;

            AjaxRequestBuilder builder = requestContext.getAjaxRequestBuilder();
            ClientBehaviorRenderingMode renderingMode = ClientBehaviorRenderingMode.OBSTRUSIVE;
            UIComponent form = null;

            if (sourceComponentId != null) {
                UIComponent component = context.getViewRoot().findComponent(sourceComponentId);
                if (component != null) {
                    form = ComponentTraversalUtils.closestForm(context, component);
                    if (form != null) {
                        formId = form.getClientId(context);
                    }
                }
            }
            sourceId = form.getClientId();

            String script = builder.init()
                    .source(sourceId)
                    .form(formId)
                    .event("scxmlHide")
                    .process(form, "@none")
                    .update(form, "@none")
                    .async(false)
                    .global(true)
                    .delay(null)
                    .timeout(0)
                    .partialSubmit(false, false, null)
                    .resetValues(false, false)
                    .ignoreAutoUpdate(false)
                    .onstart(null)
                    .onerror(null)
                    .onsuccess(null)
                    .oncomplete(null)
                    .buildBehavior(renderingMode);

            sb.append("SabaUI.openSCXMLDialog({url:'").append(gurl).append("',pfdlgcid:'").append(pfdlgcid)
                    .append("',sourceId:'").append(sourceId).append("'");

            sb.append(",options:{");
            if (options != null && options.size() > 0) {
                for (Iterator<String> it = options.keySet().iterator(); it.hasNext();) {
                    String optionName = it.next();
                    Object optionValue = options.get(optionName);

                    sb.append(optionName).append(":").append(optionValue);

                    if (it.hasNext()) {
                        sb.append(",");
                    }
                }
            }
            sb.append("}");
            sb.append(",behaviors:{");
            sb.append("scxmlHide:");
            sb.append("function(ext) {");
            sb.append(script);
            sb.append("}");
            sb.append("}});");
            requestContext.getScriptsToExecute().add(sb.toString());
            sb.setLength(0);

            flash.setKeepMessages(true);
            flash.setRedirect(true);
            SharedUtils.doLastPhaseActions(context, true);
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

        if (event.getType() == TriggerEvent.CALL_EVENT && (event.getName()
                .startsWith(FACES_PHASE_EVENT_PREFIX))) {
            if (viewId.equals(event.getSendId())) {
                FacesContext context = FacesContext.getCurrentInstance();
                try {
                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                    context.getELContext().putContext(SCXMLExecutor.class, executor);

                    Context stateContext = ictx.getContext();
                    context.getELContext().putContext(Context.class, stateContext);

                    if (event.getName().startsWith(FACES_RENDER_VIEW)) {
                        UIViewRoot view = context.getViewRoot();

                        UIComponent fresource = createResource(context, "flowfaces.js", "flowfaces", "javax.faces.resource.Script");
                        fresource.setId("flowfaces_flowfaces.js");

                        List<UIComponent> resources = view.getComponentResources(context, "head");

                        view.addComponentResource(context, view, "head");

                    }
                } catch (ModelException ex) {
                    throw new InvokerException(ex);
                }
            }
            return;
        }

        if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
            if (viewId.equals(event.getSendId())) {
                FacesContext context = FacesContext.getCurrentInstance();
                UIViewRoot view = context.getViewRoot();
                ExternalContext ec = context.getExternalContext();

                Map<String, String> params = new HashMap<>();
                params.putAll(ec.getRequestParameterMap());

                String outcome = event.getName().substring(OUTCOME_EVENT_PREFIX.length());
                EventBuilder evb = new EventBuilder("view.action." + outcome + "." + invokeId, TriggerEvent.SIGNAL_EVENT);

                evb.data(params);
                evb.sendId(invokeId);
                executor.addEvent(evb.build());
            }
        }

//        SCXMLExecutor executor = manager.getExecutor(parentSCInstance.getExecutor());
//        boolean doneBefore = executor.getCurrentStatus().isFinal();
//        try {
//            executor.triggerEvents(evts);
//        } catch (ModelException me) {
//            throw new InvokerException(me.getMessage(), me.getCause());
//        }
//        if (!doneBefore && executor.getCurrentStatus().isFinal()) {
//
//            Context ctx = executor.getRootContext();
//            if (ctx.has("__@result@__")) {
//                Context result = (Context) ctx.get("__@result@__");
//                Status pstatus = parentSCInstance.getExecutor().getCurrentStatus();
//                State pstate = (State) pstatus.getStates().iterator().next();
//                Context pcontext = parentSCInstance.getContext(pstate);
//                pcontext.setLocal("__@result@__", result);
//            }
//
//            
//            RequestContext requestContext = RequestContext.getCurrentInstance();
//            FacesContext context = FacesContext.getCurrentInstance();
//            Map<String, String> params = context.getExternalContext().getRequestParameterMap();
//            String pfdlgcid = params.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
//            requestContext.execute("parent.SabaUI.closeSCXMLDialog({pfdlgcid:'" + pfdlgcid + "'});");
//
//        }
    }

    public void decode(FacesContext context) throws InvokerException {
        if (cancelled) {
            return;
        }

        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String behaviorEvent = params.get("javax.faces.behavior.event");
        if ("scxmlHide".equals(behaviorEvent)) {
            String behaviorSource = params.get("javax.faces.source");
            if (behaviorSource != null && sourceId.equals(behaviorSource)) {
                try {
                    StateFlowHandler handler = StateFlowHandler.getInstance();

                    //manager.stop(parentSCInstance.getExecutor());
                    String viewId = context.getViewRoot().getViewId();

                    ViewHandler viewHandler = context.getApplication().getViewHandler();

                    String gurl = viewHandler.getRedirectURL(
                            context,
                            viewId,
                            null,
                            true);

                    context.getExternalContext().getFlash().setKeepMessages(true);
                    context.getExternalContext().redirect(gurl);
                    context.responseComplete();
                } catch (IOException ex) {
                    throw new InvokerException(ex);
                }
            }
        }

    }

    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot view = context.getViewRoot();
        Map<String, Object> viewMap = view.getViewMap(false);
        if (viewMap != null) {
            viewMap.remove(VIEW_PARAM);
        }

        RequestContext requestContext = RequestContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        String pfdlgcid = params.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
        requestContext.getScriptsToExecute().add("FacesFlowUI.closeSCXMLDialog({pfdlgcid:'" + pfdlgcid + "'});");
    }

    private UIComponent createResource(FacesContext context, String name, String library, String renderer) {
        UIComponent resource = context.getApplication().createComponent("javax.faces.Output");
        resource.setRendererType(renderer);

        Map<String, Object> attrs = resource.getAttributes();
        attrs.put("name", name);
        attrs.put("library", library);
        attrs.put("target", "head");

        return resource;
    }

}
