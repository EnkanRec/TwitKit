/*
 * Author : Rinka
 * Date   : 2020/2/29
 */
package com.enkanrec.twitkitFridge.steady.yui.repository;

import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanTwitterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnkanTwitterRepository extends JpaRepository<EnkanTwitterEntity, Integer> {

    EnkanTwitterEntity findByTwitterUid(String twitterUid);
}
