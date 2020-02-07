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
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class NamespaceForm extends JsonDataFridgeForm {

    /**
     * 任务id
     */
    @NotNull
    private String namespace;
}
