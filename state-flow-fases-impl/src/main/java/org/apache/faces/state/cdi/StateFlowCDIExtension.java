/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.cdi;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.faces.state.annotation.StateScoped;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.faces.state.utils.Util;
import javax.faces.state.annotation.ParallelScoped;
import javax.faces.state.annotation.DialogScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIExtension implements Extension {

    private boolean cdiOneOneOrGreater = false;

    private final Log log = LogFactory.getLog(StateFlowCDIExtension.class);

    public StateFlowCDIExtension() {
        cdiOneOneOrGreater = Util.isCdiOneOneOrGreater();
    }

    public void beforeBean(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addScope(DialogScoped.class, true, true);
        event.addScope(ParallelScoped.class, true, true);
        event.addScope(StateScoped.class, true, true);
    }

    public void processBean(@Observes ProcessBean<?> event) {
        DialogScoped dialogScoped = event.getAnnotated().getAnnotation(DialogScoped.class);
        if (dialogScoped != null && log.isDebugEnabled()) {
            log.debug("Processing occurrence of @DialogScoped");
        }
        ParallelScoped parallerScoped = event.getAnnotated().getAnnotation(ParallelScoped.class);
        if (parallerScoped != null && log.isDebugEnabled()) {
            log.debug("Processing occurrence of @ParallerScoped");
        }
        StateScoped stateScoped = event.getAnnotated().getAnnotation(StateScoped.class);
        if (stateScoped != null && log.isDebugEnabled()) {
            log.debug("Processing occurrence of @StateScoped");
        }

    }

    public void afterBean(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {

        event.addContext(new DialogScopeCDIContex(beanManager));
        event.addContext(new ParallelScopeCDIContext(beanManager));
        event.addContext(new StateScopeCDIContex(beanManager));
//        event.addBean(new NamespaceMapProducer());
//        event.addBean(new PathResolverProducer());

        if (cdiOneOneOrGreater) {
            Class clazz;
            try {
                clazz = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (log.isErrorEnabled()) {
                    log.error("CDI 1.1 events not enabled", ex);
                }
                return;
            }

            Bean bean = CdiUtil.createHelperBean(beanManager, clazz);
            event.addBean(bean);
        }

    }

}
