package org.kluge.logging;

import io.reactivex.netty.channel.ObservableConnection;
import org.kluge.logging.model.*;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

import java.util.Iterator;

/**
 * Created by giko on 10/13/14.
 */
public class LogServerConnectionHandler implements io.reactivex.netty.channel.ConnectionHandler<String, String> {
    LogServerState state;

    public LogServerConnectionHandler(LogServerState state) {
        this.state = state;
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
        state.getConnections().put(connection, node);

        return connection.getInput().flatMap((Func1<String, Observable<Void>>) msg -> {
                    System.out.println(msg);
                    String[] args = msg.replace("\r\n", "").split("\\|");

                    if (args[0].equals("node")) {
                        state.getConnections().get(connection).setName(args[1]);
                    } else if (args[0].equals("log")) {
                        int offset = 0;
                        if (args.length == 5) {
                            offset = 1;
                            state.getConnections().get(connection).setName(args[2]);
                        }

                        LogMessage message = new LogMessage();
                        message.setStream(args[1]);
                        message.setNode(state.getConnections().get(connection));
                        message.setLevel(args[2 + offset]);
                        message.setMessage(args[3 + offset]);

                        LogStream stream = new LogStream(args[1]);
                        if (!state.getStreams().contains(stream)) {
                            stream.addNode(state.getConnections().get(connection));
                            state.getStreams().add(stream);
                            state.getNonCachingEventSubject().onNext(new LogServerEvent("stream", stream));
                        }

                        state.getCachingEventSubject().onNext(new LogServerEvent("log", message));
                    }

                    return Observable.empty();
                })
                .finallyDo(() -> {
                    Iterator<LogStream> logStreamIterator = state.getStreams().iterator();
                    while (logStreamIterator.hasNext()) {
                        LogStream stream = logStreamIterator.next();
                        stream.removeNode(state.getConnections().get(connection));
                        if (stream.isEmpty()) {
                            logStreamIterator.remove();
                            state.getNonCachingEventSubject().onNext(new LogServerEvent("-stream", stream.getName()));
                        }
                    }
                    state.getNonCachingEventSubject().onNext(new LogServerEvent("-node", state.getConnections().get(connection).getName()));
                    state.getConnections().remove(connection);
                });
    }
}
