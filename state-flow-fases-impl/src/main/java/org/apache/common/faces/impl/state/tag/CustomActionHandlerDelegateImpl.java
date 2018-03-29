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
package org.apache.common.faces.impl.state.tag;

import com.sun.faces.util.Util;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.TagException;
import javax.faces.view.facelets.TagHandlerDelegate;
import org.apache.common.faces.impl.state.log.FlowLogger;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.CURRENT_FLOW_OBJECT;
import static org.apache.common.faces.impl.state.tag.AbstractFlowTagHandler.getElement;
import org.apache.common.faces.state.tag.ActionHandler;
import org.apache.common.scxml.model.Action;
import org.apache.common.scxml.model.CustomAction;
import org.apache.common.scxml.model.CustomActionWrapper;
import org.apache.common.scxml.model.Executable;
import org.apache.common.scxml.model.If;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class CustomActionHandlerDelegateImpl extends TagHandlerDelegate {

    protected static final Logger log = FlowLogger.TAGLIB.getLogger();
    
    private final ActionHandler owner;

    public CustomActionHandlerDelegateImpl(ActionHandler owner) {
        this.owner = owner;
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        Util.notNull("type", type);
        MetaRuleset m = new FlowMetaRulesetImpl(owner.getTag(), type);
        return m.ignore("binding").ignore("disabled").ignore("for");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        if (parent == null) {
            return;
        }

        String anamespace = owner.getTag().getNamespace();
        String aname = owner.getTag().getLocalName();
        String aqname = owner.getTag().getQName();
        String prefix = aqname;
        int ind = prefix.indexOf(':');
        if (ind >= 0) {
            prefix = prefix.substring(0, ind);
        }
        
        log.log(Level.INFO, "Handle custom action: {0}", aqname);

        Map<String, String> namespaces = new HashMap<>();
        namespaces.put(prefix, anamespace);

        List<CustomAction> customActions = AbstractFlowTagHandler.getCustomActions(ctx, parent);
        CustomAction customAction = null;
        if (!customActions.isEmpty()) {
            for (CustomAction ca : customActions) {
                if (ca.getNamespaceURI().equals(anamespace) && ca.getLocalName().equals(aname)) {
                    customAction = ca;
                    break;
                }
            }
        }

        if (customAction == null) {
            throw new TagException(owner.getTag(), String.format("action %s from %s is not registered.", aname, anamespace));
        }

        MetaRuleset ruleset;
        Object actionObject;
        String className = customAction.getActionClass().getName();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = CustomActionHandlerDelegateImpl.class.getClassLoader();
        }
        Class<?> clazz;
        try {
            clazz = cl.loadClass(className);
            actionObject = clazz.newInstance();
            ruleset = createMetaRuleset(clazz);

        } catch (ClassNotFoundException cnfe) {
            throw new TagException(owner.getTag(), "cannot find custom action class:" + className, cnfe);
        } catch (IllegalAccessException iae) {
            throw new TagException(owner.getTag(), "cannot access custom action class:" + className, iae);
        } catch (InstantiationException ie) {
            throw new TagException(owner.getTag(), "cannot instantiate custom action class:" + className, ie);
        }
        if (!(actionObject instanceof Action)) {
            throw new TagException(owner.getTag(), ERR_CUSTOM_ACTION_TYPE + className);
        }

        Action action = (Action) actionObject;

        CustomActionWrapper actionWrapper = new CustomActionWrapper();
        actionWrapper.setAction(action);
        actionWrapper.setPrefix(prefix);
        actionWrapper.setLocalName(aname);
        if (namespaces != null) {
            actionWrapper.getNamespaces().putAll(namespaces);
        }

        ruleset.finish().applyMetadata(ctx, action);

        Object currentFlow = getElement(parent, CURRENT_FLOW_OBJECT);

        if (currentFlow instanceof Executable) {
            Executable executable = (Executable) currentFlow;
            executable.addAction(action);
            action.setParent(executable);
        } else if (currentFlow instanceof If) {
            If ifaction = (If) currentFlow;
            ifaction.addAction(action);
            action.setParent(ifaction.getParent());
        } else {
            throw new TagException(owner.getTag(), "can not stored this element on parent element.");
        }
        
        applyNext(ctx, parent, action);

    }

    protected void applyNext(FaceletContext ctx, UIComponent parent, Object element) throws IOException {
        AbstractFlowTagHandler.applyNext(ctx, owner.getTag(), CustomAction.class, parent, element, () -> {
            owner.applyNextHandler(ctx, parent);
            return null;
        });
    }

    private static final String ERR_CUSTOM_ACTION_TYPE = "custom actions list"
            + " contained unknown object, class not a Commons SCXML Action class subtype: ";

}
