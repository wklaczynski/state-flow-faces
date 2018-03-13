/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.cdi;

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
import javax.faces.state.FlowContext;
import javax.faces.state.StateFlowExecutor;
import javax.faces.state.model.Parallel;
import javax.faces.state.model.State;
import javax.servlet.http.HttpSessionEvent;
import org.apache.faces.state.log.FlowLogger;
import static org.apache.faces.state.cdi.AbstractContext.destroyAllActive;
import org.apache.faces.state.utils.Util;
import javax.faces.state.annotation.ParallelScoped;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class ParallelScopeCDIContext extends AbstractContext {

    private static final Logger LOGGER = FlowLogger.FACES.getLogger();

    private static final String SESSION_STORAGES_LIST = StateScopeCDIContex.class.getPackage().getName() + ".PARALLER_STORAGES";
    private static final String STORAGE_KEY = "_____@@@SopeParallelContext____";

    private final BeanManager beanManager;

    public ParallelScopeCDIContext(BeanManager beanManager) {
        super(beanManager);
        this.beanManager = beanManager;
    }

    private static FlowContext getParallelContext(final FacesContext fc) {
        StateFlowExecutor executor = StateFlowUtils.getExecutor(fc);
        if (executor == null) {
            return null;
        }
        Iterator iterator = executor.getCurrentStatus().getStates().iterator();
        State state = ((State) iterator.next());
        Parallel parallel = state.getParallel();
        if (parallel == null) {
            return null;
        }

        FlowContext context = StateFlowUtils.getTransitionContext(fc, executor, parallel);
        return context;
    }

    @Override
    protected ContextualStorage getContextualStorage(Contextual<?> contextual, boolean createIfNotExist) {
        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();
        StateFlowExecutor executor = StateFlowUtils.getExecutor(fc);
        if (executor == null) {
            throw new ContextNotActiveException("StateFlowExecutor: no executor set for the current Thread yet!");
        }
        FlowContext context = getParallelContext(fc);
        if (context == null) {
            throw new ContextNotActiveException("StateFlowExecutor: no parallel set for the current Thread yet!");
        }
        ContextualStorage contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
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
        return contextualStorage;
    }

    @Override
    public boolean isActive() {
        FacesContext fc = FacesContext.getCurrentInstance();
        return getParallelContext(fc) != null;
    }

    @Override
    public Class<? extends Annotation> getScope() {
        return ParallelScoped.class;
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

    public static void flowParallelExited(Parallel parallel) {
        FacesContext fc = FacesContext.getCurrentInstance();
        StateFlowExecutor executor = StateFlowUtils.getExecutor(fc);
        if (executor == null) {
            throw new ContextNotActiveException("StateFlowExecutor: no executor set for the current Thread yet!");
        }

        ContextualStorage contextualStorage;
        if (executor != null) {
            FlowContext context = StateFlowUtils.getTransitionContext(fc, executor, parallel);
            contextualStorage = (ContextualStorage) context.get(STORAGE_KEY);
            if (contextualStorage != null) {
                destroyAllActive(contextualStorage);
            }
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
                    eventHelper.fireParallelDestroyedEvent(parallel);
                }
            }
        }
    }

    public static void flowParallelEntered(Parallel parallel) {
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
                    eventHelper.fireParallelInitializedEvent(parallel);
                }
            }
        }
    }

}
