/*
 * Author : Rinka
 * Date   : 2020/2/9
 */
package com.enkanrec.twitkitFridge.monitor;

import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.Summary;
import org.springframework.stereotype.Component;

/**
 * Class : WebSocketMonitor
 * Usage : WebSocket指标数据的包装
 */
@Component
public class WebSocketMonitor extends BaseMonitor {

    public final Summary responseTimeInMs = Summary
            .build()
            .name("twitkit_fridge_socketio_response_time_milliseconds")
            .labelNames(BaseMonitor.TAG_HTTP_HANDLER, BaseMonitor.TAG_HTTP_URI, BaseMonitor.TAG_HTTP_STATUS_CODE)
            .help("Websocket Request via socketIO completed time in milliseconds")
            .register();

    public final Counter exceptionCounter = Counter
            .build()
            .name("twitkit_fridge_socketio_exception_count")
            .labelNames(BaseMonitor.TAG_HTTP_METHOD, BaseMonitor.TAG_HTTP_URI)
            .help("Websocket Request via socketIO exception counter")
            .register();

    public final Counter connectInCounter = Counter
            .build()
            .name("twitkit_fridge_socketio_connected_count")
            .help("SocketIO connected in counter")
            .register();

    public final Counter disconnectOutCounter = Counter
            .build()
            .name("twitkit_fridge_socketio_disconnected_count")
            .help("SocketIO disconnected out counter")
            .register();

    public final Gauge activeConnectionCounter = Gauge
            .build()
            .name("twitkit_fridge_socketio_active_connection_count")
            .help("SocketIO active connection counter")
            .register();

}
