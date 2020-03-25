/*
 * Author : Rinka
 * Date   : 2020/1/30
 */
package com.enkanrec.twitkitFridge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.*;

import java.util.*;

/**
 * Class : JsonUtil
 * Usage :
 */
public class JsonUtil {

    public static final ObjectMapper Mapper = new ObjectMapper();

    public static String dumps(Object dumper) throws JsonProcessingException {
        return JsonUtil.Mapper.writeValueAsString(dumper);
    }

    public static <T> T parseRaw(String jString, TypeReference<T> valueTypeRef) throws JsonProcessingException {
        return JsonUtil.Mapper.readValue(jString, valueTypeRef);
    }

    public static <T> T parse(String jString, Class<T> outerHint) throws JsonProcessingException {
        if (Map.class.isAssignableFrom(outerHint) || List.class.isAssignableFrom(outerHint)) {
            return (T) JsonUtil.parse(jString);
        } else {
            return null;
        }
    }

    public static Object parse(String jString) throws JsonProcessingException {
        if (jString == null) {
            return null;
        }
        if (jString.startsWith("{")) {
            ObjectNode scoped = JsonUtil.Mapper.readValue(jString, ObjectNode.class);
            return JsonUtil.parseMap(scoped);
        } else if (jString.startsWith("[")) {
            ArrayNode scoped = JsonUtil.Mapper.readValue(jString, ArrayNode.class);
            return JsonUtil.parseList(scoped);
        } else {
            return jString;
        }
    }

    public static Map<String, Object> parseMap(ObjectNode jNode) throws JsonProcessingException {
        Map<String, Object> scoped = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> iter = jNode.fields();
        while (iter.hasNext()) {
            Map.Entry<String, JsonNode> iterItem = iter.next();
            String key = iterItem.getKey();
            Object value = iterItem.getValue();
            value = getTinyNode(value);
            scoped.put(key, value);
        }
        return scoped;
    }

    public static List<Object> parseList(ArrayNode jArray) throws JsonProcessingException {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < jArray.size(); i++) {
            Object value = jArray.get(i);
            value = getTinyNode(value);
            list.add(value);
        }
        return list;
    }

    private static Object getTinyNode(Object value) throws JsonProcessingException {
        if (value instanceof TextNode) {
            value = ((TextNode) value).asText();
        } else if (value instanceof ArrayNode) {
            value = parseList((ArrayNode) value);
        } else if (value instanceof ObjectNode) {
            value = parseMap((ObjectNode) value);
        } else if (value instanceof NumericNode) {
            value = ((NumericNode) value).numberValue();
        } else if (value instanceof BooleanNode) {
            value = ((BooleanNode) value).booleanValue();
        } else if (value instanceof NullNode) {
            value = null;
        }
        return value;
    }
}
