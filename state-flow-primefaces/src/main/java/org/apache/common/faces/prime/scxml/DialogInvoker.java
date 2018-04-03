/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.prime.scxml;

import org.apache.common.faces.prime.PrimeFacesFlowUtils;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.application.Resource;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.application.ResourceHandler;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import static org.apache.common.faces.prime.PrimeFacesFlowUtils.applyParams;
import static org.apache.common.faces.state.StateFlow.AFTER_PHASE_EVENT_PREFIX;
import static org.apache.common.faces.state.StateFlow.AFTER_RESTORE_VIEW;
import static org.apache.common.faces.state.StateFlow.CURRENT_COMPONENT_HINT;
import static org.apache.common.faces.state.StateFlow.CURRENT_EXECUTOR_HINT;
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
})
public class DialogInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(DialogInvoker.class.getName());

    public static final String VIEW_PARAM = "@@@SubDialogInvoker@@@";

    private static final long serialVersionUID = 1L;

    private transient SCXMLExecutor executor;
    private transient String invokeId;
    private transient boolean cancelled;
    private String viewId;
    private String pfdlgcid;
    private Map<String, Object> vieparams;
    private String ownerViewId;

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
            Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();
            Map<Object, Object> attrs = context.getAttributes();

            viewId = source;

            Map<String, Object> options = new HashMap<>();
            options.put("resizable", "false");

            Map<String, Object> ajax = new HashMap<>();
            Map<String, List<String>> query = new HashMap<>();

            vieparams = new HashMap();
            for (Object key : params.keySet()) {
                String skey = (String) key;
                Object value = params.get(key);
                if (skey.startsWith("@query.param.")) {
                    skey = skey.substring(16);
                    query.put(skey, Collections.singletonList(value.toString()));
                } else if (skey.startsWith("@dialog.param")) {
                    skey = skey.substring(13);
                    options.put(skey, value.toString());
                } else if (skey.startsWith("@ajax.")) {
                    skey = skey.substring(6);
                    ajax.put(skey, value);
                } else {
                    vieparams.put(skey, Collections.singletonList(value.toString()));
                }
            }

            UIViewRoot view = context.getViewRoot();
            
            ownerViewId = view.getViewId();
            
            PrimeFacesFlowUtils.loadResorces(context, view, this, "head");

            String url = context.getApplication().getViewHandler().getBookmarkableURL(context, viewId, params, true);
            url = ComponentUtils.escapeEcmaScriptText(url);

            pfdlgcid = UUID.randomUUID().toString();
            String sourceComponentId = (String) attrs.get(CURRENT_COMPONENT_HINT);

            pfdlgcid = requestParams.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
            if (pfdlgcid == null) {
                pfdlgcid = UUID.randomUUID().toString();
            }

            String widgetVar = "widget_" + invokeId;

            options.put("modal", "true");
            options.put("closable", "false");
            options.put("invokeId", invokeId);

            String update = (String) ajax.get("update");
            String process = (String) ajax.get("update");
            String global = (String) ajax.get("global");
            
            RequestContext requestContext = RequestContext.getCurrentInstance();
            AjaxRequestBuilder builder = requestContext.getAjaxRequestBuilder();
            ClientBehaviorRenderingMode renderingMode = ClientBehaviorRenderingMode.OBSTRUSIVE;

            String formId = null;
            UIComponent form;
            String sourceId = view.getId();
            UIComponent component;

            if (sourceComponentId != null) {
                component = context.getViewRoot().findComponent(sourceComponentId);
                if (component != null) {
                    form = ComponentTraversalUtils.closestForm(context, component);
                    if (form != null) {
                        formId = form.getClientId(context);
                    }
                    sourceId = component.getClientId();
                }
            } else {
                sourceId = view.getId();
                component = view;
                update = "@all";
            }

            String ajaxscript = builder.init()
                    .source(sourceId)
                    .form(formId)
                    .event("scxmlhide")
                    .update(component, update != null ? update : "@form")
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

            StringBuilder sb = new StringBuilder();

            ResourceHandler resourceHandler = context.getApplication().getResourceHandler();
            Resource resource = resourceHandler.createResource("scxml.js", "flowfaces");
            String scpath = resource.getRequestPath();

            sb.append("PrimeFaces.getScript('").append(scpath)
                    .append("', function(){");

            sb.append("PrimeFaces.cw(\"ScxmlDialogInvoker\",\"")
                    .append(widgetVar)
                    .append("\",{id:\"").append(invokeId).append("\"");

            sb.append(",behaviors:{");
            sb.append("dialogReturn:")
                    .append("function(ext) {")
                    .append(ajaxscript)
                    .append("}");
            sb.append("}});");

            sb.append("PrimeFaces.scxml.openScxmlDialog({url:'").append(url)
                    .append("',pfdlgcid:'").append(pfdlgcid)
                    .append("',sourceComponentId:'")
                    .append(sourceId).append("'");

            if (widgetVar != null) {
                sb.append(",sourceWidgetVar:'").append(widgetVar).append("'");
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

            sb.append("});");
            PrimeFaces.current().executeScript(sb.toString());
            sb.setLength(0);
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
            if (event.getName().startsWith(AFTER_RESTORE_VIEW)) {
                if (vieparams != null && context.getResponseComplete()) {
                    UIViewRoot viewRoot = context.getViewRoot();
                    applyParams(context, viewRoot, vieparams);
                    vieparams = null;
                }
            }

            if (event.getName().startsWith(AFTER_PHASE_EVENT_PREFIX)) {
                try {
                    context.getAttributes().put(CURRENT_EXECUTOR_HINT, executor);
                    context.getELContext().putContext(SCXMLExecutor.class, executor);

                    Context stateContext = ictx.getContext();
                    context.getELContext().putContext(Context.class, stateContext);
                } catch (ModelException ex) {
                    throw new InvokerException(ex);
                }
            }

            if (event.getName().startsWith(OUTCOME_EVENT_PREFIX)) {
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
        }
    }

    @Override
    public void cancel() throws InvokerException {
        cancelled = true;

        StringBuilder sb = new StringBuilder();

        sb.append("parent.PrimeFaces.scxml.closeScxmlDialog({pfdlgcid:'")
                .append(pfdlgcid).append("'});");

        PrimeFaces.current().executeScript(sb.toString());
        sb.setLength(0);
    }

}
