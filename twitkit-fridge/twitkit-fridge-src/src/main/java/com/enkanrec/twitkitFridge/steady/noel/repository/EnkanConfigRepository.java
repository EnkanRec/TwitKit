/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.steady.noel.repository;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Class : EnkanConfigRepository
 * Usage :
 */
public interface EnkanConfigRepository extends JpaRepository<EnkanConfigEntity, Long> {

    EnkanConfigEntity findByNamespaceAndConfigKey(String namespace, String key);

    List<EnkanConfigEntity> findAllByNamespaceAndConfigKey(String namespace, String key);

    List<EnkanConfigEntity> findAllByNamespace(String namespace);
}
