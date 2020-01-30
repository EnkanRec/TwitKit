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
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public abstract class BaseFridgeForm implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotNull
    private String forwardFrom;

    @NotNull
    private String timestamp;
}
