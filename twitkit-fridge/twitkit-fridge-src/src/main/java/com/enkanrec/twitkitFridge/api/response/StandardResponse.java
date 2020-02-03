/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class : StandardResponse
 * Usage :
 */
@Data
@ToString
@NoArgsConstructor
@EqualsAndHashCode
public class StandardResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int CODE_SUCCESS = 200;
    public static final int CODE_NOTFOUND = 404;
    public static final int CODE_EXCEPTION = 500;

    public static final String MESSAGE_SUCCESS = "OK";

    private int code;

    private String message;

    private String timestamp = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

    private Object data;

    public StandardResponse(int code, String message) {
        this(code, message, null);
    }

    public StandardResponse(int code, String message, Object payload) {
        this.code = code;
        this.message = message;
        this.data = payload;
    }

    public static StandardResponse ok() {
        return new StandardResponse(CODE_SUCCESS, MESSAGE_SUCCESS);
    }

    public static StandardResponse ok(Object payload) {
        return new StandardResponse(CODE_SUCCESS, MESSAGE_SUCCESS, payload);
    }

    public static StandardResponse exception(String message) {
        return new StandardResponse(CODE_EXCEPTION, message);
    }
}
