/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;

/**
 * Class : TaskService
 * Usage :
 */
public interface TaskService {

    EnkanTaskEntity getOneLatest();
}
