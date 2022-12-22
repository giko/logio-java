package org.kluge.logging;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import org.kluge.logging.model.LogServerEvent;
import rx.Observer;

/**
 * Created by giko on 10/12/2014.
 */
public class Main {
    public static void main(String[] args) {
        Configuration config = new Configuration();
        config.setHostname("0.0.0.0");
        config.setPort(9192);

        final SocketIOServer server = new SocketIOServer(config);

        final LogServer logServer = new LogServer();

        server.addConnectListener(client -> logServer.subscribe(new Observer<>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable throwable) {
            }

            @Override
            public void onNext(LogServerEvent logServerEvent) {
                client.sendEvent(logServerEvent.type(), logServerEvent.object());
            }
        }));
        logServer.start();
        server.start();

        try {
            server.startAsync().sync();
        } catch (InterruptedException e) {
            throw new RuntimeException("An error occurred!", e);
        }
    }
}
