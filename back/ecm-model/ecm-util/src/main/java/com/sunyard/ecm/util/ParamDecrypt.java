package com.sunyard.ecm.util;

import com.sunyard.insurance.base.util.crypto.Base64WithAccess;
import com.sunyard.insurance.ecm.socket.util.MD5Util;
import com.sunyard.insurance.encode.client.Base64;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ParamDecrypt {
    // Base64解码
    public static String decodeBase64(String encoded) throws Exception {
        return Base64.decode(encoded);
    }

    // HMAC-MD5计算
    public static String calculateHmacMd5(String key, String data) throws Exception {
        return MD5Util.encryptHmacMd5Str(key, data);
    }

    public static Map<String, Object> getDecodeParam(String encodedParam) throws Exception {
        // Step 1: Base64 解码
        try {
            String data =  Base64WithAccess.decode(encodedParam,"utf-8");
            try {
                //4、获取各参数的值
                Map map=new HashMap<>();
                map = EncryptFuncUtils.analysisParam(data);

                Map<String, Object> reMap = new HashMap<String, Object>();
                reMap.put("paramMap", map);
                return map;
            } catch (Exception e) {
                throw new ServletException("4@@根据授权密钥生成签名失败失败，请检查", e);
            }
        }catch (Exception e) {
            log.error("授权密钥解密异常",e);
        }
        return null;
    }


}
