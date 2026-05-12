package com.sunyard.sunafm.util;

import cn.hutool.core.lang.Assert;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.sunyard.sunafm.dto.ValidateRequestParamDTO;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 发票验证工具类
 *
 * @author P-JWei
 * @date 2024/3/22 14:41:05
 * @title
 * @description
 */
//todo 底座有这个工具类
public class InvoiceVerifyUtils {

    /**
     * ocr地址
     */
    private static String ocrUrl = "http://172.1.1.218:5050/api/v1/invoice";

    /**
     * 验真地址
     */
    private static String verifyUrl = "https://fapiao.glority.cn/v1/item/fapiao_validation";

    private static String appKey = "is4qktmstx8ozdf8";

    private static String appSecret = "e547zb53qa2xin48gqvqsz33qtfl4nmxynfr0r3t";

    /**
     * 通过ocr获取发票信息
     *
     * @param imagesBase64
     */
    public String getInvoiceInfoByOcr(String imagesBase64) {
        Assert.notNull(imagesBase64, "文件bae64不能为空");
        // 准备请求参数（表单数据）
        Map<String, Object> paramMap = new HashMap<>(6);
        paramMap.put("image", imagesBase64);
        //发送请求
        HttpResponse response = HttpRequest.post(ocrUrl)
                .form(paramMap)
                .execute();
        // 获取响应体内容
        return response.body();
    }

    /**
     * 进行发票验真
     *
     * @param param
     */
    public String getInvoiceVerifyByRZ(ValidateRequestParamDTO param) {
        Assert.notNull(param,"入参不能为空");
        long timestamp = System.currentTimeMillis() / 1000;
        String token = DigestUtils.md5DigestAsHex((appKey + "+" + timestamp + "+" + appSecret).getBytes(StandardCharsets.UTF_8));
        String checkCode = param.getCheck_code();
        //用于调用接口的checkCode
        String paramCheckCode = checkCode;
        if (checkCode != null && checkCode.length() > 6) {
            //校验码取后六位
            paramCheckCode = checkCode.substring(checkCode.length() - 6, checkCode.length());
        }
        // 准备请求参数（表单数据）
        Map<String, Object> map = new HashMap<>(6);
        map.put("app_key", appKey);
        map.put("timestamp", Long.toString(timestamp));
        map.put("token", token);
        map.put("code", param.getCode());
        map.put("number", param.getNumber());
        map.put("check_code", paramCheckCode);
        map.put("pretax_amount", param.getPretax_amount());
        map.put("date", param.getDate());
        map.put("type", param.getType());
        map.put("total",  param.getTotal());
        //发送请求
        HttpResponse response = HttpRequest.post(verifyUrl)
                .form(map)
                .execute();
        // 获取响应体内容
        return response.body();
    }
}
