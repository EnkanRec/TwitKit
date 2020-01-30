/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.service.kvConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;

/**
 * Class : ConfigService
 * Usage :
 */
public interface KVConfigService {

    void setOneDefault(String key, String value);

    String getOneDefault(String key);

    void setOne(String namespace, String key, String value);

    String getOne(String namespace, String key);

    void setMany(String namespace, Map<String, String> configs);

    Map<String, String> getMany(String namespace, Collection<String> keys);

    Map<String, String> getAll(String namespace);

    Map<String, String> getAll();
}
