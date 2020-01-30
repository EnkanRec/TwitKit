/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api;

import com.enkanrec.twitkitFridge.api.form.JsonDataFridgeForm;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.service.kvConfig.KVConfigService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Class : KVConfigController
 * Usage : KV设置存储器对外接口
 */
@RestController
@RequestMapping("/api/db/kv")
public class KVConfigController {

    private final KVConfigService service;

    public KVConfigController(KVConfigService service) {
        this.service = service;
    }

    @RequestMapping(value = "/getall", method = RequestMethod.POST)
    public StandardResponse getAllKVConfigs() {
        return StandardResponse.ok(this.service.getAll());
    }

    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public StandardResponse setKVConfigsByDefaultNamespace(@Valid JsonDataFridgeForm form) {
        Map params = form.getMappedData();
        this.service.setManyDefault(params);
        return StandardResponse.ok("");
    }

    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public StandardResponse getKVConfigsByDefaultNamespace(@Valid JsonDataFridgeForm form) {
        List params = form.getListedData();
        Map result = this.service.getManyDefault(params);
        return StandardResponse.ok(result);
    }
}
