/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Class : BaseFridgeForm
 * Usage : 标准请求格式
 */
@Data
@ToString
@EqualsAndHashCode
public class BaseFridgeForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 从哪个外部服务发起的请求
     */
    @NotNull
    private String forwardFrom;

    /**
     * 请求的客户端时间戳（ISO 8601）
     */
    @NotNull
    private String timestamp;
}
