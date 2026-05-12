package com.sunyard.mytool.until;

import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 非空校验工具类
 */
public class AssertUtils {

    /**
     * 判空校验，为空 ，弹出 obj 校验对象 hint 提示语句
     * @param obj obj
     * @param hint 描述
     */
    public static void isNull(Object obj, String hint) {
        Assert.isTrue(obj != null, hint);
        if (obj instanceof String) {
            Assert.isTrue(StringUtils.hasText(((String)obj).trim()), hint);
        } else if (obj instanceof List) {
            List obj1 = (List)obj;
            Assert.isTrue(!CollectionUtils.isEmpty(obj1), hint);
        } else if (obj instanceof Long[]) {
            Assert.isTrue(((Long[])obj).length > 0, hint);
        } else if (obj instanceof Integer[]) {
            Assert.isTrue(((Integer[])obj).length > 0, hint);
        } else if (obj instanceof Double[]) {
            Assert.isTrue(((Double[])obj).length > 0, hint);
        } else if (obj instanceof Boolean[]) {
            Assert.isTrue(((Boolean[])obj).length > 0, hint);
        } else if (obj instanceof byte[]) {
            Assert.isTrue(((byte[])obj).length > 0, hint);
        }
    }

    /**
     * 判空校验 不为空，弹出 obj 校验对象 hint 提示语句
     * @param obj obj
     * @param hint 描述
     */
    public static void notNull(Object obj, String hint) {
        if (obj instanceof String) {
            Assert.isTrue(!StringUtils.hasText(String.valueOf(obj)), hint);
        } else if (obj instanceof List) {
            List obj1 = (List)obj;
            Assert.isTrue(CollectionUtils.isEmpty(obj1), hint);
        } else if (obj instanceof Integer || obj instanceof Long || obj instanceof Date || obj instanceof Double
            || obj instanceof BigDecimal || obj instanceof Boolean || obj instanceof Map) {
            Assert.isTrue(obj == null, hint);
        } else {
            Assert.isTrue(obj == null, hint);
        }
    }

    /**
     * 判空校验 不为空，弹出 obj 校验对象 hint 提示语句
     * @param flag 条件
     * @param hint 描述
     */
    public static void isTrue(boolean flag, String hint) {
        if (flag) {
            Assert.isTrue(!flag, hint);
        }
    }

}
