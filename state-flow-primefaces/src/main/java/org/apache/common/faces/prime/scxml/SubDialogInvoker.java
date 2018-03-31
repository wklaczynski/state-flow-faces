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
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
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
import org.apache.common.scxml.SCXMLExecutor;
import org.apache.common.scxml.SCXMLIOProcessor;
import org.apache.common.scxml.TriggerEvent;
import org.apache.common.scxml.invoke.Invoker;
import org.apache.common.scxml.invoke.InvokerException;
import org.apache.common.scxml.model.ModelException;
import org.primefaces.PrimeFaces;
import org.primefaces.component.api.ClientBehaviorRenderingMode;
import org.primefaces.context.RequestContext;
import org.primefaces.util.AjaxRequestBuilder;
import org.primefaces.util.ComponentTraversalUtils;
import org.primefaces.util.ComponentUtils;
import org.primefaces.util.Constants;

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
    ,@ResourceDependency(library = "flowfaces", name = "flow.js")
})
public class SubDialogInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(SubDialogInvoker.class.getName());

    public static final String VIEW_PARAM = "@@@SubDialogInvoker@@@";

    private static final long serialVersionUID = 1L;

    private transient SCXMLExecutor executor;
    private transient String invokeId;
    private transient boolean cancelled;
    private String sourceId;
    private String viewId;
    private String pfdlgcid;

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
            viewId = source;

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
            Map<String, List<String>> query = new HashMap<>();

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
                    query.put(skey, Collections.singletonList(value));
                }
            }

            //vparams = SharedUtils.evaluateExpressions(context, vparams);
            UIViewRoot view = context.getViewRoot();
            SharedUtils.loadResorces(context, view, this, "head");

            String url = context.getApplication().getViewHandler().getBookmarkableURL(context, viewId, query, true);
            url = ComponentUtils.escapeEcmaScriptText(url);

            pfdlgcid = UUID.randomUUID().toString();
            StringBuilder sb = new StringBuilder();
            String sourceComponentId = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_COMPONENT);
            String sourceWidget = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_WIDGET);
            pfdlgcid = ComponentUtils.escapeEcmaScriptText(pfdlgcid);

            options.put("modal", "true");

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
                sourceId = form.getClientId();
            }

            PrimeFaces.current().executeScript(sb.toString());
            sb.setLength(0);

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

            sb.append("PrimeFaces.openDialog({url:'").append(url).append("',pfdlgcid:'").append(pfdlgcid)
                    .append("',sourceComponentId:'").append(sourceComponentId).append("'");

            if (sourceWidget != null) {
                sb.append(",sourceWidgetVar:'").append(sourceWidget).append("'");
            }

            sb.append(",options:{");
            if (options != null && options.size() > 0) {
                for (Iterator<String> it = options.keySet().iterator(); it.hasNext();) {
                    String optionName = it.next();
                    Object optionValue = options.get(optionName);

                    sb.append(optionName).append(":");
                    if (optionValue instanceof String) {
                        sb.append("'").append(ComponentUtils.escapeEcmaScriptText((String) optionValue)).append("'");
                    } else {
                        sb.append(optionValue);
                    }

                    if (it.hasNext()) {
                        sb.append(",");
                    }
                }
            }
            sb.append("}});");
            PrimeFaces.current().executeScript(sb.toString());
            sb.setLength(0);

//            sb.append("FacesFlowUI.openSCXMLDialog({url:'").append(gurl).append("',pfdlgcid:'").append(pfdlgcid)
//                    .append("',sourceId:'").append(sourceId).append("'");
//
//            sb.append(",options:{");
//            if (options != null && options.size() > 0) {
//                for (Iterator<String> it = options.keySet().iterator(); it.hasNext();) {
//                    String optionName = it.next();
//                    Object optionValue = options.get(optionName);
//
//                    sb.append(optionName).append(":").append(optionValue);
//
//                    if (it.hasNext()) {
//                        sb.append(",");
//                    }
//                }
//            }
//            sb.append("}");
//            sb.append(",behaviors:{");
//            sb.append("scxmlHide:");
//            sb.append("function(ext) {");
//            sb.append(script);
//            sb.append("}");
//            sb.append("}});");
//            requestContext.getScriptsToExecute().add(sb.toString());
//            sb.setLength(0);
//            SharedUtils.doLastPhaseActions(context, true);
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

                    String cviewId = context.getViewRoot().getViewId();

                    ViewHandler viewHandler = context.getApplication().getViewHandler();

                    String gurl = viewHandler.getRedirectURL(
                            context,
                            cviewId,
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
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();
        //String pfdlgcid = ComponentUtils.escapeEcmaScriptText(params.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM));

        Object data = null;

        if (data != null) {
            Map<String, Object> session = context.getExternalContext().getSessionMap();
            session.put(pfdlgcid, data);
        }

        PrimeFaces.current().executeScript("PrimeFaces.closeDialog({pfdlgcid:'" + pfdlgcid + "'});");

    }

}
