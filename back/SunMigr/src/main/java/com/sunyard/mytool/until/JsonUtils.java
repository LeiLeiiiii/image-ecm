

package com.sunyard.mytool.until;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger(JsonUtils.class);

    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object);
    }

    public static JSONObject parseObject(String text) {
        return JSONObject.parseObject(text);
    }

    public static JSONObject parseObject(Object obj) {
        return JSONObject.parseObject(JSONObject.toJSONString(obj));
    }

    public static <T> T parseObject(String text, Class<T> clazz) {
        try {
            return (T)JSONObject.parseObject(text, clazz);
        } catch (Exception e) {
            log.error("系统异常", e);
            return null;
        }
    }

    public static <T> T parseObject2(String text, Class<T> clazz) {
        return (T)(StrUtil.isEmpty(text) ? null : JSONUtil.toBean(text, clazz));
    }

    public static JSONArray parseArray(String text) {
        return JSONObject.parseArray(text);
    }

    public static JSONArray parseArray(Object obj) {
        return JSONObject.parseArray(JSONObject.toJSONString(obj));
    }

    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSONObject.parseArray(text, clazz);
    }
}
