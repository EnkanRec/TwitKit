/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.enkanrec.twitkitFridge.monitor.WebSocketMonitor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class : DisconnectOutListener
 * Usage :
 */
@Slf4j
@Component
public class DisconnectOutListener implements DisconnectListener {

    @Autowired
    private WebSocketMonitor monitor;

    @Override
    public void onDisconnect(SocketIOClient client) {
        log.info("A participant disconnected from Fridge-Server: " + client.getSessionId());
        WSClientPool.remove(client.getSessionId().toString());
        this.monitor.disconnectOutCounter.inc();
        this.monitor.activeConnectionCounter.dec();
    }
}
