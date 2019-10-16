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
package org.ssoft.faces.impl.state.cdi;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import org.ssoft.faces.impl.state.log.FlowLogger;
import javax.faces.state.annotation.DialogScoped;
import javax.faces.state.annotation.ChartScoped;
import javax.faces.state.annotation.StateScoped;
import org.kohsuke.MetaInfServices;

/**
 *
 * @author Waldemar Kłaczyński
 */
@MetaInfServices(Extension.class)
public class StateFlowCDIExtension implements Extension {

    private boolean cdiOneOneOrGreater = false;

    private final Map<Contextual<?>, StateCDIContext.TargetBeanInfo> targetScopedBeanFlowIds;

    /**
     *
     */
    public static final Logger log = FlowLogger.CDI.getLogger();

    /**
     *
     */
    public StateFlowCDIExtension() {
        cdiOneOneOrGreater = CdiUtil.isCdiOneOneOrGreater();
        targetScopedBeanFlowIds = new ConcurrentHashMap<>();
    }

    /**
     *
     * @param event
     * @param beanManager
     */
    public void beforeBean(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addScope(ChartScoped.class, true, true);
        event.addScope(DialogScoped.class, true, true);
        event.addScope(StateScoped.class, true, true);
    }

    /**
     *
     * @param event
     */
    public void processBean(@Observes ProcessBean<?> event) {
        DialogScoped dialogScoped = event.getAnnotated().getAnnotation(DialogScoped.class);
        if (dialogScoped != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Processing occurrence of @StateDialogScoped");
        }

        ChartScoped chartScoped = event.getAnnotated().getAnnotation(ChartScoped.class);
        if (chartScoped != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Processing occurrence of @StateChartScoped");
        }

       StateScoped targetScoped = event.getAnnotated().getAnnotation(StateScoped.class);
       if (null != targetScoped) {
           StateCDIContext.TargetBeanInfo fbi = new StateCDIContext.TargetBeanInfo();
           fbi.id = targetScoped.value();
           fbi.baseType = event.getAnnotated().getBaseType();
           
           targetScopedBeanFlowIds.put(event.getBean(), fbi);
       }
    }

    /**
     *
     * @param event
     * @param beanManager
     */
    public void afterBean(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {

        event.addContext(new FlowCDIContext());
        event.addContext(new DialogCDIContext());
        event.addContext(new ChartCDIContext());
        event.addContext(new StateCDIContext(targetScopedBeanFlowIds));
        targetScopedBeanFlowIds.clear();

//        event.addBean(new DialogContextProducer());
//        event.addBean(new ChartContextProducer());
//        event.addBean(new StateContextProducer());
        
        event.addBean(new PathResolverProducer());
        event.addBean(new ExecutorResolverProducer());

        if (cdiOneOneOrGreater) {
            Class clazz;
            try {
                clazz = Class.forName(StateFlowCDIEventFireHelperImpl.class.getName());
            } catch (ClassNotFoundException ex) {
                if (log.isLoggable(Level.SEVERE)) {
                    log.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
                }
                return;
            }

            Bean bean = CdiUtil.createHelperBean(beanManager, clazz);
            event.addBean(bean);
        }

    }

}
