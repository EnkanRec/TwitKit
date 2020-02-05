/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.api.form.TaskCreationForm;
import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTranslateEntity;

import java.util.List;
import java.util.Map;

/**
 * Class : TaskService
 * Usage :
 */
public interface TaskService {

    TaskServiceImpl.CreateTaskReplay addTask(String url, String content, String media);

    public List<TaskServiceImpl.CreateTaskReplay> addTaskByBulk(List<TaskCreationForm> twitters);

    EnkanTaskEntity getOneLatestOfVisible();

    TaskServiceImpl.TranslatedTask getOneLatestOfVisibleWithTranslation();

    EnkanTaskEntity getOneLatest();

    TaskServiceImpl.TranslatedTask getOneWithTranslation(Integer tid);

    List<TaskServiceImpl.TranslatedTask> getManyFromTidWithTranslation(Integer tid);

    EnkanTaskEntity updateComment(Integer tid, String comment);

    EnkanTaskEntity hide(Integer tid);

    EnkanTaskEntity visible(Integer tid);

    Integer removeAllTranslations(Integer tid);

    EnkanTranslateEntity addTranslation(Integer tid, String translation, String img);
}
