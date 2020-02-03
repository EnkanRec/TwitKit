/*
 * Author : Rinka
 * Date   : 2020/2/3
 */
package com.enkanrec.twitkitFridge.api.form;

import javax.validation.constraints.NotNull;

/**
 * Class : TranslateForm
 * Usage :
 */
public class TranslateForm extends JsonDataFridgeForm {

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
