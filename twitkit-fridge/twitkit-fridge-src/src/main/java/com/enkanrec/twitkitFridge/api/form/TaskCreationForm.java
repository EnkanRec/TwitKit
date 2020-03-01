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
    private String url;

    @NotNull
    private String content;

    @NotNull
    private String media;

    @NotNull
    private String pub_date;

    @NotNull
    private String status_id;

    private Integer ref = null;

    private String extra = null;

    @NotNull
    private String user_twitter_uid;

    @NotNull
    private String user_name;

    @NotNull
    private String user_display;

    @NotNull
    private String user_avatar;
}
