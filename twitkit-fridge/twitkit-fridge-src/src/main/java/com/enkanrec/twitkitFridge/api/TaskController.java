/*
 * Author : Rinka
 * Date   : 2020/2/2
 */
package com.enkanrec.twitkitFridge.api;

import com.enkanrec.twitkitFridge.api.form.TidForm;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * Class : TaskController
 * Usage : 推文翻译任务的对外接口
 */
@RestController
@RequestMapping("/api/db/task")
public class TaskController {

    /**
     * 获取单条推文
     */
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public StandardResponse getTask(@Valid TidForm form) {
        return StandardResponse.ok();
    }

    /**
     * 获取给定tid（不包含）之后的所有推文
     */
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public StandardResponse listTask(@Valid TidForm form) {
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/getlast", method = RequestMethod.POST)
    public StandardResponse getLastTask() {
        // TODO: 获取最后一条推文
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public StandardResponse commentTask() {
        // TODO: 更新comment
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/hide", method = RequestMethod.POST)
    public StandardResponse hideTask() {
        // TODO: 隐藏推文
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/visible", method = RequestMethod.POST)
    public StandardResponse visibleTask() {
        // TODO: 恢复隐藏推文
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/translate", method = RequestMethod.POST)
    public StandardResponse translateTask() {
        // TODO: 翻译
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/rollback", method = RequestMethod.POST)
    public StandardResponse rollbackTask() {
        // TODO: 翻译回滚
        return StandardResponse.ok();
    }

    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public StandardResponse resetTask() {
        // TODO: 恢复未翻译
        return StandardResponse.ok();
    }
}
