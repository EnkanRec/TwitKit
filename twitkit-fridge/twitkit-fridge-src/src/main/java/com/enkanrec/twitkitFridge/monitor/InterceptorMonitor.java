/*
 * Author : Rinka
 * Date   : 2020/1/31
 */
package com.enkanrec.twitkitFridge.monitor;

import io.prometheus.client.Summary;
import org.springframework.stereotype.Component;

/**
 * Class : InterceptorMonitor
 * Usage : 拦截器指标数据的包装
 */
@Component
public class InterceptorMonitor extends BaseMonitor {

    public final Summary responseTimeInMs = Summary
            .build()
            .name("twitkit_fridge_http_response_time_milliseconds")
            .labelNames(BaseMonitor.TAG_HTTP_METHOD, BaseMonitor.TAG_HTTP_HANDLER,
                    BaseMonitor.TAG_HTTP_URI, BaseMonitor.TAG_HTTP_STATUS_CODE)
            .help("Request completed time in milliseconds")
            .register();
}
