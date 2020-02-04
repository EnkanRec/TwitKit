/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;

import java.util.List;
import java.util.Map;

/**
 * Class : TaskService
 * Usage :
 */
public interface TaskService {

    EnkanTaskEntity getOneLatest();

    TaskServiceImpl.TranslatedTask getOneWithTranslation(Integer tid);

    List<TaskServiceImpl.TranslatedTask> getManyFromTidWithTranslation(Integer tid);

    EnkanTaskEntity updateComment(Integer tid, String comment);
}
