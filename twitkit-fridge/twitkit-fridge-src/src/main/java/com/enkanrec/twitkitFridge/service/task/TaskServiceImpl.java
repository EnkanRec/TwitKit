/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.api.form.TaskCreationForm;
import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTranslateEntity;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanTaskRepository;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanTranslateRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
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

    @PersistenceContext(unitName = "entityManagerFactoryNoel")
    private EntityManager entityManager;

    public TaskServiceImpl(EnkanTaskRepository taskRepository,
                           EnkanTranslateRepository translateRepository) {
        this.taskRepository = taskRepository;
        this.translateRepository = translateRepository;
    }

    @Transactional
    @Override
    public CreateTaskReplay addTask(String url, String content, String media) {
        log.info(String.format("Begin add twitter [%s], size: %s", url, content.length()));
        Query insertQuery = this.entityManager.createNativeQuery("INSERT IGNORE INTO enkan_task (url, content, media) VALUES (:url, :content, :media)");
        insertQuery.setParameter("url", url);
        insertQuery.setParameter("content", content);
        insertQuery.setParameter("media", media);
        int affected = insertQuery.executeUpdate();
        log.info(String.format("Pre-commit twitter [%s]", url));
        EnkanTaskEntity task = this.taskRepository.findByUrl(url);
        return CreateTaskReplay.of(task, affected == 0);
    }

    @Transactional
    @Override
    public List<CreateTaskReplay> addTaskByBulk(List<TaskCreationForm> twitters) {
        log.info(String.format("Begin bulk twitter, size: %s", twitters.size()));
        List<CreateTaskReplay> result = new ArrayList<>();
        if (twitters.size() == 0) {
            return result;
        }

        // 测试存在性，但这里会有幻读的问题，会影响返回“已存在性”字段的准确性，但入库只会有唯一一条记录
        // 比如两个bulk请求同时提交了同一个URL的推文，可能都返回`alreadyExist`为`false`
        // 使用串行隔离级别可以解决这个问题，但有性能开销，不是很必要？
        Set<String> urlSet = new HashSet<>();
        Map<String, Boolean> existMap = new HashMap<>();
        for (TaskCreationForm twitter : twitters) {
            String url = twitter.getUrl();
            existMap.put(url, this.taskRepository.existsByUrl(url));
            urlSet.add(url);
        }
        if (urlSet.size() != twitters.size()) {
            log.warn(String.format("Url distinct set size skewed, set size: %s, twi size: %s",
                    urlSet.size(), twitters.size()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT IGNORE INTO enkan_task (url, content, media) VALUES ");
        twitters.forEach(t -> sb.append("(?,?,?),"));
        String sqlTemplate = sb.toString().substring(0, sb.length() - 1);
        log.info(String.format("Bulk sql length is: %s", sqlTemplate.length()));
        Query insertQuery = this.entityManager.createNativeQuery(sqlTemplate);
        int paramPointer = 1;
        for (TaskCreationForm twitter : twitters) {
            insertQuery.setParameter(paramPointer++, twitter.getUrl());
            insertQuery.setParameter(paramPointer++, twitter.getContent());
            insertQuery.setParameter(paramPointer++, twitter.getMedia());
        }
        int affected = insertQuery.executeUpdate();
        log.info("Pre-commit bulk with affected count: " + affected);
        List<EnkanTaskEntity> tasks = this.taskRepository.findByUrlIn(urlSet);

        for (EnkanTaskEntity task : tasks) {
            result.add(CreateTaskReplay.of(task, existMap.getOrDefault(task.getUrl(), false)));
        }
        return result;
    }

    @Transactional
    @Override
    public Boolean removeTask(Integer tid) {
        log.warn("try to delete task by tid: " + tid);
        Optional<EnkanTaskEntity> et = this.taskRepository.findById(tid);
        boolean flag = false;
        if (et.isPresent()) {
            EnkanTaskEntity task = et.get();
            this.taskRepository.delete(task);
            flag = true;
        } else {
            log.warn("Delete task by tid, but nothing affected");
        }
        return flag;
    }

    @Transactional
    @Override
    public EnkanTaskEntity getOneLatestOfVisible() {
        EnkanTaskEntity et = this.taskRepository.findFirstByHidedIsFalseOrderByTidDesc();
        if (et == null) {
            log.warn("Retrieve last visible task, but return null");
        } else {
            log.info(String.format("Retrieve last visible task, response tid: %s with updatetime: %s",
                    et.getTid(), et.getUpdatetime()));
        }
        return et;
    }

    @Transactional
    @Override
    public TranslatedTask getOneLatestOfVisibleWithTranslation() {
        EnkanTaskEntity entity = this.taskRepository.findFirstByHidedIsFalseOrderByTidDesc();
        if (entity != null) {
            List<EnkanTranslateEntity> translations = entity.getTranslations();
            if (translations.isEmpty()) {
                return TranslatedTask.of(entity, null);
            }
            EnkanTranslateEntity lastTrans = translations.get(translations.size() - 1);
            return TranslatedTask.of(entity, lastTrans);
        }
        return TranslatedTask.of(null, null);
    }

    @Transactional
    @Override
    public EnkanTaskEntity getOneLatest() {
        EnkanTaskEntity et = this.taskRepository.findFirstByOrderByTidDesc();
        if (et == null) {
            log.warn("Retrieve actual last task with largest tid, but return null");
        } else {
            log.info(String.format("Retrieve actual last task with largest tid, response tid: %s with updatetime: %s",
                    et.getTid(), et.getUpdatetime()));
        }
        return et;
    }

    @Transactional
    @Override
    public TranslatedTask getOneWithTranslation(Integer tid) {
        Optional<EnkanTaskEntity> task = this.taskRepository.findById(tid);
        if (task.isPresent()) {
            EnkanTaskEntity taskEntity = task.get();
            EnkanTranslateEntity translateEntity = this.translateRepository.findFirstByTaskOrderByVersionDesc(taskEntity);
            return TranslatedTask.of(taskEntity, translateEntity);
        } else {
            log.warn("try to get task, but tid not mapped any record in DB: " + tid.toString());
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
        log.info(String.format("Update comment for twitter [%s] size: %s", tid, comment.length()));
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setComment(comment);
            this.taskRepository.save(existed);
            return existed;
        } else {
            log.warn("try to comment a task, but tid not mapped any record in DB: " + tid.toString());
            return null;
        }
    }

    @Transactional
    @Override
    public EnkanTaskEntity hide(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setHided(true);
            this.taskRepository.save(existed);
            return existed;
        } else {
            log.warn("try to hide a task, but tid not mapped any record in DB: " + tid.toString());
            return null;
        }
    }

    @Transactional
    @Override
    public EnkanTaskEntity visible(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setHided(false);
            this.taskRepository.save(existed);
            return existed;
        } else {
            log.warn("try to set visible a task, but tid not mapped any record in DB: " + tid.toString());
            return null;
        }
    }

    @Transactional
    @Override
    public Integer removeAllTranslations(Integer tid) {
        log.warn("Remove all translations for tid: " + tid);
        return this.translateRepository.bulkDeleteByTid(tid);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public TranslatedTask addTranslation(Integer tid, String translation, String img) {
        log.info(String.format("Add translation for twitter [%s] size: %s", tid, translation.length()));
        EnkanTaskEntity chosenOne = this.taskRepository.findByTidForUpdate(tid);
        if (chosenOne != null) {
            List<EnkanTranslateEntity> translations = chosenOne.getTranslations();
            Optional<EnkanTranslateEntity> maxOne = translations
                    .stream()
                    .max(Comparator.comparingInt(EnkanTranslateEntity::getVersion));
            int nextVersion = 0;
            if (maxOne.isPresent()) {
                EnkanTranslateEntity lastTranslation = maxOne.get();
                nextVersion = lastTranslation.getVersion() + 1;
            }
            EnkanTranslateEntity trans = new EnkanTranslateEntity();
            trans.setVersion(nextVersion);
            trans.setTask(chosenOne);
            trans.setImg(img);
            trans.setTranslation(translation);
            translations.add(trans);
            this.taskRepository.save(chosenOne);
            EnkanTranslateEntity latest = this.translateRepository.findFirstByTaskOrderByVersionDesc(chosenOne);
            this.entityManager.refresh(latest);  // refresh for `updatetime` and `newdate`
            return TranslatedTask.of(chosenOne, latest);
        } else {
            log.warn("try to add translation for a task, but tid not mapped any record in DB: " + tid);
            return TranslatedTask.of(null, null);
        }
    }

    @Transactional
    @Override
    public TranslatedTask rollbackTranslation(Integer tid) {
        Optional<EnkanTaskEntity> et = this.taskRepository.findById(tid);
        if (et.isPresent()) {
            EnkanTaskEntity task = et.get();
            List<EnkanTranslateEntity> translations = task.getTranslations();
            if (translations.size() == 0) {
                return TranslatedTask.of(task, null);
            }
            translations.sort((x, y) -> -1 * Integer.compare(x.getVersion(), y.getVersion()));
            EnkanTranslateEntity pendingTrans = translations.remove(0);
            this.translateRepository.delete(pendingTrans);
            EnkanTranslateEntity lastTrans = null;
            if (translations.size() > 0) {
                lastTrans = translations.get(0);
            }
            return TranslatedTask.of(task, lastTrans);
        } else {
            log.warn("try to rollback translation for a task, but tid not mapped any record in DB: " + tid);
            return TranslatedTask.of(null, null);
        }
    }

    @Transactional
    @Override
    public VersionedTranslatedTask getAllTranslation(Integer tid) {
        Optional<EnkanTaskEntity> et = this.taskRepository.findById(tid);
        if (et.isPresent()) {
            EnkanTaskEntity task = et.get();
            List<EnkanTranslateEntity> translations = task.getTranslations();
            translations.sort((x, y) -> -1 * Integer.compare(x.getVersion(), y.getVersion()));
            return VersionedTranslatedTask.of(task, translations);
        } else {
            log.warn("try to get all translations for a task, but tid not mapped any record in DB: " + tid);
            return VersionedTranslatedTask.of(null, null);
        }
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class TranslatedTask {

        private EnkanTaskEntity twitter;

        private EnkanTranslateEntity translation;
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class VersionedTranslatedTask {

        private EnkanTaskEntity twitter;

        private List<EnkanTranslateEntity> translations;
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class CreateTaskReplay {

        private EnkanTaskEntity twitter;

        private boolean alreadyExist;
    }
}
