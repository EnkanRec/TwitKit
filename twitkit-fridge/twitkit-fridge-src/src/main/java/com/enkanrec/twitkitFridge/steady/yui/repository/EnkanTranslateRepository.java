/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.yui.repository;

import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTaskEntity;
import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTranslateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Class : EnkanTranslateRepository
 * Usage :
 */
public interface EnkanTranslateRepository extends JpaRepository<EnkanTranslateEntity, Integer> {

    @Transactional
    @Query(nativeQuery = true, value = "SELECT * FROM enkan_translate t, (SELECT tid, max(version) maxVersion FROM enkan_translate AS et GROUP BY et.tid) tr WHERE t.tid = tr.tid AND t.version = tr.maxVersion")
    List<EnkanTranslateEntity> getTranslationsWithLatestVersion();

    EnkanTranslateEntity findFirstByTaskOrderByVersionDesc(EnkanTaskEntity task);

    @Modifying
    @Transactional
    @Query("DELETE FROM EnkanTranslateEntity et WHERE et.task.tid = :tid")
    int bulkDeleteByTid(Integer tid);

//    List<EnkanTranslateEntity> findAllByTidIn(Collection<Integer> tidCandidates);
}
