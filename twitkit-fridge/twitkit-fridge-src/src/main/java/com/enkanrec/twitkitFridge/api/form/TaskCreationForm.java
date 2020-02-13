/*
 * Author : Rinka
 * Date   : 2020/2/5
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Class : TaskCreationForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public class TaskCreationForm {

    @NotNull
    private String url;

    @NotNull
    private String content;

    @NotNull
    private String media;
}
