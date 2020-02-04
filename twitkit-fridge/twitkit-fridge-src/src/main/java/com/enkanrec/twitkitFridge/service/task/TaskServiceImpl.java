/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanTaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Class : TaskServiceImpl
 * Usage : 烤推任务的交互逻辑
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private final EnkanTaskRepository repository;

    public TaskServiceImpl(EnkanTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public EnkanTaskEntity getOneLatest() {
        EnkanTaskEntity task = this.repository.findFirstByOrderByTidDesc();
        return task;
    }
}
