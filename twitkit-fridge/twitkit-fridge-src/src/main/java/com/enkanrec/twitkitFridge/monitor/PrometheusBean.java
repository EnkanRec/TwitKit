/*
 * Author : Rinka
 * Date   : 2020/1/31
 */
package com.enkanrec.twitkitFridge.monitor;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Class : PrometheusBean
 * Usage : 注册Prometheus监控Bean
 */
@Configuration
public class PrometheusBean {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Bean
    public ServletRegistrationBean servletRegistrationBean(){
        DefaultExports.initialize();
        return new ServletRegistrationBean(new MetricsServlet(), "/metrics");
    }
}
