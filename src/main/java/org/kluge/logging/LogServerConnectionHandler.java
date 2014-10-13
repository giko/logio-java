package org.kluge.logging;

import io.reactivex.netty.channel.ObservableConnection;
import org.kluge.logging.model.LogMessage;
import org.kluge.logging.model.LogNode;
import org.kluge.logging.model.LogServerEvent;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by giko on 10/13/14.
 */
public class LogServerConnectionHandler implements io.reactivex.netty.channel.ConnectionHandler<String, String> {
    private PublishSubject<LogServerEvent> eventPublishSubject;
    private Map<ObservableConnection<String, String>, LogNode> connections;


    public LogServerConnectionHandler(PublishSubject<LogServerEvent> eventPublishSubject, Map<ObservableConnection<String, String>, LogNode> connections) {
        this.eventPublishSubject = eventPublishSubject;
        this.connections = connections;
    }

    @Override
    public Observable<Void> handle(final ObservableConnection<String, String> connection) {
//        Send a log message
//
//        +log|my_stream|my_node|info|this is log message\r\n
//        Register a new node
//
//                +node|my_node\r\n
//        Register a new node, with stream associations
//
//        +node|my_node|my_stream1,my_stream2\r\n
//        Remove a node
//
//                -node|my_node\r\n

        LogNode node = new LogNode();
        node.setAddress(connection.getChannel().remoteAddress().toString());
        connections.put(connection, node);
        
        return connection.getInput().flatMap(new Func1<String, Observable<Void>>() {
            @Override
            public Observable<Void> call(String msg) {
                String[] args = msg.replace("\r\n", "").split("\\|");
                eventPublishSubject.onNext(new LogServerEvent(args[0], Arrays.copyOfRange(args, 1, args.length)));

                if (args[0].equals("node")) {
                    connections.get(connection).setName(args[1]);
                } else if (args[0].equals("log")) {
                    int offset = 0;
                    if (args.length == 5) {
                        offset = 1;
                        connections.get(connection).setName(args[2]);
                    }
                    
                    LogMessage message = new LogMessage();
                    message.setStream(args[1]);
                    message.setNode(connections.get(connection));
                    message.setLevel(args[2+offset]);
                    message.setMessage(args[3+offset]);
                    eventPublishSubject.onNext(new LogServerEvent(args[0], message));
                }

                return Observable.empty();
            }
        })
                .finallyDo(new Action0() {
                    @Override
                    public void call() {
                        connections.remove(connection);
                    }
                });
    }
}
