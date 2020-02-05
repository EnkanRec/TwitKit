/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;

import java.util.List;

/**
 * Class : TaskService
 * Usage :
 */
public interface TaskService {

    EnkanTaskEntity getOneLatestWithVisible();

    EnkanTaskEntity getOneLatest();

    TaskServiceImpl.TranslatedTask getOneWithTranslation(Integer tid);

    List<TaskServiceImpl.TranslatedTask> getManyFromTidWithTranslation(Integer tid);

    EnkanTaskEntity updateComment(Integer tid, String comment);

    EnkanTaskEntity hide(Integer tid);

    EnkanTaskEntity visible(Integer tid);

    Integer removeAllTranslations(Integer tid);
}
