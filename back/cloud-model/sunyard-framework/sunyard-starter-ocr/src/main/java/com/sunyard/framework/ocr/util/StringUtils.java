package com.sunyard.framework.ocr.util;

/**
 *
 * @author PJW*/
public class StringUtils {

    public static final String MSG_GET_MESSAGE_FAILDED =
        "Get message failded, message={x}, i={x}, pos={x}, replace={x}.";

    /**
     * str非空判断
     * @param cs char数据
     * @return boolean
     */
    public static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     *  str非空判断
     * @param cs char数据
     * @return boolean
     */
    public static boolean isBlank(final CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int l = cs.length();
        if (l > 0) {
            for (int i = 0; i < l; i++) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }
}
