/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Class : BaseFridgeForm
 * Usage : 标准请求格式
 */
@Slf4j
@ToString
@EqualsAndHashCode
public class BaseFridgeForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 从哪个外部服务发起的请求
     */
    @Getter
    @NotNull
    private String forwardFrom;

    /**
     * 请求的客户端时间戳（ISO8601）
     */
    @Getter
    @Setter
    @NotNull
    private String timestamp;

    public void setForwardFrom(String forwardFrom) {
        log.info(String.format("Request form built, forward from: %s", forwardFrom));
        this.forwardFrom = forwardFrom;
    }
}
