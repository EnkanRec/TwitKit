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
        Query insertQuery = this.entityManager.createNativeQuery("INSERT IGNORE INTO enkan_task (url, content, media) VALUES (:url, :content, :media)");
        insertQuery.setParameter("url", url);
        insertQuery.setParameter("content", content);
        insertQuery.setParameter("media", media);
        int affected = insertQuery.executeUpdate();
        EnkanTaskEntity task = this.taskRepository.findByUrl(url);
        return CreateTaskReplay.of(task, affected == 0);
    }

    @Transactional
    @Override
    public List<CreateTaskReplay> addTaskByBulk(List<Map<String, String>> twitters) {
        List<CreateTaskReplay> result = new ArrayList<>();
        if (twitters.size() == 0) {
            return result;
        }

        // 测试存在性，但这里会有幻读的问题，会影响返回“已存在性”字段的准确性，但入库只会有唯一一条记录
        // 比如两个bulk请求同时提交了同一个URL的推文，可能都返回`alreadyExist`为`false`
        // 使用串行隔离级别可以解决这个问题，但有性能开销，不是很必要？
        Set<String> urlSet = new HashSet<>();
        Map<String, Boolean> existMap = new HashMap<>();
        for (Map<String, String> twitter : twitters) {
            String url = twitter.get("url");
            existMap.put(url, this.taskRepository.existsByUrl(url));
            urlSet.add(url);
        }
        if (urlSet.size() != twitters.size()) {
            log.warn("Url distinct set size skewed");
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT IGNORE INTO enkan_task (url, content, media) VALUES ");
        twitters.forEach(t -> sb.append("(?,?,?),"));
        String sqlTemplate = sb.toString().substring(0, sb.length() - 1);
        Query insertQuery = this.entityManager.createNativeQuery(sqlTemplate);
        int paramPointer = 1;
        for (Map<String, String> twitter : twitters) {
            insertQuery.setParameter(paramPointer++, twitter.get("url"));
            insertQuery.setParameter(paramPointer++, twitter.get("content"));
            insertQuery.setParameter(paramPointer++, twitter.get("media"));
        }
        int affected = insertQuery.executeUpdate();
        List<EnkanTaskEntity> tasks = this.taskRepository.findByUrlIn(urlSet);

        for (EnkanTaskEntity task : tasks) {
            result.add(CreateTaskReplay.of(task, existMap.getOrDefault(task.getUrl(), false)));
        }

        return result;
    }

    @Transactional
    @Override
    public EnkanTaskEntity getOneLatestOfVisible() {
        return this.taskRepository.findFirstByHidedIsFalseOrderByTidDesc();
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
        return this.taskRepository.findFirstByOrderByTidDesc();
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
        return this.translateRepository.bulkDeleteByTid(tid);
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    @Override
    public EnkanTranslateEntity addTranslation(Integer tid, String translation, String img) {
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
            return latest;
        } else {
            log.warn("try to add translation for a task, but tid not mapped any record in DB: " + tid.toString());
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

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class CreateTaskReplay {

        private EnkanTaskEntity twitter;

        private boolean alreadyExist;
    }
}
