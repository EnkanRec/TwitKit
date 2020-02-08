/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DisconnectListener;
import lombok.extern.slf4j.Slf4j;

/**
 * Class : DisconnectOutListener
 * Usage :
 */
@Slf4j
public class DisconnectOutListener implements DisconnectListener {

    @Override
    public void onDisconnect(SocketIOClient client) {
        log.info("A participant disconnected from Seele-Server: " + client.getSessionId());
    }
}
