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
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Class : JsonDataFridgeForm
 * Usage : 标准请求体表单。表单会自动解开data字段成Map和List组合的Json结构体
 */
@Slf4j
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
            if (this.getClass() != JsonDataFridgeForm.class) {
                this.autoDispatchMappedDataField();
            }
        } else {
            this.autoDecodeType = JsonDataType.List;
            this.listedData = this.dataToList();
        }
        this.afterSetData();
    }

    public Map<String, Object> dataToMap() {
        JSONObject jObject = JSONObject.parseObject(this.data);
        return JsonUtil.jsonToMap(jObject);
    }

    public List<Object> dataToList() {
        JSONArray jObject = JSONObject.parseArray(this.data);
        return JsonUtil.toList(jObject);
    }

    public Map<String, Object> asMap() {
        return this.mappedData;
    }

    public List<Object> asList() {
        return this.listedData;
    }

    private void autoDispatchMappedDataField() {
        for (Map.Entry<String, Object> kvp : this.mappedData.entrySet()) {
            try {
                Field keyedField = this.getClass().getDeclaredField(kvp.getKey());
                keyedField.setAccessible(true);
                keyedField.set(this, kvp.getValue());
            }
            catch (Exception ignore) {
                // pass
            }
        }
    }

    protected void afterSetData() { }

    public static enum JsonDataType {
        Map,
        List,
        Null
    }
}
