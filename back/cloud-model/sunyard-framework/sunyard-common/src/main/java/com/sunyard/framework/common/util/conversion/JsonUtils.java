package com.sunyard.framework.common.util.conversion;
/*
 * Project: com.sunyard.am.utils
 *
 * File Created at 2021/6/30
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @Type com.sunyard.am.utils
 * @Desc
 * @author zhouleibin
 * @date 2021/6/30 9:30
 * @version
 */
@Slf4j
public class JsonUtils {

    /**
     * object转jsonString
     * @param object obj
     * @return String
     */
    public static String toJSONString(Object object) {
        return JSONObject.toJSONString(object);
    }

    /**
     * 解析String成obj
     * @param text 内容
     * @return json对象
     */
    public static JSONObject parseObject(String text) {
        return JSONObject.parseObject(text);
    }

    /**
     * obj转Json
     * @param obj 对象
     * @return Json对象
     */
    public static JSONObject parseObject(Object obj) {
        return JSONObject.parseObject(JSONObject.toJSONString(obj));
    }

    /**
     * jsonString转Obj
     * @param text json的String
     * @param clazz 对象clazz
     * @return T
     * @param <T> 需要返回的obj类型
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        try {
            return JSONObject.parseObject(text, clazz);
        } catch (Exception e) {
            log.error("系统异常", e);
            return null;
        }

    }

    /**
     * 将字符串解析成指定类型的对象
     * 使用 {@link #parseObject(String, Class)} 时，在@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS) 的场景下，
     * 如果 text 没有 class 属性，则会报错。此时，使用这个方法，可以解决。
     *
     * @param text 字符串
     * @param clazz 类型
     * @return Result 对象
     */
    public static <T> T parseObject2(String text, Class<T> clazz) {
        if (StrUtil.isEmpty(text)) {
            return null;
        }
        return JSONUtil.toBean(text, clazz);
    }

    /**
     * 解析String成Json数组
     * @param text string的内容
     * @return json数组
     */
    public static JSONArray parseArray(String text) {
        return JSONObject.parseArray(text);
    }

    /**
     * 解析Obj成json数组
     * @param obj obj
     * @return json数组
     */
    public static JSONArray parseArray(Object obj) {
        return JSONObject.parseArray(JSONObject.toJSONString(obj));
    }

    /**
     * 解析jsonString成指定的对象
     * @param text jsonString格式
     * @param clazz 需要转换的对象
     * @return T
     * @param <T> 泛型
     */
    public static <T> List<T> parseArray(String text, Class<T> clazz) {
        return JSONObject.parseArray(text, clazz);
    }
}

/**
 * Revision history -------------------------------------------------------------------------
 *
 * Date Author Note ------------------------------------------------------------------------- 2021/6/30 zhouleibin creat
 */
