/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.cdi;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.state.FlowContext;
import javax.faces.state.StateFlowExecutor;
import javax.servlet.http.HttpSessionEvent;
import org.ssoft.faces.state.log.FlowLogger;
import org.ssoft.faces.state.utils.Util;
import javax.faces.state.annotation.StateChartScoped;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateChartScopeCDIContex extends AbstractContext {

    private static final Logger LOGGER = FlowLogger.FLOW.getLogger();

    private static final String STORAGE_KEY = "_____@@@SopeStateChartContext____";
    private static final String SESSION_STORAGES_LIST = StateChartScopeCDIContex.class.getPackage().getName() + ".STATE_FOW_STORAGES";
    
    private final BeanManager beanManager;

    public StateChartScopeCDIContex(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        StateFlowExecutor executor = StateFlowUtils.getExecutor(fc);
        ContextualStorage contextualStorage;
        if (executor != null) {
            FlowContext context = executor.getRootContext();
            contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
            if (contextualStorage == null) {
                synchronized (this) {
                    if (createIfNotExist) {
                        contextualStorage = new ContextualStorage(beanManager, true, true);
                        context.set(STORAGE_KEY, contextualStorage);

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
            ec.getRequestMap().put(STORAGE_KEY, contextualStorage);
        } else {
            contextualStorage = (ContextualStorage) ec.getRequestMap().get(STORAGE_KEY);
        }
        return contextualStorage;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return StateChartScoped.class;
    }

    @Override
    public boolean isActive() {
        FacesContext fc = FacesContext.getCurrentInstance();
        boolean result = StateFlowUtils.getExecutor(fc) != null;
        if (!result) {
            ExternalContext ec = fc.getExternalContext();
            result = ec.getRequestMap().containsKey(STORAGE_KEY);
        }
        return result;
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

    public static void flowExecutorExited(StateFlowExecutor executor) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ContextualStorage contextualStorage;
        if (executor != null) {
            FlowContext context = executor.getRootContext();
            contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
            if (contextualStorage != null) {
                destroyAllActive(contextualStorage);
            }
        }
        BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
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
                Set<Bean<?>> availableBeans = beanManager.getBeans(flowCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext = beanManager.createCreationalContext(null);
                    StateFlowCDIEventFireHelper eventHelper = (StateFlowCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(), creationalContext);
                    eventHelper.fireExecutorDestroyedEvent(executor);
                }
            }
        }
    }

    public static void flowExecutorEntered(StateFlowExecutor executor) {
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
                    eventHelper.fireExecutorInitializedEvent(executor);
                }
            }
        }
    }

}
