/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Class : TidForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public class TidForm {

    /**
     * 任务id
     */
    @NotNull
    private Integer tid;
}
