/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTranslateEntity;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanTaskRepository;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanTranslateRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Class : TaskServiceImpl
 * Usage : 烤推任务的交互逻辑
 */
@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private final EnkanTaskRepository taskRepository;

    private final EnkanTranslateRepository translateRepository;

    public TaskServiceImpl(EnkanTaskRepository taskRepository,
                           EnkanTranslateRepository translateRepository) {
        this.taskRepository = taskRepository;
        this.translateRepository = translateRepository;
    }

    @Transactional
    @Override
    public EnkanTaskEntity getOneLatest() {
        EnkanTaskEntity task = this.taskRepository.findFirstByOrderByTidDesc();
        return task;
    }

    @Transactional
    @Override
    public TranslatedTask getOneWithTranslation(Integer tid) {
        Optional<EnkanTaskEntity> task = this.taskRepository.findById(tid);
        if (task.isPresent()) {
            EnkanTaskEntity taskEntity = task.get();
            EnkanTranslateEntity translateEntity = this.translateRepository.getFirstByTidOrderByVersionDesc(tid);
            return TranslatedTask.of(taskEntity, translateEntity);
        } else {
            log.warn("try to get task by tid but return null from DB");
            return null;
        }
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class TranslatedTask {

        private EnkanTaskEntity twitter;

        private EnkanTranslateEntity translation;
    }
}
