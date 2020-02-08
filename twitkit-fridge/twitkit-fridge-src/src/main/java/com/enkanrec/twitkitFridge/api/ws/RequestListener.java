/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.enkanrec.twitkitFridge.api.form.JsonDataFridgeForm;
import lombok.extern.slf4j.Slf4j;

/**
 * Class : RequestListener
 * Usage :
 */
@Slf4j
public class RequestListener implements DataListener<JsonDataFridgeForm> {

    @Override
    public void onData(SocketIOClient client, JsonDataFridgeForm data, AckRequest ackSender) throws Exception {

    }
}
