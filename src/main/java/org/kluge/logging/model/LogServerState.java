package org.kluge.logging.model;

import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by giko on 10/13/2014.
 */
public class LogServerState {
    private final Observable<LogServerEvent> cachingEventObservable;
    private final PublishSubject<LogServerEvent> cachingEventSubject;
    private final PublishSubject<LogServerEvent> nonCachingEventSubject;
    private final Map<ObservableConnection<String, String>, LogNode> connections;
    private final Set<LogStream> streams = new HashSet<>();

    public LogServerState() {
        connections = new HashMap<>();
        cachingEventSubject = PublishSubject.create();
        nonCachingEventSubject = PublishSubject.create();
        cachingEventObservable = cachingEventSubject.cacheWithInitialCapacity(100);
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
    
    public void addStream(LogStream stream){
        
    }

    public Map<ObservableConnection<String, String>, LogNode> getConnections() {
        return connections;
    }

    public Set<LogStream> getStreams() {
        return streams;
    }
}
