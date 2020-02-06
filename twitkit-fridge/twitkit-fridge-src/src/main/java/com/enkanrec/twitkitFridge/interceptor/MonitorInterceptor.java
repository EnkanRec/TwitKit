/*
 * Author : Rinka
 * Date   : 2020/1/9
 * Contact: gzlinjia@corp.netease.com
 */
package com.enkanrec.twitkitFridge.interceptor;

import com.enkanrec.twitkitFridge.monitor.InterceptorMonitor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Class : MonitorInterceptor
 * Usage : 监控拦截器，在请求进入时注入监控信息，并在结束时更新统计指标
 */
@Slf4j
public class MonitorInterceptor implements HandlerInterceptor {

    private static final String REQ_PARAM_TIMING = "__inject_cost_timing";
    private static final String REQ_REQUEST_ID = "__inject_request_id";

    private static final String LOG_KEY_REQUEST_ID = "requestId";

    @Autowired
    private InterceptorMonitor monitor;

    /**
     * 所有`/api/*`的请求在这里带上她的开始时间戳
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        request.setAttribute(REQ_PARAM_TIMING, System.currentTimeMillis());
        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQ_REQUEST_ID, requestId);
        MDC.put(LOG_KEY_REQUEST_ID, requestId);
        log.info(String.format("Request id generated: %s -> path: %s", requestId, request.getRequestURI()));
        return true;
    }

    /**
     * 所有`/api/*`的请求在结束后记录一下访问情况指标
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        Long timingAttr = (Long) request.getAttribute(REQ_PARAM_TIMING);
        long completedTime = System.currentTimeMillis() - timingAttr;
        String handlerLabel = handler.toString();
        if (handler instanceof HandlerMethod) {
            Method method = ((HandlerMethod) handler).getMethod();
            handlerLabel = method.getDeclaringClass().getSimpleName() + "." + method.getName();
        }
        this.monitor.responseTimeInMs.labels(request.getMethod(), handlerLabel, request.getRequestURI(), Integer.toString(response.getStatus()))
                .observe(completedTime);
        String requestId = MDC.get(LOG_KEY_REQUEST_ID);
        MDC.remove(LOG_KEY_REQUEST_ID);
        log.debug("Request id is removed: " + requestId);
    }
}
