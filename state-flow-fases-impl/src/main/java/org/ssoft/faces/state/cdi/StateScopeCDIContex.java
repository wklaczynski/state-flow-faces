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
package org.ssoft.faces.state.cdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.state.annotation.StateScoped;
import javax.scxml.Context;
import javax.scxml.SCXMLExecutor;
import javax.scxml.model.EnterableState;
import javax.scxml.model.State;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import org.ssoft.faces.state.log.FlowLogger;
import static org.ssoft.faces.state.cdi.AbstractContext.destroyAllActive;
import org.ssoft.faces.state.utils.Util;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateScopeCDIContex extends AbstractContext {

    private static final Logger LOGGER = FlowLogger.FLOW.getLogger();

    private static final String SESSION_STORAGES_LIST = StateScopeCDIContex.class.getPackage().getName() + ".STATE_STORAGES";
    private static final String STORAGE_KEY = "_____@@@SopeTransitionContext___";

    private final BeanManager beanManager;

    public StateScopeCDIContex(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    private static Context getStateContext(SCXMLExecutor executor, final FacesContext fc) {
        Context context = null;
        Iterator iterator = executor.getStatus().getStates().iterator();
        if (iterator.hasNext()) {
            EnterableState state = ((EnterableState) iterator.next());
            //context = state.getInstance().getContext(state);
        }
        return context;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        SCXMLExecutor executor = StateFlowUtils.getExecutor(fc);
        if (executor == null) {
            throw new ContextNotActiveException("StateFlowExecutor: no executor set for the current Thread yet!");
        }
        Context context = getStateContext(executor, fc);
        ContextualStorage contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
        if (contextualStorage == null) {
            synchronized (this) {
                if (createIfNotExist) {
                    contextualStorage = new ContextualStorage(beanManager, true, true);

                    context.setLocal(STORAGE_KEY, contextualStorage);

                    HttpSession session = (HttpSession) ec.getSession(true);
                    List<ContextualStorage> strlist = (List<ContextualStorage>) session.getAttribute(SESSION_STORAGES_LIST);
                    if (strlist == null) {
                        strlist = new ArrayList<>();
                        session.setAttribute(SESSION_STORAGES_LIST, strlist);
                    }
                    strlist.add(contextualStorage);
                }
            }
        }
        return contextualStorage;
    }

    @Override
    public boolean isActive() {
        return StateFlowUtils.getExecutor() != null;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return StateScoped.class;
    }

    public static void sessionDestroyed(HttpSessionEvent hse) {
        HttpSession session = hse.getSession();
        List<ContextualStorage> strlist = (List<ContextualStorage>) session.getAttribute(SESSION_STORAGES_LIST);
        if (strlist != null) {
            for (ContextualStorage contextualStorage : strlist) {
                destroyAllActive(contextualStorage);
            }

            strlist.clear();
        }
    }

    public static void flowStateExited(State state) {
        FacesContext fc = FacesContext.getCurrentInstance();
        SCXMLExecutor executor = StateFlowUtils.getExecutor(fc);
        if (executor == null) {
            throw new ContextNotActiveException("StateFlowExecutor: no executor set for the current Thread yet!");
        }

        ContextualStorage contextualStorage;
        if (state != null) {
            Context context = null;//executor.getFlowInstance().getContext(state);
//            contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
//            if (contextualStorage != null) {
//                destroyAllActive(contextualStorage);
//            }
        }
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(fc);
        if (Util.isCdiOneOneOrLater(fc)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }

            if (null != flowCDIEventFireHelperImplClass) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
                    eventHelper.fireStateDestroyedEvent(state);
                }
            }
        }
    }

    public static void flowStateEntered(State state) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (Util.isCdiOneOneOrLater(facesContext)) {
            Class flowCDIEventFireHelperImplClass = null;
            try {
                flowCDIEventFireHelperImplClass = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
            }
            if (null != flowCDIEventFireHelperImplClass) {
                BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
                    eventHelper.fireStateInitializedEvent(state);
                }
            }
        }
    }

}
