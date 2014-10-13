package org.kluge.logging;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import org.kluge.logging.model.LogServerEvent;
import rx.Observer;

/**
 * Created by giko on 10/12/2014.
 */
public class Main {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setHostname("localhost");
        config.setPort(9191);

        final SocketIOServer server = new SocketIOServer(config);

        final LogServer logServer = new LogServer();

        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(final SocketIOClient client) {
                logServer.subscribe(new Observer<LogServerEvent>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable throwable) {
                    }

                    @Override
                    public void onNext(LogServerEvent logServerEvent) {
                        client.sendEvent(logServerEvent.getType(), logServerEvent.getObject());
                    }
                });
            }
        });
        logServer.start();
        server.start();

        while (true) {
        }
    }
}
