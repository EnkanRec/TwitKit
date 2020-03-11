/*
 * Author : Rinka
 * Date   : 2020/1/31
 */
package com.enkanrec.twitkitFridge.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Class : InterceptorBean
 * Usage :
 */
@Configuration
public class InterceptorBean implements WebMvcConfigurer {

    @Bean
    public MonitorInterceptor monitorInterceptorBean() {
        return new MonitorInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.monitorInterceptorBean()).addPathPatterns("/api/**");
    }
}
