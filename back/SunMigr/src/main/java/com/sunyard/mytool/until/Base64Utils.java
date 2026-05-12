package com.sunyard.mytool.until;



import java.io.UnsupportedEncodingException;
import java.util.Base64;


public class Base64Utils {
    /**
     * String转base64
     * @param str string
     * @return base64的String
     */
    public static String encodeBase64(String str) {
        try {
            return Base64.getEncoder().encodeToString(str.getBytes());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * base64转String
     * @param str base64String
     * @return
     */
    public static String decodeBase64(String str) {
        try {
            byte[] byte64 = Base64.getDecoder().decode(str.getBytes("UTF-8"));
            return new String(byte64, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}

