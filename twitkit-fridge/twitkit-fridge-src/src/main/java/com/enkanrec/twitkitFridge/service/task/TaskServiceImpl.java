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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;

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
            EnkanTranslateEntity translateEntity = this.translateRepository.findFirstByTidOrderByVersionDesc(tid);
            return TranslatedTask.of(taskEntity, translateEntity);
        } else {
            log.warn("try to get task, but tid not mapped any record in DB");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Transactional
    @Override
    public List<TranslatedTask> getManyFromTidWithTranslation(Integer tid) {
        List<EnkanTaskEntity> tasks = this.taskRepository.findAllByTidGreaterThanAndHidedIsFalse(tid);
        if (tasks.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        List<TranslatedTask> result = new ArrayList<>();
        for (EnkanTaskEntity task : tasks) {
            List<EnkanTranslateEntity> versionedTrans = task.getTranslations();
            if (versionedTrans.size() == 0) {
                result.add(TranslatedTask.of(task, null));
            } else {
                versionedTrans.sort((x, y) ->
                        -1 * Integer.compare(x.getVersion(), y.getVersion()));
                EnkanTranslateEntity translated = versionedTrans.get(0);
                result.add(TranslatedTask.of(task, translated));
            }
        }
        return result;
    }

    @Transactional
    @Override
    public EnkanTaskEntity updateComment(Integer tid, String comment) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setComment(comment);
            this.taskRepository.save(existed);
            return existed;
        } else {
            log.warn("try to comment a task, but tid not mapped any record in DB");
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
