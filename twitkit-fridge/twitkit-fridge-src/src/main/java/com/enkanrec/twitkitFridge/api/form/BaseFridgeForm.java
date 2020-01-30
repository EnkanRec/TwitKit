/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * Class : BaseFridgeForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public abstract class BaseFridgeForm implements Serializable {
    private static final long serialVersionUID = 1L;

    private String forwardFrom;

    private String timestamp;
}
