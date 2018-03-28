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
package org.apache.common.faces.impl.state.cdi;

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
import org.apache.common.faces.state.annotation.StateChartScoped;
import org.apache.common.faces.impl.state.log.FlowLogger;
import org.apache.common.faces.state.annotation.StateDialogScoped;
import org.apache.common.faces.state.annotation.StateTargetScoped;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class StateFlowCDIExtension implements Extension {

    private boolean cdiOneOneOrGreater = false;

    private final Map<Contextual<?>, StateTargetCDIContext.TargetBeanInfo> targetScopedBeanFlowIds;

    public static final Logger log = FlowLogger.CDI.getLogger();

    public StateFlowCDIExtension() {
        cdiOneOneOrGreater = CdiUtil.isCdiOneOneOrGreater();
        targetScopedBeanFlowIds = new ConcurrentHashMap<>();
    }

    public void beforeBean(@Observes final BeforeBeanDiscovery event, BeanManager beanManager) {
        event.addScope(StateChartScoped.class, true, true);
        event.addScope(StateDialogScoped.class, true, true);
        event.addScope(StateTargetScoped.class, true, true);
    }

    public void processBean(@Observes ProcessBean<?> event) {
        StateDialogScoped dialogScoped = event.getAnnotated().getAnnotation(StateDialogScoped.class);
        if (dialogScoped != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Processing occurrence of @StateDialogScoped");
        }

        StateChartScoped chartScoped = event.getAnnotated().getAnnotation(StateChartScoped.class);
        if (chartScoped != null && log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Processing occurrence of @StateChartScoped");
        }

       StateTargetScoped targetScoped = event.getAnnotated().getAnnotation(StateTargetScoped.class);
       if (null != targetScoped) {
           StateTargetCDIContext.TargetBeanInfo fbi = new StateTargetCDIContext.TargetBeanInfo();
           fbi.id = targetScoped.value();
           targetScopedBeanFlowIds.put(event.getBean(), fbi);
       }
    }

    public void afterBean(@Observes final AfterBeanDiscovery event, BeanManager beanManager) {

        event.addContext(new StateChartCDIContext());
        event.addContext(new StateDialogCDIContext());
        event.addContext(new StateTargetCDIContext(targetScopedBeanFlowIds));
        targetScopedBeanFlowIds.clear();

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
