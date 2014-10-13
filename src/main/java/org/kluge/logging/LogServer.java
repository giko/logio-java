package org.kluge.logging;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.server.RxServer;
import org.kluge.logging.model.LogNode;
import org.kluge.logging.model.LogServerEvent;
import rx.Observable;
import rx.Observer;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giko on 10/12/2014.
 */
public class LogServer {
    private PublishSubject<LogServerEvent> eventPublishSubject;
    private RxServer<String, String> server;
    private Map<ObservableConnection<String, String>, LogNode> connections = new HashMap<ObservableConnection<String, String>, LogNode>();

    public void subscribe(Observer<LogServerEvent> observer) {
        eventPublishSubject.subscribe(observer);
    }

    public LogServer() {
        eventPublishSubject = PublishSubject.create();
        server = RxNetty.createTcpServer(28878, PipelineConfigurators.textOnlyConfigurator(), new LogServerConnectionHandler(eventPublishSubject, connections));
    }

    public void start() {
        server.start();
    }
}
