/*
 * Author : Rinka
 * Date   : 2020/2/8
 */
package com.enkanrec.twitkitFridge.api.ws;

import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOServer;
import com.enkanrec.twitkitFridge.GDP;
import com.enkanrec.twitkitFridge.api.form.JsonDataFridgeForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * Class : FridgeWSServer
 * Usage :
 */
@Slf4j
@Component
public class FridgeWSServer {
    public static final String EVENT_REQUEST = "request";

    @Value("${server.port}")
    private Integer listenPort;

    private SocketIOServer server;

    @PostConstruct
    public void init() {
        if (GDP.EnableWebSocket) {
            log.info("`EnableWebSocket` is true, providing service by REST and SocketIO");
            Configuration config = new Configuration();
            config.setPort(this.listenPort);
            config.setHostname("localhost");
            this.server = new SocketIOServer(config);
            this.server.addConnectListener(new ConnectInListener());
            this.server.addDisconnectListener(new DisconnectOutListener());
            this.server.addEventListener(EVENT_REQUEST, JsonDataFridgeForm.class, new RequestListener());
            this.server.start();
            log.info("SocketIO server is started");
        } else {
            log.info("`EnableWebSocket` is false, providing service by REST only");
        }
    }

    @PreDestroy
    public void disposing() {
        log.info("WS server is disposing");
        WSClientPool.clearAndDisconnect();
    }
}
