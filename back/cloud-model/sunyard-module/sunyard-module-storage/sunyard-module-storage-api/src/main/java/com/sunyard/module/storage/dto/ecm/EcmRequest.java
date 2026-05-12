package com.sunyard.module.storage.dto.ecm;

import lombok.Data;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc
 * @date 14:05 2021/9/30
 */
@Data
public class EcmRequest {

    /**
     * 请求IP
     */
    private String ip;

    /**
     * 请求端口
     */
    private String port;

    /**
     * 接口地址
     */
    private String address;

    /**
     * 参数加密密钥
     */
    private String key;

    /**
     * 请求参数
     */
    private EcmRequestBusiData ecmRequestBusiData;

    /**
     * 获取请求地址
     *
     * @return Result 请求地址
     */
    public String getRequestUrl() {
        StringBuilder urlB = new StringBuilder();
        urlB.append("http://").append(ip).append(":").append(port).append("/").append(address).append("?");
        if (ecmRequestBusiData != null) {
            urlB.append("data=").append(ecmRequestBusiData.getEncodeParam(key));
        }
        return urlB.toString();
    }
}
