package com.sunyard.sunafm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * base64工具类
 */
public class Base64Utils {
    private static final Map<Character, String> ENCODING_MAP = new HashMap<>();
    private static final Map<String, Character> DECODING_MAP = new HashMap<>();
    private static final String PREFIX = "_";
    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWSYZ";
    private static final int MAX_SPECIAL_CHARS = ALPHABET.length();

    static {
        // 假设我们要编码的特殊字符集
        String specialChars = "!@#$%^&*()_+<>:\"-}[{]= ～！？|/\\—?";
        for (int i = 0; i < specialChars.length() && i < MAX_SPECIAL_CHARS; i++) {
            char specialChar = specialChars.charAt(i);
            String encoded = PREFIX + ALPHABET.charAt(i);
            ENCODING_MAP.put(specialChar, encoded);
            DECODING_MAP.put(encoded, specialChar);
        }
    }

    /**
     * 加密
     * @param input
     * @return
     */
    public static String encode(String input) {
        StringBuilder encoded = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (ENCODING_MAP.containsKey(c)) {
                encoded.append(ENCODING_MAP.get(c));
            } else {
                encoded.append(c);
            }
        }
        return encoded.toString();
    }

    /**
     * 解码
     * @param encoded
     * @return
     */
    public static String decode(String encoded) {
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < encoded.length(); ) {
            if (encoded.charAt(i) == PREFIX.charAt(0)) {
                int endIndex = i + PREFIX.length() + 1;
                if (endIndex <= encoded.length() && Character.isLetter(encoded.charAt(i + PREFIX.length()))) {
                    String encodedChar = encoded.substring(i, endIndex);
                    if (DECODING_MAP.containsKey(encodedChar)) {
                        decoded.append(DECODING_MAP.get(encodedChar));
                        i = endIndex;
                    } else {
                        // 如果不是有效的编码，则直接添加字符
                        decoded.append(encoded.charAt(i));
                        i++;
                    }
                } else {
                    // 如果下划线后没有字母，则直接添加下划线
                    decoded.append(PREFIX);
                    i++;
                }
            } else {
                decoded.append(encoded.charAt(i));
                i++;
            }
        }
        return decoded.toString();
    }

    public static void main(String[] args) {
        String original = "zl-1";
        String encoded = encode(original);
        System.out.println("Encoded: " + encoded);

        String decoded = decode(encoded);
        System.out.println("Decoded: " + decoded);
    }
}