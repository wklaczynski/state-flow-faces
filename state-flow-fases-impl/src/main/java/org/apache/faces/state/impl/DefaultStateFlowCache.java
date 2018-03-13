/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.faces.state.impl;

import com.sun.faces.util.ConcurrentCache;
import com.sun.faces.util.ExpiringConcurrentCache;
import com.sun.faces.util.Util;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;
import javax.faces.state.ModelException;
import javax.faces.state.model.StateChart;
import org.apache.faces.state.log.FlowLogger;

/**
 *
 * @author Waldemar Kłaczyński
 */
public class DefaultStateFlowCache extends StateFlowCache {

    private static final Logger log = FlowLogger.FLOW.getLogger();

    private final ConcurrentCache<URL, Record> stateFlowCache;

    public DefaultStateFlowCache(final long refreshPeriod) {

        final boolean checkExpiry = (refreshPeriod > 0);

        @SuppressWarnings("Convert2Lambda")
        ConcurrentCache.Factory<URL, Record> faceletFactory = new ConcurrentCache.Factory<URL, Record>() {

            @Override
            public Record newInstance(final URL key) throws IOException, ModelException {

                long lastModified = checkExpiry ? Util.getLastModified(key) : 0;
                return new Record(System.currentTimeMillis(), lastModified, getMemberFactory().newInstance(key), refreshPeriod);
            }
        };

        if (refreshPeriod == 0) {
            stateFlowCache = new NoCache(faceletFactory);
        } else {
            ExpiringConcurrentCache.ExpiryChecker<URL, Record> checker = (refreshPeriod > 0) ? new ExpiryChecker() : new NeverExpired();
            stateFlowCache = new ExpiringConcurrentCache<>(faceletFactory, checker);
        }

    }

    @Override
    public StateChart getStateFlow(URL url) throws ModelException {
        if (url == null) {
            throw new IllegalStateException("Parametr path can not be null");
        }

        StateChart f = null;
        try {
            f = stateFlowCache.get(url).getStateflow();
        } catch (ExecutionException e) {
            unwrapException(e);
        }
        return f;
    }

    private void unwrapException(ExecutionException e) throws ModelException {
        Throwable t = e.getCause();
        if (t instanceof ModelException) {
            throw (ModelException) t;
        }
        if (t.getCause() instanceof ModelException) {
            throw (ModelException) t.getCause();
        }
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        }
        throw new ModelException(t);
    }

    @Override
    public boolean isStateFlowCached(URL url) {
        if (url == null) {
            throw new IllegalStateException("Parametr path can not be null");
        }
        return stateFlowCache.containsKey(url);
    }

    private static class Record {

        Record(long creationTime, long lastModified, StateChart facelet, long refreshInterval) {
            this.stateflow = facelet;
            this.creationTime = creationTime;
            this.lastModified = lastModified;
            this.refreshInterval = refreshInterval;

            // There is no point in calculating the next refresh time if we are refreshing always/never
            this.nextRefreshTime = (refreshInterval > 0) ? new AtomicLong(creationTime + refreshInterval) : null;
        }

        StateChart getStateflow() {
            return stateflow;
        }

        long getLastModified() {
            return lastModified;
        }

        long getNextRefreshTime() {
            // There is no point in calculating the next refresh time if we are refreshing always/never
            return (refreshInterval > 0) ? nextRefreshTime.get() : 0;
        }

        long getAndUpdateNextRefreshTime() {
            // There is no point in calculating the next refresh time if we are refreshing always/never
            return (refreshInterval > 0) ? nextRefreshTime.getAndSet(System.currentTimeMillis() + refreshInterval) : 0;
        }

        private final long lastModified;
        private final long refreshInterval;
        private final long creationTime;
        private final AtomicLong nextRefreshTime;
        private final StateChart stateflow;
    }

    private static class ExpiryChecker implements ExpiringConcurrentCache.ExpiryChecker<URL, Record> {

        @Override
        public boolean isExpired(URL url, Record record) {
            if (System.currentTimeMillis() > record.getNextRefreshTime()) {
                record.getAndUpdateNextRefreshTime();
                long lastModified = Util.getLastModified(url);
                // The record is considered expired if its original last modified time
                // is older than the URL's current last modified time
                return (lastModified > record.getLastModified());
            }
            return false;
        }
    }

    private static class NeverExpired implements ExpiringConcurrentCache.ExpiryChecker<URL, Record> {

        @Override
        public boolean isExpired(URL key, Record value) {
            return false;
        }
    }

    /**
     * ConcurrentCache implementation that does no caching (always creates new
     * instances)
     */
    private static class NoCache extends ConcurrentCache<URL, Record> {

        public NoCache(ConcurrentCache.Factory<URL, Record> f) {
            super(f);
        }

        @Override
        public Record get(final URL key) throws ExecutionException {
            try {
                return this.getFactory().newInstance(key);
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        }

        @Override
        public boolean containsKey(final URL key) {
            return false;
        }
    }

}
