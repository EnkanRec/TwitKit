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
 * Class : TranslateForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public class TranslateForm {

    /**
     * 任务id
     */
    @NotNull
    private Integer tid;

    /**
     * 翻译完毕文本
     */
    @NotNull
    private String trans;

    /**
     * 烤推出图地址
     */
    @NotNull
    private String img;
}
