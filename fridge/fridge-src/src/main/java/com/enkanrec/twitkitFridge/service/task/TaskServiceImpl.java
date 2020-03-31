/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.service.task;

import com.enkanrec.twitkitFridge.api.form.TaskCreationForm;
import com.enkanrec.twitkitFridge.monitor.BulkMonitor;
import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTranslateEntity;
import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTwitterEntity;
import com.enkanrec.twitkitFridge.steady.yui.repository.EnkanTaskRepository;
import com.enkanrec.twitkitFridge.steady.yui.repository.EnkanTranslateRepository;
import com.enkanrec.twitkitFridge.steady.yui.repository.EnkanTwitterRepository;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
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
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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

    private final EnkanTwitterRepository twitterRepository;

    private final BulkMonitor bulkMonitor;

    @PersistenceContext(unitName = "entityManagerFactoryYui")
    private EntityManager entityManager;

    @SuppressWarnings("UnstableApiUsage")
    private BloomFilter<String> cacheFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 102400, 0.005);

    public TaskServiceImpl(EnkanTaskRepository taskRepository,
                           EnkanTranslateRepository translateRepository,
                           EnkanTwitterRepository twitterRepository,
                           BulkMonitor bulkMonitor) {
        this.taskRepository = taskRepository;
        this.translateRepository = translateRepository;
        this.twitterRepository = twitterRepository;
        this.bulkMonitor = bulkMonitor;
    }

    @Deprecated
    @Transactional
    public CreateTaskReplay addTask(String url, String content, String media) {
        log.info(String.format("Begin add twitter [%s], size: %s", url, content.length()));
        Query insertQuery = this.entityManager.createNativeQuery("INSERT IGNORE INTO enkan_task (url, content, media) VALUES (:url, :content, :media)");
        insertQuery.setParameter("url", url);
        insertQuery.setParameter("content", content);
        insertQuery.setParameter("media", media);
        int affected = insertQuery.executeUpdate();
        log.info(String.format("Pre-commit twitter [%s]", url));
        EnkanTaskEntity task = this.taskRepository.findByUrl(url);
        return CreateTaskReplay.of(task, null, affected == 0);
    }

    @Transactional
    @Override
    public CreateTaskReplay addTask(TaskCreationForm twitter) {
        log.info(String.format("Begin add twitter [%s], size: %s", twitter.getUrl(), twitter.getContent().length()));
        EnkanTwitterEntity twitterUser = this.getTwitterUser(twitter);
        String pubDateStr = twitter.getPub_date();
        Timestamp pubTs = Timestamp.from(ZonedDateTime.parse(pubDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
        Query insertQuery = this.entityManager.createNativeQuery("INSERT IGNORE INTO enkan_task (url, content, media, pub_date, status_id, twitter_uid, ref_tid, extra) VALUES (:url, :content, :media, :pub_date, :status_id, :twitter_uid, :ref_tid, :extra)");
        insertQuery.setParameter("url", twitter.getUrl());
        insertQuery.setParameter("content", twitter.getContent());
        insertQuery.setParameter("media", twitter.getMedia());
        insertQuery.setParameter("status_id", twitter.getStatus_id());
        insertQuery.setParameter("pub_date", pubTs);
        insertQuery.setParameter("twitter_uid", twitterUser.getTwitterUid());
        insertQuery.setParameter("ref_tid", twitter.getRef());
        insertQuery.setParameter("extra", twitter.getExtra());
        int affected = insertQuery.executeUpdate();
        log.info(String.format("Pre-commit twitter [%s]", twitter.getUrl()));
        EnkanTaskEntity task = this.taskRepository.findByStatusId(twitter.getStatus_id());
        return CreateTaskReplay.of(task, twitterUser,affected == 0);
    }

    @Deprecated
    @SuppressWarnings("UnstableApiUsage")
    @Transactional
    @Override
    public List<CreateTaskReplay> addTaskByBulkWithCache(List<TaskCreationForm> twitters) {
        List<TaskCreationForm> distinctForms = new ArrayList<>();
        int falsePositiveCount = 0;
        for (TaskCreationForm tcf : twitters) {
            String url = tcf.getUrl();
            if (this.cacheFilter.mightContain(url)) {
                EnkanTaskEntity testOne = this.taskRepository.findByUrl(url);
                if (testOne == null) {
                    distinctForms.add(tcf);
                    falsePositiveCount++;
                }
            } else {
                this.cacheFilter.put(url);
                distinctForms.add(tcf);
            }
        }
        List<CreateTaskReplay> results = this.addTaskByBulk(distinctForms);
        this.bulkMonitor.bloomQueryCounter.inc(twitters.size());
        this.bulkMonitor.bloomHitCounter.inc(distinctForms.size());
        this.bulkMonitor.bloomFalsePositiveCounter.inc(falsePositiveCount);
        return results;
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
        // 比如两个bulk请求同时提交了同一个statusId的推文，可能都返回`alreadyExist`为`false`
        // 使用串行隔离级别可以解决这个问题，但有性能开销，不是很必要？
        Set<String> statusIdSet = new HashSet<>();
        Map<String, Boolean> existMap = new HashMap<>();
        for (TaskCreationForm twitter : twitters) {
            String statusId = twitter.getStatus_id();
            existMap.put(statusId, this.taskRepository.existsByStatusId(statusId));
            statusIdSet.add(statusId);
        }
        if (statusIdSet.size() != twitters.size()) {
            log.warn(String.format("Url distinct set size skewed, set size: %s, twi size: %s",
                    statusIdSet.size(), twitters.size()));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT IGNORE INTO enkan_task (url, content, media, pub_date, status_id, twitter_uid, ref_tid, extra) VALUES ");
        twitters.forEach(t -> sb.append("(?,?,?,?,?,?,?,?),"));
        String sqlTemplate = sb.toString().substring(0, sb.length() - 1);
        log.info(String.format("Bulk sql length is: %s", sqlTemplate.length()));
        Query insertQuery = this.entityManager.createNativeQuery(sqlTemplate);
        int paramPointer = 1;
        for (TaskCreationForm twitter : twitters) {
            String pubDateStr = twitter.getPub_date();
            Timestamp pubTs = Timestamp.from(ZonedDateTime.parse(pubDateStr, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
            EnkanTwitterEntity twitterUser = this.getTwitterUser(twitter);

            insertQuery.setParameter(paramPointer++, twitter.getUrl());
            insertQuery.setParameter(paramPointer++, twitter.getContent());
            insertQuery.setParameter(paramPointer++, twitter.getMedia());
            insertQuery.setParameter(paramPointer++, pubTs);
            insertQuery.setParameter(paramPointer++, twitter.getStatus_id());
            insertQuery.setParameter(paramPointer++, twitterUser.getTwitterUid());
            insertQuery.setParameter(paramPointer++, twitter.getRef());
            insertQuery.setParameter(paramPointer++, twitter.getExtra());
        }
        int affected = insertQuery.executeUpdate();
        log.info("Pre-commit bulk with affected count: " + affected);
        List<EnkanTaskEntity> tasks = this.taskRepository.findByStatusIdIn(statusIdSet);

        for (EnkanTaskEntity task : tasks) {
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(task.getTwitterUid());
            result.add(CreateTaskReplay.of(task, twiUser, existMap.getOrDefault(task.getStatusId(), false)));
        }

        this.bulkMonitor.totalCounter.inc(twitters.size());
        return result;
    }

    private EnkanTwitterEntity getTwitterUser(TaskCreationForm twitter) {
        EnkanTwitterEntity twitterUser = this.twitterRepository.findByTwitterUid(twitter.getUser_twitter_uid());
        if (twitterUser == null) {
            twitterUser = new EnkanTwitterEntity();
        }
        twitterUser.setAvatar(twitter.getUser_avatar());
        twitterUser.setDisplay(twitter.getUser_display());
        twitterUser.setName(twitter.getUser_name());
        twitterUser.setTwitterUid(twitter.getUser_twitter_uid());
        twitterUser = this.twitterRepository.saveAndFlush(twitterUser);
        this.entityManager.refresh(twitterUser);
        log.info("twitter user updated: " + twitterUser.toString());
        return twitterUser;
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
    public TaskReplay getOneLatestOfVisible() {
        EnkanTaskEntity et = this.taskRepository.findFirstByHidedIsFalseOrderByTidDesc();
        if (et == null) {
            log.warn("Retrieve last visible task, but return null");
            return TaskReplay.of(null, null);
        } else {
            log.info(String.format("Retrieve last visible task, response tid: %s with updatetime: %s",
                    et.getTid(), et.getUpdatetime()));
        }
        EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(et.getTwitterUid());
        return TaskReplay.of(et, twiUser);
    }

    @Transactional
    @Override
    public TranslatedTask getOneLatestOfVisibleWithTranslation() {
        EnkanTaskEntity entity = this.taskRepository.findFirstByHidedIsFalseOrderByTidDesc();
        if (entity != null) {
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(entity.getTwitterUid());
            List<EnkanTranslateEntity> translations = entity.getTranslations();
            if (translations.isEmpty()) {
                return TranslatedTask.of(entity, twiUser, null);
            }
            EnkanTranslateEntity lastTrans = translations.get(translations.size() - 1);
            return TranslatedTask.of(entity, twiUser, lastTrans);
        }
        return TranslatedTask.of(null, null, null);
    }

    @Transactional
    @Override
    public TaskReplay getOneLatest() {
        EnkanTaskEntity et = this.taskRepository.findFirstByOrderByTidDesc();
        if (et == null) {
            log.warn("Retrieve actual last task with largest tid, but return null");
            return TaskReplay.of(null, null);
        } else {
            log.info(String.format("Retrieve actual last task with largest tid, response tid: %s with updatetime: %s",
                    et.getTid(), et.getUpdatetime()));
        }
        EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(et.getTwitterUid());
        return TaskReplay.of(et, twiUser);
    }

    @Transactional
    @Override
    public TranslatedTask getOneWithTranslation(Integer tid) {
        Optional<EnkanTaskEntity> task = this.taskRepository.findById(tid);
        if (task.isPresent()) {
            EnkanTaskEntity taskEntity = task.get();
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(taskEntity.getTwitterUid());
            EnkanTranslateEntity translateEntity = this.translateRepository.findFirstByTaskOrderByVersionDesc(taskEntity);
            return TranslatedTask.of(taskEntity, twiUser, translateEntity);
        } else {
            log.warn("try to get task, but tid not mapped any record in DB: " + tid.toString());
            return TranslatedTask.of(null, null, null);
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
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(task.getTwitterUid());
            if (versionedTrans.size() == 0) {
                result.add(TranslatedTask.of(task, twiUser, null));
            } else {
                versionedTrans.sort((x, y) ->
                        -1 * Integer.compare(x.getVersion(), y.getVersion()));
                EnkanTranslateEntity translated = versionedTrans.get(0);
                result.add(TranslatedTask.of(task, twiUser, translated));
            }
        }
        return result;
    }

    @Transactional
    @Override
    public TaskReplay updateComment(Integer tid, String comment) {
        log.info(String.format("Update comment for twitter [%s] size: %s", tid, comment.length()));
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setComment(comment);
            this.taskRepository.save(existed);
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(existed.getTwitterUid());
            return TaskReplay.of(existed, twiUser);
        } else {
            log.warn("try to comment a task, but tid not mapped any record in DB: " + tid.toString());
            return TaskReplay.of(null, null);
        }
    }

    @Transactional
    @Override
    public TaskReplay hide(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setHided(true);
            this.taskRepository.save(existed);
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(existed.getTwitterUid());
            return TaskReplay.of(existed, twiUser);
        } else {
            log.warn("try to hide a task, but tid not mapped any record in DB: " + tid.toString());
            return TaskReplay.of(null, null);
        }
    }

    @Transactional
    @Override
    public TaskReplay visible(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setHided(false);
            this.taskRepository.save(existed);
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(existed.getTwitterUid());
            return TaskReplay.of(existed, twiUser);
        } else {
            log.warn("try to set visible a task, but tid not mapped any record in DB: " + tid.toString());
            return TaskReplay.of(null, null);
        }
    }

    @Transactional
    @Override
    public TaskReplay setPublished(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setPublished(true);
            this.taskRepository.save(existed);
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(existed.getTwitterUid());
            return TaskReplay.of(existed, twiUser);
        } else {
            log.warn("try to set a task published, but tid not mapped any record in DB: " + tid.toString());
            return TaskReplay.of(null, null);
        }
    }

    @Transactional
    @Override
    public TaskReplay setUnpublished(Integer tid) {
        Optional<EnkanTaskEntity> chosenOne = this.taskRepository.findById(tid);
        if (chosenOne.isPresent()) {
            EnkanTaskEntity existed = chosenOne.get();
            existed.setPublished(false);
            this.taskRepository.save(existed);
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(existed.getTwitterUid());
            return TaskReplay.of(existed, twiUser);
        } else {
            log.warn("try to reset a task unpublished, but tid not mapped any record in DB: " + tid.toString());
            return TaskReplay.of(null, null);
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
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(chosenOne.getTwitterUid());
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
            return TranslatedTask.of(chosenOne, twiUser, latest);
        } else {
            log.warn("try to add translation for a task, but tid not mapped any record in DB: " + tid);
            return TranslatedTask.of(null, null, null);
        }
    }

    @Transactional
    @Override
    public TranslatedTask rollbackTranslation(Integer tid) {
        Optional<EnkanTaskEntity> et = this.taskRepository.findById(tid);
        if (et.isPresent()) {
            EnkanTaskEntity task = et.get();
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(task.getTwitterUid());
            List<EnkanTranslateEntity> translations = task.getTranslations();
            if (translations.size() == 0) {
                return TranslatedTask.of(task, twiUser, null);
            }
            translations.sort((x, y) -> -1 * Integer.compare(x.getVersion(), y.getVersion()));
            EnkanTranslateEntity pendingTrans = translations.remove(0);
            this.translateRepository.delete(pendingTrans);
            EnkanTranslateEntity lastTrans = null;
            if (translations.size() > 0) {
                lastTrans = translations.get(0);
            }
            return TranslatedTask.of(task, twiUser, lastTrans);
        } else {
            log.warn("try to rollback translation for a task, but tid not mapped any record in DB: " + tid);
            return TranslatedTask.of(null, null, null);
        }
    }

    @Transactional
    @Override
    public VersionedTranslatedTask getAllTranslation(Integer tid) {
        Optional<EnkanTaskEntity> et = this.taskRepository.findById(tid);
        if (et.isPresent()) {
            EnkanTaskEntity task = et.get();
            EnkanTwitterEntity twiUser = this.twitterRepository.findByTwitterUid(task.getTwitterUid());
            List<EnkanTranslateEntity> translations = task.getTranslations();
            translations.sort((x, y) -> -1 * Integer.compare(x.getVersion(), y.getVersion()));
            return VersionedTranslatedTask.of(task, twiUser, translations);
        } else {
            log.warn("try to get all translations for a task, but tid not mapped any record in DB: " + tid);
            return VersionedTranslatedTask.of(null, null, null);
        }
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class TranslatedTask {

        private EnkanTaskEntity twitter;

        private EnkanTwitterEntity user;

        private EnkanTranslateEntity translation;
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class VersionedTranslatedTask {

        private EnkanTaskEntity twitter;

        private EnkanTwitterEntity user;

        private List<EnkanTranslateEntity> translations;
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class CreateTaskReplay {

        private EnkanTaskEntity twitter;

        private EnkanTwitterEntity user;

        private boolean alreadyExist;
    }

    @Data
    @EqualsAndHashCode
    @AllArgsConstructor(staticName = "of")
    public static class TaskReplay {

        private EnkanTaskEntity twitter;

        private EnkanTwitterEntity user;
    }
}
