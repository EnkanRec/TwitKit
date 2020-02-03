/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge.api;

import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Class : ExceptionControllerAdvice
 * Usage : 异常处理路由
 */
@ControllerAdvice
public class ExceptionControllerAdvice {

    @ResponseBody
    @ExceptionHandler(Exception.class)
    public StandardResponse exceptionHandler(HttpServletRequest request, Exception e) {
        StandardResponse sr = new StandardResponse();
        sr.setMessage(e.getMessage());
        if (e instanceof org.springframework.web.servlet.NoHandlerFoundException) {
            sr.setCode(404);
        } else {
            sr.setCode(500);
        }
        return sr;
    }
}
