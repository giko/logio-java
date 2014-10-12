import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ConnectionHandler;
import io.reactivex.netty.channel.ObservableConnection;
import io.reactivex.netty.pipeline.PipelineConfigurators;
import io.reactivex.netty.server.RxServer;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by giko on 10/12/2014.
 */
public class LogServer {
    private PublishSubject<LogServerEvent> eventPublishSubject;
    RxServer<String, String> server;

    public void subscribe(Observer<LogServerEvent> observer) {
        eventPublishSubject.subscribe(observer);
    }

    public LogServer() {
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

        eventPublishSubject = PublishSubject.create();
        server = RxNetty.createTcpServer(28878, PipelineConfigurators.textOnlyConfigurator(), new ConnectionHandler<String, String>() {
            @Override
            public Observable<Void> handle(final ObservableConnection<String, String> connection) {
                return connection.getInput().flatMap(new Func1<String, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(String msg) {
                        String[] args = msg.replace("\r\n","").split("\\|");
                        eventPublishSubject.onNext(new LogServerEvent(args[0], Arrays.copyOfRange(args, 1, args.length)));
                        return Observable.empty();
                    }
                });
            }
        });
    }

    public void start(){
        server.start();
    }
}
