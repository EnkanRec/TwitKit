/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.api.form.TaskCreationForm;
import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTaskEntity;

import java.util.List;

/**
 * Class : TaskService
 * Usage :
 */
public interface TaskService {

    TaskServiceImpl.CreateTaskReplay addTask(TaskCreationForm twitter);

    List<TaskServiceImpl.CreateTaskReplay> addTaskByBulk(List<TaskCreationForm> twitters);

    List<TaskServiceImpl.CreateTaskReplay> addTaskByBulkWithCache(List<TaskCreationForm> twitters);

    Boolean removeTask(Integer tid);

    TaskServiceImpl.TaskReplay getOneLatestOfVisible();

    TaskServiceImpl.TranslatedTask getOneLatestOfVisibleWithTranslation();

    TaskServiceImpl.TaskReplay getOneLatest();

    TaskServiceImpl.TranslatedTask getOneWithTranslation(Integer tid);

    List<TaskServiceImpl.TranslatedTask> getManyFromTidWithTranslation(Integer tid);

    TaskServiceImpl.TaskReplay updateComment(Integer tid, String comment);

    TaskServiceImpl.TaskReplay hide(Integer tid);

    TaskServiceImpl.TaskReplay visible(Integer tid);

    TaskServiceImpl.TaskReplay setPublished(Integer tid);

    TaskServiceImpl.TaskReplay setUnpublished(Integer tid);

    Integer removeAllTranslations(Integer tid);

    TaskServiceImpl.TranslatedTask addTranslation(Integer tid, String translation, String img);

    TaskServiceImpl.TranslatedTask rollbackTranslation(Integer tid);

    TaskServiceImpl.VersionedTranslatedTask getAllTranslation(Integer tid);
}
