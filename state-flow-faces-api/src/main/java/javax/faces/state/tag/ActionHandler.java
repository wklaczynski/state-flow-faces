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
package javax.faces.state.tag;

import java.io.IOException;
import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.MetaRuleset;
import javax.faces.view.facelets.MetaTagHandler;
import javax.faces.view.facelets.Tag;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagHandlerDelegate;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ActionHandler  extends MetaTagHandler {

    /**
     *
     */
    protected TagHandlerDelegateFactory delegateFactory;
    
    private final TagAttribute binding;

    private final TagAttribute disabled;
    
    private TagHandlerDelegate helper;
    
    /**
     *
     * @param config
     */
    public ActionHandler(TagConfig config) {
        super(config);
        this.binding = this.getAttribute("binding");
        this.disabled = this.getAttribute("disabled");
        
        FacesContext fc = FacesContext.getCurrentInstance();
        delegateFactory = (TagHandlerDelegateFactory) fc.getExternalContext()
                .getApplicationMap().get(TagHandlerDelegateFactory.KEY);
    }
    
    /**
     *
     * @param ctx
     * @return
     */
    public boolean isDisabled(FaceletContext ctx) {
        return disabled != null && Boolean.TRUE.equals(disabled.getBoolean(ctx));
    }
    
    /**
     *
     * @return
     */
    public TagAttribute getBinding() {
        return this.binding;
    }
    
    
    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        return getTagHandlerDelegate().createMetaRuleset(type);
    }

    /**
     *
     * @return
     */
    protected TagHandlerDelegate getTagHandlerDelegate() {
        if (null == helper) {
            helper = delegateFactory.createStateFlowActionDelegate(this);
        }
        return helper;
    }
    
    
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        getTagHandlerDelegate().apply(ctx, parent);
    }
    
    /**
     *
     * @return
     */
    public Tag getTag() {
        return this.tag;
    }
    
    /**
     *
     * @return
     */
    public String getTagId() {
        return this.tagId;
    }
    
    /**
     *
     * @param localName
     * @return
     */
    public TagAttribute getTagAttribute(String localName) {
        return super.getAttribute(localName);
    }
    
    /**
     *
     * @param ctx
     * @param c
     * @throws IOException
     * @throws FacesException
     * @throws ELException
     */
    public void applyNextHandler(FaceletContext ctx, UIComponent c) 
            throws IOException, FacesException, ELException {
        this.nextHandler.apply(ctx, c);
    }
    
}
