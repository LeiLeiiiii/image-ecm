package com.sunyard.sunafm.util;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author P-JWei
 * @date 2024/4/9 10:14:22
 * @title
 * @description
 */
public class MilvusExprUtils {


    /**
     * json转换工具
     * @param map
     * @return
     */
    public static String andEq(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            stringBuilder.append(item.getKey() + " == " + item.getValue());
            stringBuilder.append(" && ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param map
     * @return
     */
    public static String andIn(Map<String, Set<String>> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Set<String>> item : map.entrySet()) {
            stringBuilder.append(item.getKey() + " in " + JSONObject.toJSONString(item.getValue()));
            stringBuilder.append(" && ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonAndEq(String jsonKey, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " == " + item.getValue());
            stringBuilder.append(" && ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonAndIn(String jsonKey, Map<String, Set<String>> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Set<String>> item : map.entrySet()) {
            stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " in " + JSONObject.toJSONString(item.getValue()));
            stringBuilder.append(" && ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonAndEqIn(String jsonKey, Map<String, Set<String>> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Set<String>> item : map.entrySet()) {
            if (item.getValue() instanceof Set) {
                stringBuilder.append("(json_contains_any(file_prop[\""+item.getKey()+"\"]," + JSONObject.toJSONString(item.getValue())+"))");
            }
            stringBuilder.append(" and ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param map
     * @return
     */
    public static String orEq(Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            stringBuilder.append(item.getKey() + " == " + item.getValue());
            stringBuilder.append(" || ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param map
     * @return
     */
    public static String orIn(Map<String, String[]> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> item : map.entrySet()) {
            stringBuilder.append(item.getKey() + " in " + JSONObject.toJSONString(item.getValue()));
            stringBuilder.append(" || ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonOrEq(String jsonKey, Map<String, String> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> item : map.entrySet()) {
            stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " == " + item.getValue());
            stringBuilder.append(" || ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonOrIn(String jsonKey, Map<String, String[]> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String[]> item : map.entrySet()) {
            stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " in " + JSONObject.toJSONString(item.getValue()));
            stringBuilder.append(" || ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param jsonKey
     * @param map
     * @return
     */
    public static String jsonOrEqIn(String jsonKey, Map<String, Object> map) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, Object> item : map.entrySet()) {
            if (item.getValue() instanceof Integer[] || item.getValue() instanceof String[]) {
                stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " in " + JSONObject.toJSONString(item.getValue()));
            } else {
                stringBuilder.append(jsonKey + "[\"" + item.getKey() + "\"]" + " == " + JSONObject.toJSONString(item.getValue()));
            }
            stringBuilder.append(" || ");
        }
        return deleteAfterFour(stringBuilder);
    }

    /**
     * json转换工具
     * @param stringBuilder
     * @return
     */
    private static String deleteAfterFour(StringBuilder stringBuilder) {
        int length = stringBuilder.length();

        // 删除后四位字符
        if (length >= 4) {
            stringBuilder.delete(length - 4, length);
        } else {
            // 如果字符串长度不足四位，则删除所有字符
            stringBuilder.delete(0, length);
        }

        return stringBuilder.toString();
    }

    public static void main(String[] args) {
        //json字段名字：如file_prop
        String jsonKey = "file_prop";
        //属性查询map： key-file_prop.key  value-file_prop.key-value
        //value类型，传单个值Integer\String之类会用==拼接，传数组会用in拼接。
        // 根据json字段属性的类型传value类型，是jsonObject传单个值，是jsonArray传数组
        Map<String, Set<String>> map = new HashMap<>();
        //如file_prop下的fileMD5是jsonObject，
//        map.put("fileMD5", "asdxcxcxsdadascxczxcsdsdxc");
        //如file_prop下的materialType是jsonArray
        Set<String> set = new HashSet<>();
        set.add("code1");
        set.add("code2");
        map.put("materialType", set);

        //及map中各个用and（&&）拼接，与查
        String andExpr = jsonAndEqIn(jsonKey, map);
        System.out.println(andExpr);
        //及map中各个用or（||）拼接，或查
//        String orExpr = jsonOrEqIn(jsonKey, map);
//        System.out.println(orExpr);

    }
}
