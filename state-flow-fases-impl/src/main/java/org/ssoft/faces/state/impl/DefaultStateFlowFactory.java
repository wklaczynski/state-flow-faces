/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ssoft.faces.state.impl;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.context.FacesContext;
import javax.faces.state.ModelException;
import javax.faces.state.ModelFileNotFoundException;
import javax.faces.state.PathResolver;
import javax.faces.state.model.StateChart;
import org.ssoft.faces.state.StateFlowHandlerImpl;
import org.ssoft.faces.state.log.FlowLogger;
import org.ssoft.faces.state.scxml.io.SCXMLParser;
import org.ssoft.faces.state.scxml.io.SimpleErrorHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DefaultStateFlowFactory {

    private static final Logger log = FlowLogger.FLOW.getLogger();

    private StateFlowCache cache;
    private PathResolver resolver;
    private final StateFlowHandlerImpl handler;
    
    public DefaultStateFlowFactory(StateFlowHandlerImpl handler) {
        this.handler = handler;
    }

    public final void init(StateFlowCache cache, long refreshPeriod, PathResolver resolver) {
        this.resolver = resolver;
        this.cache = initCache(cache);
    }

    private StateFlowCache initCache(StateFlowCache cache) {

        @SuppressWarnings("Convert2Lambda")
        StateFlowCache.MemberFactory stateFlowFactory = new StateFlowCache.MemberFactory() {
            @Override
            public StateChart newInstance(final URL key) throws IOException, ModelException {
                return createStateFlow(key);
            }
        };

        cache.setCacheFactories(stateFlowFactory);
        return cache;
    }

    public StateChart getStateFlow(FacesContext context, String uri) throws ModelException {
        return this.getStateFlow(context, resolveURL(uri));
    }

    public StateChart getStateFlow(FacesContext context, URL url) throws ModelException {
        StateChart result = getCache(context).getStateFlow(url);
        return result;
    }

    private StateChart createStateFlow(URL url) throws IOException, ModelException {
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Creating StateFlow for: {0}", url);
        }
        String path = resolver.resolvePath(url);

        StateChart stateFlow = null;
        if (path.endsWith(".scxml")) {
            stateFlow = createStateMachineFromScxml(path);
        } else {
            throw new ModelException(String.format("Unknow state machine format %s.", path));
        }
        return stateFlow;

    }

    private URL resolveURL(String uri) throws ModelException {
        URL url = resolver.resolveURL(uri);
        if (url == null) {
            throw new ModelFileNotFoundException("'" + uri + "' not found.");
        }
        return url;
    }

    private StateChart createStateMachineFromScxml(String path) throws ModelException, IOException {
        try {
            SimpleErrorHandler errHandler = new SimpleErrorHandler();
            
            URL resolveURL = resolver.resolveURL(path);

            StateChart stateFlow = SCXMLParser.parse(
                    resolveURL,
                    errHandler,
                    resolver.getResolver(path),
                    handler.getCustomActions());

            return stateFlow;
        } catch (SAXException ex) {
            throw new ModelException(ex);
        }

    }

    private StateFlowCache getCache(FacesContext context) {
        return this.cache;
    }

}
