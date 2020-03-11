/*
 * Author : Rinka
 * Date   : 2020/2/7
 */
package com.enkanrec.twitkitFridge.api.form;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;

/**
 * Class : NamespaceForm
 * Usage :
 */
@Data
@ToString
@EqualsAndHashCode
public class NamespaceForm {

    /**
     * 任务id
     */
    @NotNull
    private String namespace;
}
