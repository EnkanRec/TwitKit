/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.service.kvConfig;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanConfigEntity;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanConfigRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class : KVConfigServiceImpl
 * Usage :
 */
@Service
public class KVConfigServiceImpl implements KVConfigService {

    private static final String KEY_NAMESPACE_DEFAULT = "___DEFAULT___";

    private final EnkanConfigRepository repository;

    public KVConfigServiceImpl(EnkanConfigRepository repository) {
        this.repository = repository;
    }

    @Transactional
    @Override
    public void setOneDefault(String key, String value) {
        this.setOne(KEY_NAMESPACE_DEFAULT, key, value);
    }

    @Transactional
    @Override
    public String getOneDefault(String key) {
        return this.getOne(KEY_NAMESPACE_DEFAULT, key);
    }

    @Transactional
    @Override
    public void setOne(String namespace, String key, String value) {
        EnkanConfigEntity ece = this.repository.findByNamespaceAndKey(namespace, key);
        if (ece == null) {
            EnkanConfigEntity nObj = new EnkanConfigEntity();
            nObj.setNamespace(namespace);
            nObj.setKey(key);
            nObj.setValue(value);
            this.repository.saveAndFlush(nObj);
        } else {
            ece.setValue(value);
            this.repository.saveAndFlush(ece);
        }
    }

    @Transactional
    @Override
    public String getOne(String namespace, String key) {
        EnkanConfigEntity ece = this.repository.findByNamespaceAndKey(namespace, key);
        if (ece == null) {
            return null;
        } else {
            return ece.getValue();
        }
    }

    @Transactional
    @Override
    public void setMany(String namespace, Map<String, String> configs) {
        for (Map.Entry<String, String> kvp : configs.entrySet()) {
            this.setOne(namespace, kvp.getKey(), kvp.getValue());
        }
    }

    @Transactional
    @Override
    public Map<String, String> getMany(String namespace, Collection<String> keys) {
        Map<String, String> result = new HashMap<>();
        for (String k : keys) {
            String val = this.getOne(namespace, k);
            result.put(k, val);
        }
        return result;
    }

    @Transactional
    @Override
    public Map<String, String> getAll(String namespace) {
        Map<String, String> result = new HashMap<>();
        List<EnkanConfigEntity> eces = this.repository.findAllByNamespace(namespace);
        for (EnkanConfigEntity ece : eces) {
            result.put(ece.getKey(), ece.getValue());
        }
        return result;
    }

    @Transactional
    @Override
    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        List<EnkanConfigEntity> allConfig = repository.findAll();
        allConfig.forEach(c -> result.put(c.getKey(), c.getValue()));
        return result;
    }
}
