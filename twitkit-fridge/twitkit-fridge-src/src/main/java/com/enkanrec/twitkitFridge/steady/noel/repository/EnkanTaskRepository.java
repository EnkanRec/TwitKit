/*
 * Author : Rinka
 * Date   : 2020/2/4
 */
package com.enkanrec.twitkitFridge.steady.noel.repository;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Class : EnkanTaskRepository
 * Usage :
 */
public interface EnkanTaskRepository extends JpaRepository<EnkanTaskEntity, Integer> {

    EnkanTaskEntity findFirstByOrderByTidDesc();

    EnkanTaskEntity findTop10ByTidOrderByNewdate(Integer tid);
}
