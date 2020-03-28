/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.yui.repository;

import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import javax.persistence.LockModeType;
import java.util.Collection;
import java.util.List;

/**
 * Class : EnkanTaskRepository
 * Usage :
 */
public interface EnkanTaskRepository extends JpaRepository<EnkanTaskEntity, Integer> {

    boolean existsByStatusId(String statusId);

    EnkanTaskEntity findByUrl(String url);

    EnkanTaskEntity findByStatusId(String statusId);

    List<EnkanTaskEntity> findByStatusIdIn(Collection<String> statusIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT et FROM EnkanTaskEntity et WHERE et.tid = :tid")
    EnkanTaskEntity findByTidForUpdate(Integer tid);

    EnkanTaskEntity findFirstByHidedIsFalseOrderByTidDesc();

    EnkanTaskEntity findFirstByOrderByTidDesc();

    List<EnkanTaskEntity> findAllByTidGreaterThanAndHidedIsFalse(Integer tid);

    EnkanTaskEntity findTop10ByTidOrderByNewdate(Integer tid);
}
