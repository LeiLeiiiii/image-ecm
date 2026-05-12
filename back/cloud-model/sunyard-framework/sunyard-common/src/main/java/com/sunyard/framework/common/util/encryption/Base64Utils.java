package com.sunyard.framework.common.util.encryption;
/*
 * Project: com.sunyard.am.utils
 *
 * File Created at 2021/7/1
 *
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license.
 */




import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

/**
 * @author zhouleibin
 * @Type com.sunyard.am.utils
 * @Desc
 * @date 2021/7/1 15:35
 */
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

    /**
     * Base64字符串转换为MultipartFile
     * @param base64Str Base64编码字符串（可带前缀，如data:image/png;base64,）
     * @param fileName  生成MultipartFile的文件名（需带后缀，如test.png）
     * @return MultipartFile实例
     * @throws Exception 解码或封装异常
     */
    public static MultipartFile convert(String base64Str, String fileName) {
        // 剥离Base64前缀
        String pureBase64Str = base64Str;
        if (base64Str.contains(",")) {
            pureBase64Str = base64Str.split(",")[1];
        }

        // Base64解码为字节数组
        byte[] fileBytes = Base64.getDecoder().decode(pureBase64Str);

        // 封装为MockMultipartFile
        return new MockMultipartFile(
                "file",
                fileName,
                null,
                fileBytes
        );
    }
}
/**
 * Revision history -------------------------------------------------------------------------
 * <p>
 * Date Author Note ------------------------------------------------------------------------- 2021/7/1 zhouleibin creat
 */
