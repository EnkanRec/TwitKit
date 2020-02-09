/*
 * Author : Rinka
 * Date   : 2020/1/31
 */
package com.enkanrec.twitkitFridge.monitor;

import io.prometheus.client.Counter;
import io.prometheus.client.Summary;
import org.springframework.stereotype.Component;

/**
 * Class : InterceptorMonitor
 * Usage : HTTP拦截器指标数据的包装
 */
@Component
public class InterceptorMonitor extends BaseMonitor {

    public final Summary responseTimeInMs = Summary
            .build()
            .name("twitkit_fridge_http_response_time_milliseconds")
            .labelNames(BaseMonitor.TAG_HTTP_METHOD, BaseMonitor.TAG_HTTP_HANDLER,
                    BaseMonitor.TAG_HTTP_URI, BaseMonitor.TAG_HTTP_STATUS_CODE)
            .help("HTTP Request completed time in milliseconds")
            .register();

    public final Counter exceptionCounter = Counter
            .build()
            .name("twitkit_fridge_http_exception_count")
            .labelNames(BaseMonitor.TAG_HTTP_METHOD, BaseMonitor.TAG_HTTP_URI)
            .help("HTTP Request exception counter")
            .register();
}
