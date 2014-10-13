package org.kluge.logging.model;

import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by giko on 10/13/2014.
 */
public class LogServerState {
    private Observable<LogServerEvent> cachingEventObservable;
    private PublishSubject<LogServerEvent> cachingEventSubject;
    private PublishSubject<LogServerEvent> nonCachingEventSubject;
    private Map<ObservableConnection<String, String>, LogNode> connections;
    private Set<LogStream> streams = new HashSet<>();

    public LogServerState() {
        connections = new HashMap<>();
        cachingEventSubject = PublishSubject.create();
        nonCachingEventSubject = PublishSubject.create();
        cachingEventObservable = cachingEventSubject.cache(100);
    }

    public Observable<LogServerEvent> getObservable() {
        return cachingEventObservable.mergeWith(nonCachingEventSubject);
    }

    public PublishSubject<LogServerEvent> getCachingEventSubject() {
        return cachingEventSubject;
    }

    public Observable<LogServerEvent> getCachingEventObservable() {
        return cachingEventObservable;
    }

    public PublishSubject<LogServerEvent> getNonCachingEventSubject() {
        return nonCachingEventSubject;
    }

    public Map<ObservableConnection<String, String>, LogNode> getConnections() {
        return connections;
    }

    public Set<LogStream> getStreams() {
        return streams;
    }
}
