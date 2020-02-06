/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge.api;

import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Class : ExceptionControllerAdvice
 * Usage : 异常处理路由
 */
@Slf4j
@ControllerAdvice
public class ExceptionControllerAdvice {

    /**
     * 捕获全部异常
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    public StandardResponse exceptionHandler(HttpServletRequest request, Exception e) {
        StandardResponse sr = new StandardResponse();
        Map<String, String> hint = new HashMap<>();
        hint.put("msg", e.getMessage());
        hint.put("path", request.getPathInfo());
        hint.put("method", request.getMethod());
        String formatted;
        try {
            formatted = JsonUtil.Mapper.writeValueAsString(hint);
        } catch (JsonProcessingException ex) {
            log.error("cannot json dump exception hint, " + ex.getMessage());
            formatted = "___EXCEPTION_HANDLER_FAULT___";
        }
        log.error(String.format("Rest Exception: %s", formatted));
        sr.setMessage(formatted);
        if (e instanceof org.springframework.web.servlet.NoHandlerFoundException) {
            sr.setCode(StandardResponse.CODE_NOTFOUND);
        } else {
            sr.setCode(StandardResponse.CODE_EXCEPTION);
        }
        return sr;
    }
}
