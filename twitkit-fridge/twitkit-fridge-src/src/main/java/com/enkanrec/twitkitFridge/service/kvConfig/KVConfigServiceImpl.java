/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.service.kvConfig;

import com.enkanrec.twitkitFridge.steady.noel.entity.EnkanConfigEntity;
import com.enkanrec.twitkitFridge.steady.noel.repository.EnkanConfigRepository;
import org.springframework.stereotype.Service;

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

    private final EnkanConfigRepository repository;

    public KVConfigServiceImpl(EnkanConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public void setOne(String namespace, String key, String value) {

    }

    @Override
    public String getOne(String namespace, String key) {
        return null;
    }

    @Override
    public void setMany(String namespace, Map<String, String> configs) {

    }

    @Override
    public Map<String, String> getMany(String namespace, Collection<String> keys) {
        return null;
    }

    @Override
    public Map<String, String> getAll(String namespace) {
        return null;
    }

    @Override
    public Map<String, String> getAll() {
        Map<String, String> result = new HashMap<>();
        List<EnkanConfigEntity> allConfig = repository.findAll();
        allConfig.forEach(c -> result.put(c.getKey(), c.getValue()));
        return result;
    }
}
