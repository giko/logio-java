package org.kluge.logging;

import io.reactivex.netty.RxNetty;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.server.RxServer;
import org.kluge.logging.model.LogServerEvent;
import org.kluge.logging.model.LogServerState;
import org.kluge.logging.model.LogStream;
import rx.Observable;
import rx.Observer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by giko on 10/12/2014.
 */
public class LogServer {
    private RxServer<String, String> server;
    private LogServerState state = new LogServerState();

    public LogServer() {
        server = RxNetty.createTcpServer(28878, PipelineConfigurators.textOnlyConfigurator(), new LogServerConnectionHandler(state));
    }

    private Observable<LogServerEvent> generateWelcomingEvents() {
        List<LogServerEvent> events = new LinkedList<>();
        for (LogStream stream : state.getStreams()) {
            events.add(new LogServerEvent("stream", stream));
        }

        return Observable.from(events);
    }

    public void subscribe(Observer<LogServerEvent> observer) {
        generateWelcomingEvents().mergeWith(state.getObservable()).subscribe(observer);
    }

    public void start() {
        server.start();
    }
}
