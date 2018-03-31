/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.common.faces.prime.scxml;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
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
public class DialogInvoker implements Invoker, Serializable {

    private final static Logger logger = Logger.getLogger(DialogInvoker.class.getName());

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
            Map<String, String> requestParams = context.getExternalContext().getRequestParameterMap();
            Map<Object, Object> attrs = context.getAttributes();

            viewId = source;
            
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
                    query.put(skey, Collections.singletonList(value.toString()));
                }
            }

            UIViewRoot view = context.getViewRoot();
            SharedUtils.loadResorces(context, view, this, "head");

            String url = context.getApplication().getViewHandler().getBookmarkableURL(context, viewId, query, true);
            url = ComponentUtils.escapeEcmaScriptText(url);

            pfdlgcid = UUID.randomUUID().toString();
            StringBuilder sb = new StringBuilder();
            String sourceComponentId = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_COMPONENT);
            String sourceWidget = (String) attrs.get(Constants.DIALOG_FRAMEWORK.SOURCE_WIDGET);
            pfdlgcid = requestParams.get(Constants.DIALOG_FRAMEWORK.CONVERSATION_PARAM);
            if (pfdlgcid == null) {
                pfdlgcid = UUID.randomUUID().toString();
            }

            options.put("modal", "true");

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
        }
    }

    @Override
    public void cancel() throws InvokerException {
        cancelled = true;
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext().getRequestParameterMap();

        Object data = null;

        if (data != null) {
            Map<String, Object> session = context.getExternalContext().getSessionMap();
            session.put(pfdlgcid, data);
        }

        PrimeFaces.current().executeScript("PrimeFaces.closeDialog({pfdlgcid:'" + pfdlgcid + "'});");
    }

}
