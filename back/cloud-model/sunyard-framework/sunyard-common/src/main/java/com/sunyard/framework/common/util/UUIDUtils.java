package com.sunyard.framework.common.util;

import java.util.UUID;

/**
 * @author PJW
 */
public class UUIDUtils {

    /**
     * 替换横杠
     *
     * @return String
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 替换横杠
     *
     * @param str 原str
     * @return String
     */
    public static String removePoint(String str) {
        return str.replace(".", "");
    }
}
