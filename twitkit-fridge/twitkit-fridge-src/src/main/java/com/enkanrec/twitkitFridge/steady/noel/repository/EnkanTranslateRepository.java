/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.noel.repository;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTranslateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Class : EnkanTranslateRepository
 * Usage :
 */
public interface EnkanTranslateRepository extends JpaRepository<EnkanTranslateEntity, Integer> {

    @Query(value = "SELECT * FROM enkan_translate t, (SELECT tid, max(version) maxVersion FROM enkan_translate AS et GROUP BY et.tid) tr WHERE t.tid = tr.tid AND t.version = tr.maxVersion", nativeQuery = true)
    List<EnkanTranslateEntity> getTranslationsWithLatestVersion();

    EnkanTranslateEntity getFirstByTidOrderByVersionDesc(Integer tid);
}
