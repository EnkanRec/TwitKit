/*
 * Author : Rinka
 * Date   : 2020/2/2
 */
package com.enkanrec.twitkitFridge.api.rest;

import com.enkanrec.twitkitFridge.api.form.*;
import com.enkanrec.twitkitFridge.api.response.AffectedCountResponse;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.service.task.TaskService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
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
     * 入库一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public StandardResponse createTask(@Valid @RequestBody BaseJsonWarp<TaskCreationForm> form) {
        TaskCreationForm data = form.getData();
        return StandardResponse.ok(this.service.addTask(data.getUrl(), data.getContent(), data.getMedia()));
    }

    /**
     * 入库一批推文
     */
    @ResponseBody
    @RequestMapping(value = "/bulk", method = RequestMethod.POST)
    public StandardResponse bulkTask(@Valid @RequestBody BaseJsonWarp<List<TaskCreationForm>> form) {
        List<TaskCreationForm> taskCreationForms = form.getData();
        return StandardResponse.ok(this.service.addTaskByBulk(taskCreationForms));
    }

    /**
     * 带有缓存判定地入库一批推文
     */
    @ResponseBody
    @RequestMapping(value = "/cachebulk", method = RequestMethod.POST)
    public StandardResponse cachebulkTask(@Valid @RequestBody BaseJsonWarp<List<TaskCreationForm>> form) {
        List<TaskCreationForm> taskCreationForms = form.getData();
        return StandardResponse.ok(this.service.addTaskByBulkWithCache(taskCreationForms));
    }

    /**
     * 删除一条推文任务和她的所有翻译，这个操作不能回滚
     */
    @ResponseBody
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public StandardResponse removeTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.removeTask(form.getData().getTid()));
    }

    /**
     * 获取单条推文
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public StandardResponse getTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.getOneWithTranslation(form.getData().getTid()));
    }

    /**
     * 获取给定tid（不包含）之后的所有推文
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    public StandardResponse listTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.getManyFromTidWithTranslation(form.getData().getTid()));
    }

    /**
     * 忽略被隐藏的推文后，获取最后一条推文及其最新的翻译
     */
    @ResponseBody
    @RequestMapping(value = "/last", method = RequestMethod.POST)
    public StandardResponse getLastTask(@Valid @RequestBody BaseJsonWarp<Map<String, Object>> form) {
        Map<String, Object> param = form.getData();
        if (param != null && (Boolean) param.getOrDefault("withTranslation", false)) {
            return StandardResponse.ok(this.service.getOneLatestOfVisibleWithTranslation());
        } else {
            return StandardResponse.ok(this.service.getOneLatestOfVisible());
        }
    }

    /**
     * 获取tid最大的一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/actuallast", method = RequestMethod.POST)
    public StandardResponse getActualLastTask(@Valid @RequestBody BaseJsonWarp<Object> form) {
        return StandardResponse.ok(this.service.getOneLatest());
    }

    /**
     * 更新comment
     */
    @ResponseBody
    @RequestMapping(value = "/comment", method = RequestMethod.POST)
    public StandardResponse commentTask(@Valid @RequestBody BaseJsonWarp<CommentForm> form) {
        CommentForm data = form.getData();
        return StandardResponse.ok(this.service.updateComment(data.getTid(), data.getComment()));
    }

    /**
     * 隐藏一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/hide", method = RequestMethod.POST)
    public StandardResponse hideTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.hide(form.getData().getTid()));
    }

    /**
     * 撤销隐藏一条推文
     */
    @ResponseBody
    @RequestMapping(value = "/visible", method = RequestMethod.POST)
    public StandardResponse visibleTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.visible(form.getData().getTid()));
    }

    /**
     * 设置推文任务为已经发布
     */
    @ResponseBody
    @RequestMapping(value = "/published", method = RequestMethod.POST)
    public StandardResponse publishTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.setPublished(form.getData().getTid()));
    }

    /**
     * 设置推文任务为未发布
     */
    @ResponseBody
    @RequestMapping(value = "/unpublished", method = RequestMethod.POST)
    public StandardResponse unpublishedTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.setUnpublished(form.getData().getTid()));
    }

    /**
     * 进行一次翻译
     */
    @ResponseBody
    @RequestMapping(value = "/translate", method = RequestMethod.POST)
    public StandardResponse translateTask(@Valid @RequestBody BaseJsonWarp<TranslateForm> form) {
        TranslateForm data = form.getData();
        return StandardResponse.ok(this.service.addTranslation(data.getTid(), data.getTrans(), data.getImg()));
    }

    /**
     * 获取一个推文的全部翻译版本
     */
    @ResponseBody
    @RequestMapping(value = "/translations", method = RequestMethod.POST)
    public StandardResponse getAllTranslationsForTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.getAllTranslation(form.getData().getTid()));
    }

    /**
     * 将翻译向前回滚一个版本
     */
    @ResponseBody
    @RequestMapping(value = "/rollback", method = RequestMethod.POST)
    public StandardResponse rollbackTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return StandardResponse.ok(this.service.rollbackTranslation(form.getData().getTid()));
    }

    /**
     * 恢复到未翻译状态
     */
    @ResponseBody
    @RequestMapping(value = "/reset", method = RequestMethod.POST)
    public StandardResponse resetTask(@Valid @RequestBody BaseJsonWarp<TidForm> form) {
        return AffectedCountResponse.of(this.service.removeAllTranslations(form.getData().getTid()));
    }
}
