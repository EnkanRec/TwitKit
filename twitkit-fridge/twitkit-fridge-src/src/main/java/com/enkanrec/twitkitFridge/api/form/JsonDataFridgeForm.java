/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.api.form;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.enkanrec.twitkitFridge.util.JsonUtil;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

/**
 * Class : JsonDataFridgeForm
 * Usage :
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class JsonDataFridgeForm extends BaseFridgeForm {

    @Getter
    private String data;

    @Getter
    private Map<String, Object> mappedData;

    @Getter
    private List<Object> listedData;

    @Getter
    private JsonDataType autoDecodeType;

    public void setData(String data) {
        if (data == null) {
            this.autoDecodeType = JsonDataType.Null;
            this.data = null;
            return;
        }
        this.data = data;
        if (data.startsWith("{")) {
            this.autoDecodeType = JsonDataType.Map;
            this.mappedData = this.dataToMap();
        } else {
            this.autoDecodeType = JsonDataType.List;
            this.listedData = this.dataToList();
        }
    }

    public Map<String, Object> dataToMap() {
        JSONObject jObject = JSONObject.parseObject(this.data);
        return JsonUtil.jsonToMap(jObject);
    }

    public List<Object> dataToList() {
        JSONArray jObject = JSONObject.parseArray(this.data);
        return JsonUtil.toList(jObject);
    }

    public static enum JsonDataType {
        Map,
        List,
        Null
    }
}
