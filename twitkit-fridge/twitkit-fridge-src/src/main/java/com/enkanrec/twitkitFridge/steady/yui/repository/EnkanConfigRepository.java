/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.steady.yui.repository;

import com.enkanrec.twitkitFridge.steady.yui.entity.EnkanConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Class : EnkanConfigRepository
 * Usage :
 */
public interface EnkanConfigRepository extends JpaRepository<EnkanConfigEntity, Integer> {

    EnkanConfigEntity findByNamespaceAndConfigKey(String namespace, String key);

    List<EnkanConfigEntity> findAllByNamespaceAndConfigKey(String namespace, String key);

    List<EnkanConfigEntity> findAllByNamespace(String namespace);

    void deleteAllByNamespace(String namespace);
}
