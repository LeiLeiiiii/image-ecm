package com.sunyard.ecm;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author： yzy
 * @create： 16点19分
 * @desc: EcmUserDTO中的roleCode自定义反序列化器
 */
public  class RoleCodeDeserializer extends JsonDeserializer<List<String>> {
    @Override
    public List<String> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // 判断输入的类型，如果是一个数组（List），直接返回
        if (p.isExpectedStartArrayToken()) {
            // 直接解析成 List 类型
            return p.readValueAs(List.class);
        }

        // 如果是单一字符串，返回一个包含该字符串的 List
        String value = p.getValueAsString();
        return value != null ? Collections.singletonList(value) : Collections.emptyList();
    }
}