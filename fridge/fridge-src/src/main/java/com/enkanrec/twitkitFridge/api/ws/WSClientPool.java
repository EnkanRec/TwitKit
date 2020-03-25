/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class : WSClientPool
 * Usage :
 */
@Slf4j
public class WSClientPool {
    private static ConcurrentHashMap<String, SocketIOClient> pool = new ConcurrentHashMap<>();

    public static void add(String clientId, SocketIOClient clientRef) {
        WSClientPool.pool.put(clientId, clientRef);
    }

    public static SocketIOClient get(String clientId) {
        return WSClientPool.pool.get(clientId);
    }

    public static SocketIOClient remove(String clientId) {
        return WSClientPool.pool.remove(clientId);
    }

    public static void clearAndDisconnect() {
        log.info("client pool will be clear soon");
        for (Map.Entry<String, SocketIOClient> cached : WSClientPool.pool.entrySet()) {
            try {
                cached.getValue().disconnect();
                log.info("client is disconnected elegant, " + cached.getKey());
            } catch (Exception de) {
                log.warn("cannot disconnect client elegant, " + cached.getKey());
            }
        }
        WSClientPool.pool.clear();
        log.info("client pool is cleared");
    }
}
