package com.sunyard.module.storage.dto.ecm;

import com.sunyard.framework.common.util.conversion.XmlUtils;
import com.sunyard.framework.common.util.date.DateUtils;
import com.sunyard.insurance.encode.client.EncodeAccessParam;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author liugang
 * @Type com.sunyard.sunam.service.impl.sunecm
 * @Desc 代表影像接口请求数据
 * @date 14:05 2021/9/30
 */
@Data
@Slf4j
public class EcmRequestBusiData {
    /**
     * 交互参数为xml格式
     */
    public static final String FORMAT_XML = "xml";
    /**
     * 接口号-查阅接口
     */
    public static final String CODE_QUERY = "ECM0002";
    /**
     * 接口号-扫描上传接口 ECM0001 （具有扫描仪和高拍仪的功能：ECMW001）
     */
    public static final String CODE_SCAN = "ECM0001";

    /**
     * 接口号-扫描上传接口 具有扫描仪和高拍仪的功能：ECMW001
     */
    public static final String CODE_SCAN_W = "ECMW001";
    /**
     * 接口号-资源请求接口
     */
    public static final String CODE_RESOURCE_ACCESS = "ECM0010";
    /**
     * 接口号-第3方下载接口
     * ECM0001
     * （具有扫描仪和高拍仪的功能：ECMW001）
     */
    public static final String CODE_DOWNLOAD = "ECM0009";
    /**
     * 删除文件
     */
    public static final String CODE_DEL = "ECM0025";

    /**
     * 接口号-复制接口
     */
    public static final String CODE_COPY = "ECMC0023";
    /**
     * 交互参数格式
     */
    private String format = FORMAT_XML;
    /**
     * 接口号
     */
    private String code;
    /**
     * 交互参数
     */
    private BusiData busiData;

    /**
     * 获取加密参数
     * 
     * @param key 密钥
     * @return Result 加密后的字符串
     */
    public String getEncodeParam(String key) {
        try {
            String xmlStr = XmlUtils.marshal(this.busiData);
            log.debug("xml={}", xmlStr);
            return EncodeAccessParam.getEncodeParam("format=" + format + "&code=" + code + "&xml=" + xmlStr,
                DateUtils.getNowDate().getTime(), key);
        } catch (Exception e) {
            log.error("系统异常", e);
        }
        return null;
    }
}
