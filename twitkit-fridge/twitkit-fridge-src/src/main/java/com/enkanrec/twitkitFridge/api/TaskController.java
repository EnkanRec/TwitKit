/*
 * Author : Rinka
 * Date   : 2020/2/2
 */
package com.enkanrec.twitkitFridge.api;

import com.enkanrec.twitkitFridge.api.form.BaseFridgeForm;
import com.enkanrec.twitkitFridge.api.form.CommentForm;
import com.enkanrec.twitkitFridge.api.form.TidForm;
import com.enkanrec.twitkitFridge.api.form.TranslateForm;
import com.enkanrec.twitkitFridge.api.response.AffectedCountResponse;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.service.task.TaskService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * Class : TaskController
 * Usage : 推文翻译任务的对外接口
 */
@RestController
@RequestMapping("/api/db/task")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    /**
     * 获取单条推文
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public StandardResponse getTask(@Valid TidForm form) {
        return StandardResponse.ok(this.service.getOneWithTranslation(form.getTid()));
    }

    /**
     * 获取给定tid（不包含）之后的所有推文
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public StandardResponse listTask(@Valid TidForm form) {
        return StandardResponse.ok(this.service.getManyFromTidWithTranslation(form.getTid()));
    }

    /**
     * 忽略被隐藏的推文后，获取最后一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/last", method = RequestMethod.POST)
    public StandardResponse getLastTask(@Valid BaseFridgeForm form) {
        return StandardResponse.ok(this.service.getOneLatestWithVisible());
    }

    /**
     * 获取tid最大的一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/actuallast", method = RequestMethod.POST)
    public StandardResponse getActualLastTask(@Valid BaseFridgeForm form) {
        return StandardResponse.ok(this.service.getOneLatest());
    }

    /**
     * 更新comment
     */
    @ResponseBody
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public StandardResponse commentTask(@Valid CommentForm form) {
        return StandardResponse.ok(this.service.updateComment(form.getTid(), form.getComment()));
    }

    /**
     * 隐藏一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/hide", method = RequestMethod.POST)
    public StandardResponse hideTask(@Valid TidForm form) {
        return StandardResponse.ok(this.service.hide(form.getTid()));
    }

    /**
     * 撤销隐藏一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/visible", method = RequestMethod.POST)
    public StandardResponse visibleTask(@Valid TidForm form) {
        return StandardResponse.ok(this.service.visible(form.getTid()));
    }

    /**
     * 进行一次翻译
     */
    @ResponseBody
    @RequestMapping(value = "/translate", method = RequestMethod.POST)
    public StandardResponse translateTask(@Valid TranslateForm form) {
        return StandardResponse.ok(this.service.addTranslation(form.getTid(), form.getTrans(), form.getImg()));
    }

    /**
     * 将翻译向前回滚一个版本
     */
    @ResponseBody
    @RequestMapping(value = "/rollback", method = RequestMethod.POST)
    public StandardResponse rollbackTask(@Valid TidForm form) {
        return StandardResponse.ok();
    }

    /**
     * 恢复到未翻译状态
     */
    @ResponseBody
    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public StandardResponse resetTask(@Valid TidForm form) {
        return AffectedCountResponse.of(this.service.removeAllTranslations(form.getTid()));
    }
}
