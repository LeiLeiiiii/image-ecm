package com.sunyard.mytool.until;

import java.util.UUID;

public class UUIDUtil {
    // 替换横杠
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 去掉小数点
     *
     * @param str
     * @return
     */
    public static String removePoint(String str) {
        return str.replace(".", "");
    }
}
