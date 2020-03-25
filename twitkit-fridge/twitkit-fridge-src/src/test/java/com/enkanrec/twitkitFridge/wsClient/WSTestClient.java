/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.wsClient;

import com.enkanrec.twitkitFridge.api.ws.FridgeWSServer;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.Polling;
import io.socket.engineio.client.transports.WebSocket;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Class : WSTestClient
 * Usage :
 */
@Slf4j
public class WSTestClient {

    public static void main(String[] args) throws Exception {
        WSTestClient wsc = new WSTestClient();
        wsc.integrations();
    }

    private Socket webSocket;

    private void integrations() throws Exception {
        IO.Options options = new IO.Options();
        options.forceNew = true;
        options.reconnection = true;
        options.transports = new String[]{WebSocket.NAME, Polling.NAME};
        webSocket = IO.socket("http://localhost:10103", options);

        webSocket.on(Socket.EVENT_CONNECT, args -> log.info("connected in with her handshake: " + Arrays.toString(args)))
                .on(Socket.EVENT_DISCONNECT, args -> log.warn("disconnected in with her handshake: " + Arrays.toString(args)))
                .on(Socket.EVENT_RECONNECTING, args -> log.warn("reconnecting: " + Arrays.toString(args)))
                .on(Socket.EVENT_RECONNECT, args -> log.warn("reconnected: " + Arrays.toString(args)))
                .on(FridgeWSServer.RESPONSE_EVT, args -> {
                    log.info(Arrays.toString(args));
                });

        webSocket.connect();

        while (!webSocket.connected()) {
            Thread.sleep(1000);
            System.out.println("waiting: " + webSocket.connected());
        }

        Map<String, Object> getPayload = new HashMap<>();
        getPayload.put("tid", 1001);
        webSocket.emit(FridgeWSServer.REQUEST_EVT, buildPayload("task", "get", getPayload));

        while (true) {
            Thread.sleep(10000);
            System.out.println("beat: " + webSocket.connected());
        }
    }

    private String buildPayload(String of, String command, Object data) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("of", of);
        payload.put("command", command);
        payload.put("forwardFrom", "tester.socketIO");
        payload.put("timestamp", ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        payload.put("data", data);
        return JsonUtil.dumps(payload);
    }
}
