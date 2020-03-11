/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.rest;

import com.enkanrec.twitkitFridge.api.form.BaseJsonWarp;
import com.enkanrec.twitkitFridge.api.form.NamespaceForm;
import com.enkanrec.twitkitFridge.api.response.StandardResponse;
import com.enkanrec.twitkitFridge.service.kvConfig.KVConfigService;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取全部配置项
     */
    @ResponseBody
    @RequestMapping(value = "/getall", method = RequestMethod.POST)
    public StandardResponse getAllKVConfigs(@Valid @RequestBody BaseJsonWarp<Object> form) {
        return StandardResponse.ok(this.service.getAll());
    }

    /**
     * 更新默认命名空间下的设置项
     */
    @ResponseBody
    @RequestMapping(value = "/set", method = RequestMethod.POST)
    public StandardResponse setKVConfigsByDefaultNamespace(@Valid @RequestBody BaseJsonWarp<Map<String, Object>> form) throws Exception {
        this.service.setManyDefault(form.getData());
        return StandardResponse.ok("");
    }

    /**
     * 获取默认命名空间下的设置项
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public StandardResponse getKVConfigsByDefaultNamespace(@Valid @RequestBody BaseJsonWarp<List<String>> form) {
        List params = form.getData();
        Map result = this.service.getManyDefault(params);
        return StandardResponse.ok(result);
    }

    /**
     * 清空一个命名空间下的所有配置项
     */
    @ResponseBody
    @RequestMapping(value = "/clear", method = RequestMethod.POST)
    public StandardResponse clearConfigOfNamespace(@Valid @RequestBody BaseJsonWarp<NamespaceForm> form) {
        this.service.clearNamespace(form.getData().getNamespace());
        return StandardResponse.ok();
    }
}
