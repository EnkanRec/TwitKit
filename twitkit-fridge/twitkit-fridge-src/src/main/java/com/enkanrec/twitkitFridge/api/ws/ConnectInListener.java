/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.enkanrec.twitkitFridge.monitor.WebSocketMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class : ConnectInListener
 * Usage :
 */
@Slf4j
@Component
public class ConnectInListener implements ConnectListener {

    @Autowired
    private WebSocketMonitor monitor;

    @Override
    public void onConnect(SocketIOClient client) {
        log.info(String.format("A client connected to Fridge-Server: %s (%s | %s)",
                client.getSessionId(), client.getRemoteAddress().toString(), client.getTransport().getValue()));
        WSClientPool.add(client.getSessionId().toString(), client);
        this.monitor.connectInCounter.inc();
        this.monitor.activeConnectionCounter.inc();
    }
}
