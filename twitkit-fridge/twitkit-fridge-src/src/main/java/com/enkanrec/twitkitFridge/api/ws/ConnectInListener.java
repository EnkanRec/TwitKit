/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.ConnectListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Class : ConnectInListener
 * Usage :
 */
@Slf4j
public class ConnectInListener implements ConnectListener {

    @Override
    public void onConnect(SocketIOClient client) {
        log.info(String.format("A client connected to Fridge-Server: %s (%s | %s)",
                client.getSessionId(), client.getRemoteAddress().toString(), client.getTransport().getValue()));
        WSClientPool.add(client.getSessionId().toString(), client);
    }
}
