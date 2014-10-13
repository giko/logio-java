package org.kluge.logging;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.kluge.logging.model.LogServerEvent;
import rx.Observer;

/**
 * Created by giko on 10/12/2014.
 */
public class Main {
    public static void main(String[] args){
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9191);

        final SocketIOServer server = new SocketIOServer(config);

        LogServer logServer = new LogServer();
        logServer.subscribe(new Observer<LogServerEvent>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onNext(LogServerEvent logServerEvent) {
                System.out.println(logServerEvent.getType());
                server.getBroadcastOperations().sendEvent(logServerEvent.getType(), logServerEvent.getObject());
            }
        });

        logServer.start();
        server.start();

        while (true) {
        }
    }
}
